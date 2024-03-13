package sp.inetvpn.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import de.blinkt.openvpn.core.App
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNService.setDefaultStatus
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import sp.inetvpn.BuildConfig
import sp.inetvpn.Data.GlobalData
import sp.inetvpn.Data.GlobalData.TODAY
import sp.inetvpn.Data.GlobalData.appValStorage
import sp.inetvpn.R
import sp.inetvpn.databinding.ActivityMainBinding
import sp.inetvpn.handler.CheckVipUser.checkInformationUser
import sp.inetvpn.handler.GetAllV2ray
import sp.inetvpn.handler.GetVersionApi
import sp.inetvpn.handler.SetupMain
import sp.inetvpn.util.Animations
import sp.inetvpn.util.CheckInternetConnection
import sp.inetvpn.util.CountryListManager
import sp.inetvpn.util.ManageDisableList
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * MehrabSp
 */
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding

    /**
     * openvpn state
     */
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
    private var imageCountry: String? =
        GlobalData.connectionStorage.getString("image", GlobalData.NA)
    private var city: String? =
        GlobalData.connectionStorage.getString("city", GlobalData.NA)

    private var vpnState: Int =
        0 // 0 --> ninja (no connect) \\ 1 --> loading (circle loading) (connecting) \\ 2 --> connected (wifi (green logo))

    private var footerState: Int =
        1 // 0 --> v2ray test layout \\ 1 --> main_today

    private var isSetupFirst: Boolean = true

    private var fadeIn1000: Animation? = null
    private var fadeOut1000: Animation? = null

    /**
     *
     */

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
    val adapter by lazy { MainRecyclerAdapter(MainAngActivity()) }
    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startV2Ray()
            }
        }

    // ViewModel (V2ray)
    private val mainViewModel: MainViewModel by viewModels()

    // Usage
    private val df: SimpleDateFormat
        get() = SimpleDateFormat("dd-MMM-yyyy")
    private var today: String = df.format(Calendar.getInstance().time)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkInformationUser(this)
        handlerSetupFirst()

        SetupMain.setupDrawer(this, binding)
        ManageDisableList.restoreList() // disable list
        initializeAll() // openvpn
        // save default config for v2ray
        initializeApp()

        setupViewModel()
        copyAssets()

        // Load default config type and save.
        GlobalData.defaultItemDialog =
            GlobalData.settingsStorage.getInt("default_connection_type", 0)
        GlobalData.cancelFast = GlobalData.settingsStorage.getBoolean("cancel_fast", false)

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
            if (!GlobalData.isStart) {
                setupMainDialog()
            } else {
                showToast("لطفا اول اتصال را قطع کنید")
            }
        }

        binding.linearLayoutMainHome.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.linearLayoutMainServers.setOnClickListener {
            if (GlobalData.defaultItemDialog == 0) {
                startAngActivity()
            } else {
                startServersActivity()
            }
        }

        binding.btnConnection.setOnClickListener {
            runOnUiThread {
                if (vpnState != 1) {
                    when (GlobalData.defaultItemDialog) {
                        1 -> connectToOpenVpn()
                        0 -> connectToV2ray()
                    }
                } else {
                    when (GlobalData.defaultItemDialog) {
                        1 -> stopVpn()
                        0 -> connectToV2ray()
                    }
                }
            }
        }

        binding.layoutTest.setOnClickListener {
            layoutTest()
        }
    }

    private fun setupMainDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(GlobalData.item_txt)
        builder.setSingleChoiceItems(
            GlobalData.item_options,
            GlobalData.defaultItemDialog
        ) { dialog: DialogInterface, which: Int ->  // which --> 0, 1
            GlobalData.settingsStorage.putInt("default_connection_type", which)
            Handler().postDelayed({ dialog.dismiss() }, 300)
            GlobalData.defaultItemDialog = which

            setNewFooterState(which)

        }
        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun restoreTodayTextTv() {
        val longUsageToday = GlobalData.prefUsageStorage.getLong(today, 0)
        if (longUsageToday < 1000) {
            binding.tvDataTodayText.text =
                "${GlobalData.default_ziro_txt} ${GlobalData.KB}"
        } else if (longUsageToday <= 1000000) {
            binding.tvDataTodayText.text = (longUsageToday / 1000).toString() + GlobalData.KB
        } else {
            binding.tvDataTodayText.text = (longUsageToday / 1000000).toString() + GlobalData.MB
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
        binding.tvMessageTopText.text = GlobalData.connected_catch_txt
        binding.tvMessageBottomText.text =
            GlobalData.connected_catch_check_internet_txt

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

            Animations.startAnimation(
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
            1 -> R.raw.loading_circle // connecting
            2 -> R.raw.connected_wifi // connected
            else -> R.raw.ninjainsecure // ??
        }
        binding.laAnimation.setAnimation(animationResource)

        when (vpnState) {
            0 -> {
                saveIsStart(false)
//                Data.isStart = false
                // disconnected
                binding.btnConnection.text = GlobalData.disconnected_btn
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

                binding.tvMessageTopText.text = GlobalData.disconnected_txt
                binding.tvMessageBottomText.text = GlobalData.disconnected_txt2
            }

            1 -> {
                // connecting
                binding.btnConnection.text = GlobalData.connecting_btn
                binding.btnConnection.background =
                    this@MainActivity.let {
                        ContextCompat.getDrawable(
                            it,
                            R.drawable.button_retry
                        )
                    }

                // scale
                binding.laAnimation.scaleX = 0.5f
                binding.laAnimation.scaleY = 0.5f

                // bubble

                when (GlobalData.defaultItemDialog) {
                    1 -> {
                        binding.tvMessageTopText.text = GlobalData.connecting_txt + ' ' + city
                    }

                    0 -> {
                        binding.tvMessageTopText.text = GlobalData.connecting_txt
                    }
                }

                binding.tvMessageBottomText.text = ""
            }

            2 -> {
                saveIsStart(true)
//                Data.isStart = true
                // connected
                binding.btnConnection.text = GlobalData.connected_btn
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
                when (GlobalData.defaultItemDialog) {
                    1 -> {
                        binding.tvMessageTopText.text = GlobalData.connected_txt + ' ' + city
                    }

                    0 -> {
                        binding.tvMessageTopText.text = GlobalData.connected_txt
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

    private fun saveIsStart(isStart: Boolean) {
        GlobalData.isStart = isStart
    }

    private fun handleNewFooterState() {
        if (!isSetupFirst) {
            // cancel all footer data here
            // ??
        }

        when (footerState) {
            0 -> {
                // layout test (v2ray)
                val handlerData = Handler()
                handlerData.postDelayed({
                    Animations.startAnimation(
                        this@MainActivity,
                        R.id.ll_main_layout_test,
                        R.anim.slide_up_800,
                        true
                    )
                }, 1000)
            }

            1 -> {
                val handlerData = Handler()
                handlerData.postDelayed({
                    Animations.startAnimation(
                        this@MainActivity,
                        R.id.ll_main_today,
                        R.anim.slide_up_800,
                        true
                    )
                }, 1000)
            }
        }

        handleCountryImage()
    }

    private fun showBubbleHomeAnimation() {
        if (isSetupFirst) {
            isSetupFirst = false

            fadeIn1000 = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in_1000)
            fadeOut1000 = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out_1000)
            binding.llTextBubble.animation = fadeIn1000

            val handlerToday = Handler()
            handlerToday.postDelayed({
                Animations.startAnimation(
                    this@MainActivity,
                    R.id.linearLayoutMainHome,
                    R.anim.anim_slide_down,
                    true
                )
                Animations.startAnimation(
                    this@MainActivity,
                    R.id.linearLayoutMainServers,
                    R.anim.anim_slide_down,
                    true
                )
            }, 1000)

        }
    }

    private fun setNewVpnState(newState: Int) {
        vpnState = newState

        handleNewVpnState()
    }

    private fun setNewFooterState(newState: Int) {
        footerState = newState

        handleNewFooterState()
    }

    private fun handleCountryImage() {
        if (GlobalData.defaultItemDialog == 0) {
            CountryListManager.OpenVpnSetServerList(
                "v2ray",
                binding.ivServers
            ) // v2ray
        } else {
            CountryListManager.OpenVpnSetServerList(imageCountry, binding.ivServers)
        }
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
        if (GlobalData.isStart) {
            confirmDisconnect()
        } else {
            prepareVpn()
        }
    }

    /**
     * openvpn fun
     */
    private fun initializeAll() {
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
        if (GlobalData.cancelFast) {
            stopVpn()
        } else {
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
    }

    /**
     * Prepare for vpn connect with required permission
     */
    private fun prepareVpn() {
        if (!GlobalData.isStart) {
            if (CheckInternetConnection.netCheck(this)) {
                // Checking permission for network monitor
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 1)
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
        when (requestCode) {
            33 -> {
                if (resultCode == Activity.RESULT_OK) {
                    // اطلاعاتی که از اکتیویتی دوم دریافت می‌کنید
                    val result = data?.getBooleanExtra("restart", false)
                    if (result == true) {
                        restartOpenVpnServer()
                    }
                    // انجام کار خاص با استفاده از callback
                }
            }

            else -> {
                if (resultCode == RESULT_OK) {

                    //Permission granted, start the VPN
                    startVpn()
                } else {
                    showToast("دسترسی رد شد !! ")
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Start the VPN
     */
    private fun startVpn() {
        GlobalData.prefUsageStorage

        val connectionToday = GlobalData.prefUsageStorage.getLong(TODAY + "_connections", 0)
        val connectionTotal = GlobalData.prefUsageStorage.getLong("total_connections", 0)

        GlobalData.prefUsageStorage.putLong(TODAY + "_connections", connectionToday + 1)
        GlobalData.prefUsageStorage.putLong("total_connections", connectionTotal + 1)

        try {
            val file = GlobalData.connectionStorage.getString("file", null)
            val uL = appValStorage.getString("usernameLogin", null)
            val uU = appValStorage.getString("usernamePassword", null)

            if (file != null) {
                city = GlobalData.connectionStorage.getString("city", GlobalData.NA)
                setNewVpnState(1)

                App.clearDisallowedPackageApplication()
                App.addArrayDisallowedPackageApplication(GlobalData.disableAppsList)

                OpenVpnApi.startVpn(this, file, "Japan", uL, uU)

                // Update log
                Toast.makeText(this, "در حال اتصال ...", Toast.LENGTH_SHORT).show()

            } else {
                startServersActivity()
                Toast.makeText(this, "ابتدا یک سرور را انتخاب کنید", Toast.LENGTH_SHORT).show()
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
                    checkInformationUser(this)
                }

                "WAIT" -> setNewVpnState(1)
                "AUTH" -> handleAUTH()
                "RECONNECTING" -> setNewVpnState(1)
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
    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Restart OpenVpn
     */
    private fun restartOpenVpnServer() {
        // Stop previous connection
        if (GlobalData.isStart) {
            stopVpn()
            // Delay for start
            Handler().postDelayed({ prepareVpn() }, 500)
        }
    }

    /**
     * v2ray
     */
    // v2ray
    private fun startV2Ray() {
        if (mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER).isNullOrEmpty()) {
            setNewVpnState(0)
            return
        }
        // Set loader for V2ray
        setNewVpnState(1)
        showCircle()
        // Start
        V2RayServiceManager.startV2Ray(this)
        // Hide loader
        hideCircle()
        setNewVpnState(2)
    }

    /**
     * Loading circle for V2ray
     */
    private fun showCircle() {
        // connection
        binding.fabProgressCircle.show()
    }

    //
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

    // save default v2ray config from api
    private fun initializeApp() {
        MmkvManager.removeAllServer()
        GetAllV2ray.setRetV2ray(
            this
        ) { retV2ray ->
            try {
                importBatchConfig(retV2ray)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * import config for v2ray
     */
    private fun importBatchConfig(server: String?, subid: String = "") {
        val subid2 = subid.ifEmpty {
            mainViewModel.subscriptionId
        }
        val append = subid.isEmpty()

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
            showToast("داده های سرور v2ray ذخیره نشد!")
        }
    }

    private fun setTestState(content: String?) {
        binding.tvTestState.text = content
    }

    // LayoutTest for V2ray
    private fun layoutTest() {
        if (mainViewModel.isRunning.value == true) {
            setTestState(getString(R.string.connection_test_testing))
            mainViewModel.testCurrentServerRealPing()
        } else {
            // handle error here
            setTestState(getString(R.string.connection_test_fail))
        }
    }

    // ViewModel for V2ray
    private fun setupViewModel() {
        mainViewModel.updateTestResultAction.observe(this) { setTestState(it) }
        mainViewModel.isRunning.observe(this) { isRunning ->
            adapter.isRunning = isRunning
            if (isRunning) {
                setNewVpnState(2)
                setTestState(getString(R.string.connection_connected))
                binding.layoutTest.isFocusable = true
            } else {
                /**
                 * این مدل در پس زمینه و کمی دیر تر از بقیه اجرا میشوند و باعث میشود که همه چیز را ریست کند
                 * از مقدار ذخیره شده از قبل استفاده میکنم تا به مشکل نخورد
                 */
                if (GlobalData.defaultItemDialog == 0) {
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
                    GetVersionApi.setRetVersion(
                        this
                    ) { retVersion ->
                        try {
                            if (retVersion != BuildConfig.VERSION_CODE) {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("https://panel.se2ven.sbs/api/update")
                                startActivity(intent)
                            } else {
                                showToast("برنامه شما به اخرین ورژن اپدیت هست!")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                } catch (activityNotFound: ActivityNotFoundException) {
                    showToast("اپدیتی یافت نشد")
                } catch (_: Exception) {
                }
            }

            R.id.splitTun -> {
                if (!GlobalData.isStart) {
                    startActivityForResult(Intent(this, SplitActivity::class.java), 33)
                    overridePendingTransition(
                        R.anim.anim_slide_in_left,
                        R.anim.anim_slide_out_right
                    )
                } else {
                    showToast("لطفا اول اتصال را قطع کنید")
                }
            }

            R.id.info -> {
                startActivity(Intent(this, InfoActivity::class.java))
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
                startActivity(Intent(this, FeedbackActivity::class.java))
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
            }
        }
//        binding.drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    /**
     * Resume main activity, Set new icon server..
     */
    override fun onResume() {
        super.onResume()

        imageCountry = GlobalData.connectionStorage.getString("image", GlobalData.NA)
        city = GlobalData.connectionStorage.getString("city", GlobalData.NA)

        handleCountryImage()

        // Set broadcast for OpenVpn
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))

        restoreTodayTextTv()
    }

    // Bug
    //    override fun onPause() {
    //        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    //        super.onPause()
    //    }

    private fun startServersActivity() {
        val servers = Intent(this@MainActivity, ServersActivity::class.java)
        startActivityForResult(servers, 33)
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
    }

    private fun startAngActivity() {
        startActivity(Intent(this@MainActivity, MainAngActivity::class.java))
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            moveTaskToBack(true)
            super.onBackPressed()
        }
    }

}
