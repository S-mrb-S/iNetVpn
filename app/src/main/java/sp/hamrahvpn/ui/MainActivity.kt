package sp.hamrahvpn.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import sp.hamrahvpn.R
import sp.hamrahvpn.databinding.ActivityMainBinding
import sp.hamrahvpn.handler.CheckLoginFromApi
import sp.hamrahvpn.handler.GetAllV2ray
import sp.hamrahvpn.interfaces.ChangeServer
import sp.hamrahvpn.model.OpenVpnServerList
import sp.hamrahvpn.util.CheckInternetConnection
import sp.hamrahvpn.util.CountryListManager
import sp.hamrahvpn.util.Data
import sp.hamrahvpn.util.Data.appValStorage
import sp.hamrahvpn.util.EncryptData
import com.google.android.material.navigation.NavigationView
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.mmkv.MMKV
import com.xray.lite.AppConfig
import com.xray.lite.AppConfig.ANG_PACKAGE
import com.xray.lite.service.V2RayServiceManager
import com.xray.lite.ui.BaseActivity
import com.xray.lite.ui.MainAngActivity
import com.xray.lite.ui.adapters.MainRecyclerAdapter
import com.xray.lite.util.AngConfigManager
import com.xray.lite.util.MmkvManager
import com.xray.lite.util.Utils
import com.xray.lite.viewmodel.MainViewModel
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNService.setDefaultStatus
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * @MehrabSp && Github.com/MehraB832 || MehrabSp
 */
class MainActivity : BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    ChangeServer { // , VpnStatus.ByteCountListener, VpnStatus.StateListener
    //====== Variable =======
    private lateinit var binding: ActivityMainBinding

    /**
     * openvpn state
     *
     */
    private val internetStatus: Boolean
        /**
         * Internet connection status.
         */
        get() = connection!!.netCheck(this)
    private val isServiceRunning: Unit
        /**
         * Get service status
         */
        get() {
            setStatus(OpenVPNService.getStatus())
        }


    /**
     * handler
     */

    private var imageCountry: String? = Data.connectionStorage.getString("image", Data.NA)
    private var imageCountryV2ray: String? = null
    private var City: String? = Data.connectionStorage.getString("city", Data.NA)

    private var vpnState: Int =
        0 // 0 --> ninja (no connect) \\ 1 --> loading (ninja (load again)) (connecting) \\ 2 --> connected (wifi (green logo))

    private var footerState: Int =
        1 // 0 --> main_data \\ 1 --> main_today \\ 2 --> v2ray test layout

    private var isSetupFirst: Boolean = true

    private var fadeIn1000: Animation? = null
    private var fade_out_1000: Animation? = null

    /**
     *
     */
    private var connection: CheckInternetConnection? = null
//    private var testoo: IOpenVPNAPIService? = null

