package sp.inetvpn.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.navigation.NavigationView
import com.tencent.mmkv.MMKV
import com.xray.lite.AppConfig
import com.xray.lite.AppConfig.ANG_PACKAGE
import com.xray.lite.service.V2RayServiceManager
import com.xray.lite.ui.BaseActivity
import com.xray.lite.ui.MainAngActivity
import com.xray.lite.ui.adapters.MainRecyclerAdapter
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
import sp.inetvpn.BuildConfig
import sp.inetvpn.R
import sp.inetvpn.data.GlobalData
import sp.inetvpn.data.GlobalData.appValStorage
import sp.inetvpn.databinding.ActivityMainBinding
import sp.inetvpn.handler.GetVersionApi
import sp.inetvpn.state.MainActivity.vpnState
import sp.inetvpn.util.CheckInternetConnection
import sp.inetvpn.util.ManageDisableList
import sp.inetvpn.util.UsageConnectionManager
import java.io.File
import java.io.FileOutputStream

/**
 * MehrabSp
 */
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding

    private var setup: sp.inetvpn.setup.MainActivity? = null
    private var state: sp.inetvpn.state.MainActivity? = null

    private val usageConnectionManager = UsageConnectionManager()

    /**
     * openvpn service
     */
    private val isServiceRunning: Unit
        /**
         * Get service status
         */
        get() {
            setStatus(OpenVPNService.getStatus())
        }

    /**
     * v2ray storage
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

    /**
     * enable connection button
     */
    private var enableButtonC: Boolean = true
    // ViewModel (V2ray)
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setup = sp.inetvpn.setup.MainActivity(this, binding, mainViewModel)
        state = sp.inetvpn.state.MainActivity(this, binding, setup)

        state?.handlerSetupFirst()
        setup?.setupAll()

        ManageDisableList.restoreList() // disable list
        initializeAll() // openvpn

        setupViewModel()
        copyAssets()

        // Load default config type and save.
        GlobalData.defaultItemDialog =
            GlobalData.settingsStorage.getInt("default_connection_type", 0)
        GlobalData.cancelFast = GlobalData.settingsStorage.getBoolean("cancel_fast", false)

        setupClickListener()
    }

    private fun setupClickListener() {
        binding.llProtocolMain.setOnClickListener {
                setupMainDialog()
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
            handleButtonConnect()
        }

        binding.layoutTest.setOnClickListener {
            setup?.layoutTest()
        }
    }

    private fun handleButtonConnect() {
        if (enableButtonC) {
            enableButtonC = false
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
            enableButtonC = true
        } else showToast("لطفا کمی صبر کنید..")
    }

    /**
     * set config dialog
     */
    private fun setupMainDialog() {
        if (!GlobalData.isStart) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(GlobalData.item_txt)
            builder.setSingleChoiceItems(
                GlobalData.item_options,
                GlobalData.defaultItemDialog
            ) { dialog: DialogInterface, which: Int ->  // which --> 0, 1
                GlobalData.settingsStorage.putInt("default_connection_type", which)
                Handler().postDelayed({ dialog.dismiss() }, 300)
                GlobalData.defaultItemDialog = which

                state?.setNewFooterState(which)
            }
            val dialog = builder.create()
            dialog.show()
        } else {
            showToast("لطفا اول اتصال را قطع کنید")
        }
    }

    /*
     */

    private fun connectToV2ray() {
        if (mainViewModel.isRunning.value == true) {
            Utils.stopVService(this)
            state?.setNewVpnState(0)
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
            state?.setNewVpnState(0)
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
                state?.handleErrorWhenConnect()
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
        usageConnectionManager.establishConnection()

        try {
            val file = GlobalData.connectionStorage.getString("file", null)
            val uL = appValStorage.getString("usernameLogin", null)
            val uU = appValStorage.getString("usernamePassword", null)

            if (file != null) {
                setup?.setNewImage()
                state?.setNewVpnState(1)

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
                    state?.setNewVpnState(2)
//                    checkInformationUser(this)
                }

                "WAIT" -> state?.setNewVpnState(1)
                "AUTH" -> state?.handleAUTH()
                "RECONNECTING" -> state?.setNewVpnState(1)
                "NONETWORK" -> state?.handleErrorWhenConnect()
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
            state?.setNewVpnState(0)
            return
        }
        // Set loader for V2ray
        state?.setNewVpnState(1)
        setup?.showCircle()
        // Start
        V2RayServiceManager.startV2Ray(this)
        usageConnectionManager.establishConnection()
        // Hide loader
        setup?.hideCircle()
        state?.setNewVpnState(2)
    }

    // ViewModel for V2ray
    private fun setupViewModel() {
        mainViewModel.updateTestResultAction.observe(this) { setup?.setTestState(it) }
        mainViewModel.isRunning.observe(this) { isRunning ->
            adapter.isRunning = isRunning
            if (isRunning) {
                state?.setNewVpnState(2)
                setup?.setTestState(getString(R.string.connection_connected))
                binding.layoutTest.isFocusable = true
            } else {
                /**
                 * این مدل در پس زمینه و کمی دیر تر از بقیه اجرا میشوند و باعث میشود که همه چیز را ریست کند
                 * از مقدار ذخیره شده از قبل استفاده میکنم تا به مشکل نخورد
                 */
                if (GlobalData.defaultItemDialog == 0) {
                    state?.setNewVpnState(0)
                    setup?.setTestState(getString(R.string.connection_not_connected))
                    binding.layoutTest.isFocusable = false
                }
            }
            setup?.hideCircle()
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

    fun setStateFromOtherClass(newState: Int) {
        state?.setNewVpnState(newState)
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

        setup?.setNewImage()
        setup?.handleCountryImage()

        // Set broadcast for OpenVpn
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))

        state?.restoreTodayTextTv()
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