    // MMKV
    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val settingsStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SETTING,
            MMKV.MULTI_PROCESS_MODE
        )
    }

    // v2ray
    private val adapter by lazy { MainRecyclerAdapter(MainAngActivity()) }
    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startV2Ray()
            }
        }

    // ViewModel (V2ray)
    private val mainViewModel: MainViewModel by viewModels()

    // Usage
    private val df = SimpleDateFormat("dd-MMM-yyyy")
    private var today: String = df.format(Calendar.getInstance().time)

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            moveTaskToBack(true)
            super.onBackPressed()
        }
    }

    private fun checkInformationUser() {
        val uL = appValStorage.getString("usernameLogin", null);
        val uU = appValStorage.getString("usernamePassword", null);

        CheckLoginFromApi.checkIsLogin(
            this@MainActivity,
            uL,
            uU
        ) { getApi, _ ->
            try {
//                Log.d("API", getApi.toString())
                if (!getApi) {
                    showToast("اشتراک شما به پایان رسیده است")
                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(R.anim.fade_in_1000, R.anim.fade_out_500)
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkInformationUser()
//        Data.isStart = Data.connectionStorage.getBoolean("isStart", false)
//        vpnState = Data.connectionStorage.getInt("stateVpn", 0)

        handlerSetupFirst()

        setupDrawer()
        initializeAll() // openvpn
        // save default config for v2ray
        initializeApp()
        setupViewModel()
        copyAssets()

        // Load default config type and save.
        Data.defaultItemDialog = Data.settingsStorage.getInt("default_connection_type", 0)

        setupClickListener()

        sendNotifPermission()
    }

    private fun sendNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RxPermissions(this)
                .request(Manifest.permission.POST_NOTIFICATIONS)
                .subscribe { v: Boolean? ->
                    if (!v!!) Toast.makeText(
                        this,
                        "Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun setupClickListener() {
        binding.llProtocolMain.setOnClickListener {
            if (!Data.isStart) {
                setupMainDialog()
            } else {
                showToast("لطفا اول اتصال را قطع کنید")
            }
        }

        binding.linearLayoutMainHome.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.linearLayoutMainServers.setOnClickListener {

            val servers: Intent = if (Data.defaultItemDialog == 0) {
                Intent(this@MainActivity, MainAngActivity::class.java)
            } else {
                Intent(this@MainActivity, ServerActivity::class.java)
            }
            startActivity(servers)
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
        }

        binding.btnConnection.setOnClickListener {
            if(vpnState != 1){
                when (Data.defaultItemDialog) {
                    1 -> connectToOpenVpn()
                    0 -> connectToV2ray()
                }
            }else{
                when (Data.defaultItemDialog) {
                    1 -> stopVpn()
                    0 -> connectToV2ray()
                }
            }
        }

        binding.layoutTest.setOnClickListener {
            layoutTest();
        }
    }

    private fun setupDrawer() {
        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        val actionBarDrawerToggle =
            ActionBarDrawerToggle(this, binding.drawerLayout, R.string.nav_open, R.string.nav_close)

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        // set listener
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        actionBarDrawerToggle.syncState()

        // to make the Navigation drawer icon always appear on the action bar
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.drawerLayout.useCustomBehavior(GravityCompat.START) //assign custom behavior for "Left" drawer
        binding.drawerLayout.useCustomBehavior(GravityCompat.END) //assign custom behavior for "Right" drawer
        binding.drawerLayout.setRadius(
            GravityCompat.START,
            25f
        ) //set end container's corner radius (dimension)
    }

    private fun setupMainDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(Data.item_txt)
        builder.setSingleChoiceItems(
            Data.item_options,
            Data.defaultItemDialog
        ) { dialog: DialogInterface, which: Int ->  // which --> 0, 1
            Data.settingsStorage.putInt("default_connection_type", which)
            Handler().postDelayed({ dialog.dismiss() }, 300)
            Data.defaultItemDialog = which

            if (which == 0) {
                setNewFooterState(2) //v2ray
            }
            if (which == 1) {
                setNewFooterState(0) //main data
            }

        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun restoreTodayTextTv() {
        val long_usage_today = Data.prefUsageStorage.getLong(today, 0)
        if (long_usage_today < 1000) {
            binding.tvDataTodayText.text =
                "${Data.default_ziro_txt} ${Data.KB}"
        } else if (long_usage_today <= 1000000) {
            binding.tvDataTodayText.text = (long_usage_today / 1000).toString() + Data.KB
        } else {
            binding.tvDataTodayText.text = (long_usage_today / 1000000).toString() + Data.MB
        }
    }

    /**
     * handler
     */

    private fun handlerSetupFirst() {
        // set default
        handleCountryImage()
        handleNewVpnState()
        handleNewFooterState()
        showBubbleHomeAnimation()
    }

    private fun handleErrorWhenConnect() {
        binding.tvMessageTopText.text = Data.connected_catch_txt
        binding.tvMessageBottomText.text =
            Data.connected_catch_check_internet_txt

//        binding.btnConnection.text = Data.connecting_btn
        binding.btnConnection.background =
            this@MainActivity.let {
                ContextCompat.getDrawable(
                    it,
                    R.drawable.button_retry
                )
            }
    }

    private fun handleAUTH() {
        binding.tvMessageTopText.text = "درحال ورود به سرور"
        binding.tvMessageBottomText.text = "لطفا منتظر بمانید"

        binding.btnConnection.text = "لغو"
        binding.btnConnection.background =
            this@MainActivity.let {
                ContextCompat.getDrawable(
                    it,
                    R.drawable.button_retry
                )
            }
    }

    private fun handleNewVpnState() {

        // cancel animation first (fade in)
        if (!isSetupFirst) {

            startAnimation(
                this@MainActivity,
                R.id.la_animation,
                R.anim.fade_in_1000,
                true
            )
            // stop animation
            binding.laAnimation.cancelAnimation()

        }

        // set new animation
        val animationResource = when (vpnState) {
            0 -> R.raw.ninjainsecure // disconnected
            1 -> R.raw.conneting // connecting
            2 -> R.raw.connected_wifi // connected
            else -> R.raw.ninjainsecure // ??
        }
        binding.laAnimation.setAnimation(animationResource)

        when (vpnState) {
            0 -> {
                saveIsStart(false, 0)
//                Data.isStart = false
                // disconnected
                binding.btnConnection.text = Data.disconnected_btn
                binding.btnConnection.background = this@MainActivity.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.button_connect
                    )
                }

                // scale main animation
                binding.laAnimation.scaleX = 1f
                binding.laAnimation.scaleY = 1f

                // bubble

                binding.tvMessageTopText.text = Data.disconnected_txt
                binding.tvMessageBottomText.text = Data.disconnected_txt2
            }

            1 -> {
                // connecting
                binding.btnConnection.text = Data.connecting_btn
                binding.btnConnection.background =
                    this@MainActivity.let {
                        ContextCompat.getDrawable(
                            it,
                            R.drawable.button_retry
                        )
                    }

                // scale
                binding.laAnimation.scaleX = 1.5f
                binding.laAnimation.scaleY = 1.5f

                // bubble

                when (Data.defaultItemDialog) {
                    1 -> {
                        binding.tvMessageTopText.text = Data.connecting_txt + ' ' + City
                    }
                    0 -> {
                        binding.tvMessageTopText.text = Data.connecting_txt
                    }
                }

                binding.tvMessageBottomText.text = ""
            }

            2 -> {
                saveIsStart(true, 2)
//                Data.isStart = true
                // connected
                binding.btnConnection.text = Data.connected_btn
                binding.btnConnection.background = this@MainActivity.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.button_disconnect
                    )
                }

                // scale
                binding.laAnimation.scaleX = 1.5f
                binding.laAnimation.scaleY = 1.5f

                // bubble
                when (Data.defaultItemDialog) {
                    1 -> {
                        binding.tvMessageTopText.text = Data.connected_txt + ' ' + City
                    }
                    0 -> {
                        binding.tvMessageTopText.text = Data.connected_txt
                    }
                }

                binding.tvMessageBottomText.text = "اتصال شما امن است"
            }

            else -> {
                // ??
            }
        }

        // play again
        binding.laAnimation.playAnimation()

    }

    private fun saveIsStart(isStart: Boolean, stateVpn: Int) {
//        Data.connectionStorage.putBoolean("isStart", isStart)
//        Data.connectionStorage.putInt("stateVpn", stateVpn)
        Data.isStart = isStart
    }

    private fun handleNewFooterState() {
        if (!isSetupFirst) {
            // cancel all footer data here
            // ??
        }

        when (footerState) {
            0 -> {
                // main data

//                val handlerToday12 = Handler()
//                handlerToday12.postDelayed({
//                    startAnimation(
//                        this@MainActivity,
//                        R.id.ll_main_today,
//                        R.anim.slide_down_800,
//                        false
//                    )
//                    binding.llMainData.visibility = View.INVISIBLE
//                }, 500)
                val handlerData = Handler()
                handlerData.postDelayed({
                    startAnimation(
                        this@MainActivity,
                        R.id.ll_main_data,
                        R.anim.slide_up_800,
                        true
                    )
                }, 1000)


            }

            1 -> {
                // main today
//                val handlerToday12 = Handler()
//                handlerToday12.postDelayed({
//                    startAnimation(
//                        this@MainActivity,
//                        R.id.ll_main_data,
//                        R.anim.slide_down_800,
//                        false
//                    )
//                    binding.llMainData.visibility = View.INVISIBLE
//                }, 500)

//                // stop vpn
//                val handlerToday12 = Handler()
//                handlerToday12.postDelayed({
//                    startAnimation(
//                        this@MainActivity,
//                        R.id.ll_main_data,
//                        R.anim.slide_down_800,
//                        false
//                    )
//                    binding.llMainData.visibility = View.INVISIBLE
//                }, 500)

                val handlerData = Handler()
                handlerData.postDelayed({
                    startAnimation(
                        this@MainActivity,
                        R.id.ll_main_today,
                        R.anim.slide_up_800,
                        true
                    )
                }, 1000)

            }

            2 -> {
                // layout test (v2ray)

                val handlerData = Handler()
                handlerData.postDelayed({
                    startAnimation(
                        this@MainActivity,
                        R.id.ll_main_layout_test,
                        R.anim.slide_up_800,
                        true
                    )
                }, 1000)

            }
        }
    }

    private fun showBubbleHomeAnimation() {
        if (isSetupFirst) {
            isSetupFirst = false

            fadeIn1000 = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in_1000)
            fade_out_1000 = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out_1000)
            binding.llTextBubble.animation = fadeIn1000

            val handlerToday = Handler()
            handlerToday.postDelayed({
                startAnimation(
                    this@MainActivity,
                    R.id.linearLayoutMainHome,
                    R.anim.anim_slide_down,
                    true
                )
                startAnimation(
                    this@MainActivity,
                    R.id.linearLayoutMainServers,
                    R.anim.anim_slide_down,
                    true
                )
            }, 1000)

        }
    }

    // public
    fun setNewVpnState(newState: Int) {
        vpnState = newState

        handleNewVpnState()
    }

    // public
    fun setNewFooterState(newState: Int) {
        footerState = newState

        handleNewFooterState()
    }

    private fun handleCountryImage() {
        if (imageCountryV2ray != null && imageCountry != null) {
            if (Data.defaultItemDialog == 0) {
                CountryListManager.OpenVpnSetServerList(
                    imageCountryV2ray,
                    binding.ivServers
                ) // v2ray
            } else {
                CountryListManager.OpenVpnSetServerList(imageCountry, binding.ivServers)
            }
        }
    }

    // set animations
    private fun startAnimation(ctx: Context, view: Int, animation: Int, show: Boolean) {
        val element = findViewById<View>(view)
        if (show) {
            element.visibility = View.VISIBLE
        } else {
            element.visibility = View.INVISIBLE
        }
        val anim = AnimationUtils.loadAnimation(ctx, animation)
        element.startAnimation(anim)
    }

    /*
    
    
     */

    private fun connectToV2ray() {
        if (mainViewModel.isRunning.value == true) {
            Utils.stopVService(this)
            setNewVpnState(0)
        } else if ((settingsStorage?.decodeString(AppConfig.PREF_MODE) ?: "VPN") == "VPN") {
            val intent = VpnService.prepare(this)
            if (intent == null) {
                startV2Ray()
            } else {
                requestVpnPermission.launch(intent)
            }
        } else {
            startV2Ray()
        }
    }

    private fun connectToOpenVpn() {
        if (Data.isStart) {
            confirmDisconnect()
        } else {
            prepareVpn()
        }
    }

//    private fun Int.dpToPx(): Int {
//        return (this * Resources.getSystem().displayMetrics.density).toInt()
//    }


    /**
     * openvpn fun
     */
    private fun initializeAll() {
        connection = CheckInternetConnection()

        // Create New Profile (OpenVpn)
//        val file = ENCRYPT_DATA.decrypt(Data.connectionStorage.getString("file", null))
//        val name = "Profile from remote App"
//        val profile: APIVpnProfile? = testoo?.addNewVPNProfile(name, false, file)
//        testoo?.startProfile(profile?.mUUID);

        // Checking is vpn already running or not (OpenVpn)
        isServiceRunning
        VpnStatus.initLogCache(this.cacheDir)
    }

    /**
     * Stop vpn
     *
     * @return boolean: VPN status
     */
    private fun stopVpn(): Boolean {
        try {
            OpenVPNThread.stop()
            setNewVpnState(0)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Show show disconnect confirm dialog
     */
    private fun confirmDisconnect() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("ایا میخواهید اتصال را قطع کنید ؟")
        builder.setPositiveButton(
            "قطع اتصال"
        ) { _, _ -> stopVpn() }

        builder.setNegativeButton(
            "لغو"
        ) { _, _ ->
            // User cancelled the dialog
        }

        // Create the AlertDialog
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Prepare for vpn connect with required permission
     */
    private fun prepareVpn() {
        if (!Data.isStart) {
            if (internetStatus) {
                // Checking permission for network monitor
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 1);
                } else startVpn() //have already permission

            } else {

                // No internet connection available
                showToast("شما به اینترنت متصل نیستید !!")
                handleErrorWhenConnect()
            }
        } else if (stopVpn()) {

            // VPN is stopped, show a Toast message.
            showToast("با موفقیت قطع شد")
        }
    }

    /**
     * Taking permission for network access
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {

            //Permission granted, start the VPN
            startVpn();
        } else {
            showToast("دسترسی رد شد !! ");
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Start the VPN
     */
    fun startVpn() {
        try {
            val file = Data.connectionStorage.getString("file", null)
//            val filePass = MainActivity.ENCRYPT_DATA.decrypt(
//                Data.connectionStorage.getString(
//                    "filePass",
//                    null
//                )
//            )
//            val fileUser = MainActivity.ENCRYPT_DATA.decrypt(
//                Data.connectionStorage.getString(
//                    "fileUser",
//                    null
//                )
//            )
//            val country = Data.connectionStorage.getString("country", "NULL")
//            if (file == null) {
//                Toast.makeText(this, "Null!", Toast.LENGTH_SHORT).show()
//            }
//            Toast.makeText(this, "Start!", Toast.LENGTH_SHORT).show()
//            Log.d("T", fileUser)
//            Log.d("T", filePass)
//            Log.d("T", (file)!!)
//            Log.d("G", (country)!!)

            // .ovpn file
//            val outPlocal = Data.connectionStorage.getString("fileLocal", "null");

//            val conf: InputStream? = outPlocal?.let { this.assets?.open(it) }
//            val isr = InputStreamReader(conf)
//            val br = BufferedReader(isr)
//            var config = ""
//            var line: String?
//
//            while (true) {
//                line = br.readLine()
//                if (line == null) break
//                config += "$line\n"
//            }
//
//            br.readLine()

//            Log.d("THIS is file", config)

            val uL = appValStorage.getString("usernameLogin", null);
            val uU = appValStorage.getString("usernamePassword", null);

            if (file != null) {
                City = Data.connectionStorage.getString("city", Data.NA)
                City?.let { Log.d("TAG NAME", it) }
                setNewVpnState(1)

                OpenVpnApi.startVpn(this, file, "Japan", uL, uU)

                // Update log
//            binding.tvMessageTopText.setText("Connecting...");
                Toast.makeText(this, "در حال اتصال ...", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "کانفیگی یافت نشد", Toast.LENGTH_SHORT).show()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    /**
     * Status change with corresponding vpn connection status
     *
     * @param connectionState
     */
    fun setStatus(connectionState: String?) {
        if (connectionState != null) {
            when (connectionState) {
                "DISCONNECTED" -> {
                    stopVpn()
                    setDefaultStatus()
                }

                "CONNECTED" -> {
                    setNewVpnState(2)
                    checkInformationUser()
                }

                "WAIT" -> {
                    setNewVpnState(1)
                }

                "AUTH" -> handleAUTH()

                "RECONNECTING" -> {
                    setNewVpnState(1)
                }

                "NONETWORK" -> handleErrorWhenConnect()
            }
        }
    }

    /**
     * Receive broadcast message
     */
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                setStatus(intent.getStringExtra("state"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                var duration = intent.getStringExtra("duration")
                var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
                var byteIn = intent.getStringExtra("byteIn")
                var byteOut = intent.getStringExtra("byteOut")
                if (duration == null) duration = "00:00:00"
                if (lastPacketReceive == null) lastPacketReceive = "0"
                if (byteIn == null) byteIn = " "
                if (byteOut == null) byteOut = " "
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)

//                final long Total = ins + outs;

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                // size
//                if (Total < 1000) {
//                    tv_data_text.setText("1KB");
//                    tv_data_name.setText("USED");
//                } else if ((Total >= 1000) && (Total <= 1000_000)) {
//                    tv_data_text.setText((Total / 1000) + "KB");
//                    tv_data_name.setText("USED");
//                } else {
//                    tv_data_text.setText((Total / 1000_000) + "MB");
//                    tv_data_name.setText("USED");
//                }
//            }
//        });
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Update status UI
     *
     * @param duration:          running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn:            incoming data
     * @param byteOut:           outgoing data
     */
    fun updateConnectionStatus(
        duration: String?,
        lastPacketReceive: String?,
        byteIn: String?,
        byteOut: String?
    ) {
//        binding.durationTv.setText("Duration: " + duration);
//        binding.lastPacketReceiveTv.setText("Packet Received: " + lastPacketReceive + " second ago");
//        binding.byteInTv.setText("Bytes In: " + byteIn);
//        binding.byteOutTv.setText("Bytes Out: " + byteOut);
    }

    /**
     * Show toast message
     *
     * @param message: toast message
     */
    fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Change server when user select new server
     * //     * @param server ovpn server details
     */
//    fun newServer() { // Server server
////        this.server = server;
////        updateCurrentServerIcon(server.getFlagUrl());
//
//        // Stop previous connection
//        if (Data.isStart) {
//            stopVpn()
//        }
//        prepareVpn()
//    }

    /**
     * v2ray
     */
    // v2ray
    private fun startV2Ray() {
        if (mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER).isNullOrEmpty()) {
            setNewVpnState(0)
            return
        }
        setNewVpnState(1)
        showCircle()
        V2RayServiceManager.startV2Ray(this)
        hideCircle()

        setNewVpnState(2)

    }

//    fun restartV2Ray() {
//        if (mainViewModel.isRunning.value == true) {
//            Utils.stopVService(this)
//        }
//        Observable.timer(500, TimeUnit.MILLISECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                startV2Ray()
//            }
//    }

    private fun showCircle() {
        // connection
        binding.fabProgressCircle.show()
    }

    private fun hideCircle() {
        try {
            Observable.timer(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    try {
                        if (binding.fabProgressCircle.isShown) {
                            binding.fabProgressCircle.hide()
                        }
                    } catch (e: Exception) {
                        Log.w(ANG_PACKAGE, e)
                    }
                }
        } catch (e: Exception) {
            Log.d(ANG_PACKAGE, e.toString())
        }
    }

    // save default v2ray config
    private fun initializeApp() {
        MmkvManager.removeAllServer()
        GetAllV2ray.setRetV2ray(
            this
        ) { retV2ray ->
            try {
                importBatchConfig(retV2ray)
                imageCountryV2ray = Data.connectionStorage.getString("imageV2ray", Data.NA)
                handleCountryImage()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun importBatchConfig(server: String?, subid: String = "") {
        val subid2 = subid.ifEmpty {
            mainViewModel.subscriptionId
        }
        val append = subid.isEmpty()

        Log.d("SUBID 2 ", subid2)

        var count = AngConfigManager.importBatchConfig(server, subid2, append)
        if (count <= 0) {
            count = AngConfigManager.importBatchConfig(Utils.decode(server!!), subid2, append)
        }
        if (count <= 0) {
            count = AngConfigManager.appendCustomConfigServer(server, subid2)
        }
        if (count > 0) {
            mainViewModel.reloadServerList()
        } else {
            showToast("داده ها ذخیره نشد !")
        }
    }

    private fun setTestState(content: String?) {
        binding.tvTestState.text = content
    }

    //    toggleIsLayoutTest
    private fun layoutTest() {
        if (mainViewModel.isRunning.value == true) {
            setTestState(getString(R.string.connection_test_testing))
            mainViewModel.testCurrentServerRealPing()
        } else {
            // handle error here
//                tv_test_state.text = getString(R.string.connection_test_fail)
        }
    }

    //    setup first
    private fun setupViewModel() {
        mainViewModel.updateTestResultAction.observe(this) { setTestState(it) }
        mainViewModel.isRunning.observe(this) { isRunning ->
            adapter.isRunning = isRunning
            if (isRunning) {
                if (!Utils.getDarkModeStatus(this)) {
//                    fab.setImageResource(R.drawable.ic_stat_name)
                }
//                fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_fab_orange))
                setNewVpnState(2);
                setTestState(getString(R.string.connection_connected))
                binding.layoutTest.isFocusable = true
            } else {
                if (!Utils.getDarkModeStatus(this)) {
//                    fab.setImageResource(R.drawable.ic_stat_name)
                }
//                fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_fab_grey))

                /**
                 * این مدل در پس زمینه و کمی دیر تر از بقیه اجرا میشوند و باعث میشود که همه چیز را ریست کند
                 * از مقدار ذخیره شده از قبل استفاده میکنم تا به مشکل نخورد
                 */
                if (Data.defaultItemDialog == 0) {
                    setNewVpnState(0)
                    setTestState(getString(R.string.connection_not_connected))
                    binding.layoutTest.isFocusable = false
                }

            }
            hideCircle()
        }
        mainViewModel.startListenBroadcast()
    }

    private fun copyAssets() {
        val extFolder = Utils.userAssetPath(this)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geo = arrayOf("geosite.dat", "geoip.dat")
                assets.list("")
                    ?.filter { geo.contains(it) }
                    ?.filter { !File(extFolder, it).exists() }
                    ?.forEach {
                        val target = File(extFolder, it)
                        assets.open(it).use { input ->
                            FileOutputStream(target).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Log.i(
                            ANG_PACKAGE,
                            "Copied from apk assets folder to ${target.absolutePath}"
                        )
                    }
            } catch (e: Exception) {
                Log.e(ANG_PACKAGE, "asset copy failed", e)
            }
        }
    }

    // drawer options
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.settings -> {
                // global settings and (usage)
                startActivity(Intent(this, UsageActivity::class.java))
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)
            }

            R.id.getUpdate -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("http://45.88.8.210:3008/update")
                    startActivity(intent)
                } catch (activityNotFound: ActivityNotFoundException) {

                    showToast("اپدیتی یافت نشد")

                } catch (_: Exception) {
                }
            }

            R.id.splitTun -> {
                startActivity(Intent(this, SplitActivity::class.java))
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)
            }

            R.id.info -> {
                startActivity(Intent(this, Info::class.java))
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)
            }

            R.id.logout -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)

                appValStorage.encode("isLoginBool", false)

                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.fade_in_1000, R.anim.fade_out_500)
                finish()
            }

            R.id.feedback -> {
                startActivity(Intent(this, ContactActivity::class.java))
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
            }
        }
//        binding.drawerLayout.closeDrawer(GravityCompat.START)
        /**
         * OpenVpn
         */
//        newServer()
//        changeServer.newServer(serverLists.get(index));
        return true
    }


    //     /**
//     * On navigation item click, close drawer and change server
//     *
//     * @param index: server index
//     */
//    override fun clickedItem(int index) {
//    }
    override fun onResume() {
        super.onResume()
        imageCountry = Data.connectionStorage.getString("image", Data.NA)
        City = Data.connectionStorage.getString("city", Data.NA)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
        restoreTodayTextTv()
    }

    /**
     * On navigation item click, close activity and change server
     *
     * @param index: server index
     */
    override fun newServer(server: OpenVpnServerList) {
//        changeServer.newServer(serverLists.get(index));
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)

        super.onPause()
    }

    companion object {
        @JvmField
        val ENCRYPT_DATA = EncryptData()
    }
}
