package com.gold.hamrahvpn.ui

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
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
import com.gold.hamrahvpn.MainApplication
import com.gold.hamrahvpn.R
import com.gold.hamrahvpn.databinding.ActivityMainBinding
import com.gold.hamrahvpn.util.CountryListManager
import com.gold.hamrahvpn.util.Data
import com.gold.hamrahvpn.util.EncryptData
import com.gold.hamrahvpn.util.LogManager
import com.google.android.material.navigation.NavigationView
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.mmkv.MMKV
import com.xray.lite.AppConfig
import com.xray.lite.AppConfig.ANG_PACKAGE
import com.xray.lite.InitV2rayModule
import com.xray.lite.extension.toast
import com.xray.lite.service.V2RayServiceManager
import com.xray.lite.ui.BaseActivity
import com.xray.lite.ui.MainAngActivity
import com.xray.lite.ui.adapters.MainRecyclerAdapter
import com.xray.lite.util.AngConfigManager
import com.xray.lite.util.MmkvManager
import com.xray.lite.util.Utils
import com.xray.lite.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private var inputStream: InputStream? = null
    private var bufferedReader: BufferedReader? = null
    var thread: Thread? = null

    // NEW
    private var fadeIn1000: Animation? = null
    private var fade_out_1000: Animation? = null
    var EnableConnectButton = false
    var progress: Int? = 0
    var ConnectionTimer: CountDownTimer? = null

    // v2ray
    private val adapter by lazy { MainRecyclerAdapter(MainAngActivity()) }
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
    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startV2Ray()
            }
        }

    private val mainViewModel: MainViewModel by viewModels()

    // new
    var hasFile = Data.ovpnContents.isNotEmpty()

    //    private var FileID: String? = "NULL"
//    private var File: String = ENCRYPT_DATA.decrypt(Data.connectionStorage.getString("file", Data.NA))
    private var City: String? = Data.connectionStorage.getString("city", Data.NA)
    private var Image: String? = Data.connectionStorage.getString("image", Data.NA)

    // ایجاد یک نمونه از MMKV با نام "connection_data"
    // بازیابی مقادیر از MMKV و رمزگشایی آنها
//        FileID = Data.connectionStorage.getString("file_id", Data.NA)
//        // بررسی وجود فایل
//        hasFile = FileID!!.isNotEmpty()

    private val df = SimpleDateFormat("dd-MMM-yyyy")
    private var today: String = df.format(Calendar.getInstance().time)

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (Data.isConnectionDetails) {
            restoreTodayTextTv()
        }
    }

    override fun onPause() {
        super.onPause()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        InitV2rayModule.ModuleInitVoid(this, MainApplication.application)
        setupDrawer()

        // save default config for v2ray
        initializeApp()
        setupViewModel()
        copyAssets()

        // Load default config type and save.
        defaultItemDialog = Data.settingsStorage.getInt("default_connection_type", 0)

        fadeIn1000 = AnimationUtils.loadAnimation(this, R.anim.fade_in_1000)
        fade_out_1000 = AnimationUtils.loadAnimation(this, R.anim.fade_out_1000)
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

        setupClickListener()

        // ui refresh
        thread = object : Thread() {
            var ShowData = true
            var ShowAnimation = true
            var isFistRun = true
            override fun run() {
                try {
                    while (!thread!!.isInterrupted) {
                        // important
                        if (isFistRun) {
                            sleep(300) // don't delete
                            isFistRun = false
                        } else {
                            sleep(2000) // don't delete
                        }
                        runOnUiThread {

                            // set country flag
                            if (MainApplication.abortConnection) {
                                MainApplication.abortConnection = false
                                if (MainApplication.connection_status != 2) {
                                    MainApplication.CountDown = 1
                                }
                                if (MainApplication.connection_status == 2) {
                                    try {
//                                        stop_vpn()
                                        try {
                                            ConnectionTimer!!.cancel()
                                        } catch (e: Exception) {
                                            val params = Bundle()
                                            params.putString("device_id", MainApplication.device_id)
                                            params.putString("exception", "MA7$e")
                                            LogManager.logEvent(params)
                                        }
                                        binding.ivProgressBar.visibility = View.INVISIBLE
                                        binding.tvMainCountDown.visibility = View.INVISIBLE
                                        val handlerToday12 = Handler()
                                        handlerToday12.postDelayed({
                                            startAnimation(
                                                this@MainActivity,
                                                R.id.ll_main_data,
                                                R.anim.slide_down_800,
                                                false
                                            )
                                            binding.llMainData.visibility = View.INVISIBLE
                                        }, 500)
                                        val handlerData = Handler()
                                        handlerData.postDelayed({
                                            startAnimation(
                                                this@MainActivity,
                                                R.id.ll_main_today,
                                                R.anim.slide_up_800,
                                                true
                                            )
                                        }, 1000)
                                        startAnimation(
                                            this@MainActivity,
                                            R.id.la_animation,
                                            R.anim.fade_in_1000,
                                            true
                                        )
                                        binding.laAnimation.cancelAnimation()
                                        binding.laAnimation.setAnimation(R.raw.ninjainsecure)
                                        binding.laAnimation.playAnimation()
                                        MainApplication.ShowDailyUsage = true
                                    } catch (e: Exception) {
                                        val params = Bundle()
                                        params.putString("device_id", MainApplication.device_id)
                                        params.putString("exception", "MA8$e")
                                        LogManager.logEvent(params)
                                    }
                                    MainApplication.isStart = false
                                }
                            }
                            CountryListManager.OpenVpnSetServerList(Image, binding.ivServers)

                            // set connection button
                            if (hasFile) {
                                if (MainApplication.connection_status == 0) {
                                    // disconnected
                                    binding.btnConnection.text = Data.disconnected_btn
                                    binding.btnConnection.background = ContextCompat.getDrawable(
                                        this@MainActivity,
                                        R.drawable.button_connect
                                    )
                                } else if (MainApplication.connection_status == 1) {
                                    // connecting
                                    if (EnableConnectButton) {
                                        binding.btnConnection.text = Data.connecting_cancel_btn
                                        binding.btnConnection.background =
                                            ContextCompat.getDrawable(
                                                this@MainActivity,
                                                R.drawable.button_retry
                                            )
                                    } else {
                                        binding.btnConnection.text = Data.connecting_btn
                                        binding.btnConnection.background =
                                            ContextCompat.getDrawable(
                                                this@MainActivity,
                                                R.drawable.button_retry
                                            )
                                    }
                                } else if (MainApplication.connection_status == 2) {
                                    // connected
                                    binding.btnConnection.text = Data.connected_btn
                                    binding.btnConnection.background = ContextCompat.getDrawable(
                                        this@MainActivity,
                                        R.drawable.button_disconnect
                                    )
                                } else if (MainApplication.connection_status == 3) {
                                    // connected
                                    binding.btnConnection.text = Data.connected_error_btn
                                    binding.btnConnection.background = ContextCompat.getDrawable(
                                        this@MainActivity,
                                        R.drawable.button_retry
                                    )
                                }
                            }

                            // set message text
//                            if (hasFile) {
//                                if (hasInternetConnection()) {
//                                    if (MainApplication.connection_status == 0) {
//                                        // disconnected
//                                        binding.tvMessageTopText.text = Data.disconnected_txt
//                                        binding.tvMessageBottomText.text = Data.disconnected_txt2
//                                    } else if (MainApplication.connection_status == 1) {
//                                        // connecting
//                                        binding.tvMessageTopText.text =
//                                            Data.connecting_txt + ' ' + City
//                                        binding.tvMessageBottomText.text
////                                        VpnStatus.getLastCleanLogMessage(
////                                            this@MainActivity
////                                        )
//
//                                    } else if (MainApplication.connection_status == 2) {
//                                        // connected
//                                        binding.tvMessageTopText.text =
//                                            Data.connected_txt + ' ' + City
//                                        binding.tvMessageBottomText.text = Data.StringCountDown
//                                    } else if (MainApplication.connection_status == 3) {
//                                        // connected
//                                        binding.tvMessageTopText.text =
//                                            Data.connected_error_danger_vpn_txt
//                                        binding.tvMessageBottomText.text =
//                                            Data.connected_error_long_txt
//                                    }
//                                } else {
//                                    binding.tvMessageTopText.text = Data.connected_catch_txt
//                                    binding.tvMessageBottomText.text =
//                                        Data.connected_catch_check_internet_txt
//                                }
//                            }

                            // show data limit
                            if (ShowData) {
                                ShowData = false
                                if (MainApplication.connection_status == 0) {
                                    val handlerData = Handler()
                                    handlerData.postDelayed({
                                        startAnimation(
                                            this@MainActivity,
                                            R.id.ll_main_today,
                                            R.anim.slide_up_800,
                                            true
                                        )
                                    }, 1000)
                                } else if (MainApplication.connection_status == 1) {
                                    val handlerData = Handler()
                                    handlerData.postDelayed({
                                        startAnimation(
                                            this@MainActivity,
                                            R.id.ll_main_today,
                                            R.anim.slide_up_800,
                                            true
                                        )
                                    }, 1000)
                                } else if (MainApplication.connection_status == 2) {
                                    val handlerData = Handler()
                                    handlerData.postDelayed({
                                        startAnimation(
                                            this@MainActivity,
                                            R.id.ll_main_data,
                                            R.anim.slide_up_800,
                                            true
                                        )
                                    }, 1000)
                                } else if (MainApplication.connection_status == 3) {
                                    // connected
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
                            }

                            // get daily usage
                            if (hasFile) {
                                if (MainApplication.connection_status == 0) {
                                    // disconnected
                                    if (MainApplication.ShowDailyUsage) {
                                        MainApplication.ShowDailyUsage = false
                                        // بازیابی مقدار مربوط به کلید "today"
                                        restoreTodayTextTv()
                                    }
                                }
                            }

                            // show animation
                            if (hasFile) {
                                if (ShowAnimation) {
                                    ShowAnimation = false
                                    if (MainApplication.connection_status == 0) {
                                        // disconnected
                                        startAnimation(
                                            this@MainActivity,
                                            R.id.la_animation,
                                            R.anim.fade_in_1000,
                                            true
                                        )
                                        binding.laAnimation.cancelAnimation()
                                        binding.laAnimation.setAnimation(R.raw.ninjainsecure)
                                        binding.laAnimation.playAnimation()
                                    } else if (MainApplication.connection_status == 1) {
                                        // connecting
                                        startAnimation(
                                            this@MainActivity,
                                            R.id.la_animation,
                                            R.anim.fade_in_1000,
                                            true
                                        )
                                        binding.laAnimation.cancelAnimation()
                                        binding.laAnimation.setAnimation(R.raw.conneting)
                                        binding.laAnimation.playAnimation()
                                    } else if (MainApplication.connection_status == 3) {
                                        // connected
                                        startAnimation(
                                            this@MainActivity,
                                            R.id.la_animation,
                                            R.anim.fade_in_1000,
                                            true
                                        )
                                        binding.laAnimation.cancelAnimation()
                                        binding.laAnimation.setAnimation(R.raw.ninjainsecure)
                                        binding.laAnimation.playAnimation()
                                    }
                                }
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    val params = Bundle()
                    params.putString("device_id", MainApplication.device_id)
                    params.putString("exception", "MA9$e")
                    LogManager.logEvent(params)
                }
            }
        }
        (thread as Thread).start()

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
            setupMainDialog()
        }
        binding.linearLayoutMainHome.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.linearLayoutMainServers.setOnClickListener {

            val servers: Intent = if (defaultItemDialog == 0) {
                Intent(this@MainActivity, MainAngActivity::class.java)
            } else {
                Intent(this@MainActivity, ServerActivity::class.java)
            }
            startActivity(servers)
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
        }

        binding.btnConnection.setOnClickListener {
            fabOnClick()
        }

        binding.laAnimation.setOnClickListener {
            fabOnClick()
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
            defaultItemDialog
        ) { dialog: DialogInterface, which: Int ->  // which --> 0, 1
            if (which == 0 || which == 1) {
                Handler().postDelayed({ dialog.dismiss() }, 300)
                val selectedOption = Data.item_options[which]
                Toast.makeText(
                    this@MainActivity,
                    "گزینه انتخاب شده: $selectedOption",
                    Toast.LENGTH_SHORT
                ).show()
                defaultItemDialog = which
                Data.settingsStorage.putInt("default_connection_type", which)
                toggleIsLayoutTest()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "کانفیگ مورد نظر یافت نشد!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun toggleIsLayoutTest() {
        startAnimation(
            this,
            R.id.ll_main_layout_test,
            if (defaultItemDialog == 1) R.anim.slide_down_800 else R.anim.slide_up_800,
            defaultItemDialog == 1
        )

        if (defaultItemDialog == 1) {
            // show openvpn layout
            binding.llMainData.visibility =
                if (binding.llMainLayoutTest.visibility == View.VISIBLE) {
                    binding.llMainLayoutTest.postDelayed({
                        startAnimation(
                            this,
                            R.id.ll_main_layout_test,
                            R.anim.slide_down_800,
                            false
                        )
                    }, 100)
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
        } else if (defaultItemDialog == 0) {
            // show layout test (v2ray)
            binding.llMainToday.visibility = if (binding.llMainToday.visibility == View.VISIBLE) {
                binding.llMainToday.postDelayed({
                    startAnimation(this, R.id.ll_main_today, R.anim.slide_down_800, false)
                    binding.llMainToday.visibility = View.INVISIBLE
                }, 100)
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

            binding.llMainData.visibility = if (binding.llMainData.visibility == View.VISIBLE) {
                binding.llMainData.postDelayed({
                    startAnimation(this, R.id.ll_main_data, R.anim.slide_down_800, false)
                    binding.llMainData.visibility = View.INVISIBLE
                }, 100)
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

            binding.llMainLayoutTest.visibility =
                if (binding.llMainLayoutTest.visibility == View.INVISIBLE) {
                    binding.llMainLayoutTest.postDelayed({
                        startAnimation(
                            this,
                            R.id.ll_main_layout_test,
                            R.anim.slide_up_800,
                            true
                        )
                    }, 50)
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
        }
    }

    private fun restoreTodayTextTv() {
        val long_usage_today = Data.prefUsageStorage.getLong(today, 0)
        if (long_usage_today < 1000) {
            binding.tvDataTodayText.text =
                Data.default_ziro_txt + ' ' + Data.KB
        } else if (long_usage_today <= 1000000) {
            binding.tvDataTodayText.text = (long_usage_today / 1000).toString() + Data.KB
        } else {
            binding.tvDataTodayText.text = (long_usage_today / 1000000).toString() + Data.MB
        }
    }

    private fun connectToV2ray() {
        if (mainViewModel.isRunning.value == true) {
            Utils.stopVService(this)
            mainAnimationState(0);
        } else if ((settingsStorage?.decodeString(AppConfig.PREF_MODE) ?: "VPN") == "VPN") {
            val intent = VpnService.prepare(this)
            if (intent == null) {
                startV2Ray()
                mainAnimationState(3)
            } else {
                requestVpnPermission.launch(intent)
            }
        } else {
            startV2Ray()
            mainAnimationState(3)
        }
    }

    private fun connectToOpenVpn() {
//        val r = Runnable {
//            if (!MainApplication.isStart) {
//                if (!hasFile) {
//                    val servers = Intent(this@MainActivity, ServerActivity::class.java)
//                    startActivity(servers)
//                    overridePendingTransition(
//                        R.anim.anim_slide_in_right,
//                        R.anim.anim_slide_out_left
//                    )
//                } else {
//                    if (hasInternetConnection()) {
//                        try {
//                            startVpn(Data.ovpnContents) // File, certificate
//
//                            val handlerToday1 = Handler()
//                            handlerToday1.postDelayed({
//                                startAnimation(
//                                    this@MainActivity,
//                                    R.id.ll_main_today,
//                                    R.anim.slide_down_800,
//                                    false
//                                )
//                            }, 500)
//                            val handlerData = Handler()
//                            handlerData.postDelayed({
//                                startAnimation(
//                                    this@MainActivity,
//                                    R.id.ll_main_data,
//                                    R.anim.slide_up_800,
//                                    true
//                                )
//                            }, 1000)
//                            startAnimation(
//                                this@MainActivity,
//                                R.id.la_animation,
//                                R.anim.fade_in_1000,
//                                true
//                            )
//                            binding.laAnimation.cancelAnimation()
//                            binding.laAnimation.setAnimation(R.raw.conneting)
//                            binding.laAnimation.playAnimation()
//                            binding.ivProgressBar.layoutParams.width = 10
//                            progress = 10
//                            startAnimation(
//                                this@MainActivity,
//                                R.id.iv_progress_bar,
//                                R.anim.fade_in_1000,
//                                true
//                            )
//                            binding.tvMainCountDown.visibility = View.VISIBLE
//                            MainApplication.CountDown = 30
//                            try {
//                                ConnectionTimer = object : CountDownTimer(32000, 1000) {
//                                    override fun onTick(millisUntilFinished: Long) {
//                                        MainApplication.CountDown = MainApplication.CountDown - 1
//                                        binding.ivProgressBar.layoutParams.width = progress as Int
//                                        progress =
//                                            progress!! + resources.getDimension(R.dimen.lo_10dpGrid)
//                                                .toInt()
//                                        binding.tvMainCountDown.text = MainApplication.CountDown.toString()
//                                        if (MainApplication.connection_status == 2) {
//                                            ConnectionTimer!!.cancel()
//                                            // ویرایش کردن مقدار "connection_time" در MMKV
//                                            Data.settingsStorage.putString(
//                                                "connection_time",
//                                                MainApplication.CountDown.toString()
//                                            )
//                                            // بررسی شرط
//                                            if (MainApplication.CountDown >= 20) {
//                                                // بازیابی مقدار "rate" از MMKV
//                                                val rate =
//                                                    Data.settingsStorage.getString("rate", "false")
//                                                // بررسی شرط
//                                                if (rate == "false") {
//                                                    // ایجاد یک Handler برای تاخیر
//                                                    val handler = Handler()
//                                                    handler.postDelayed({
//
//                                                        // ایجاد Intent برای رفتن به ReviewActivity
//                                                        val servers = Intent(
//                                                            this@MainActivity,
//                                                            ReviewActivity::class.java
//                                                        )
//                                                        startActivity(servers)
//                                                        overridePendingTransition(
//                                                            R.anim.anim_slide_in_right,
//                                                            R.anim.anim_slide_out_left
//                                                        )
//                                                    }, 1000)
//                                                }
//                                            }
//                                            startAnimation(
//                                                this@MainActivity,
//                                                R.id.tv_main_count_down,
//                                                R.anim.fade_out_1000,
//                                                false
//                                            )
//                                            startAnimation(
//                                                this@MainActivity,
//                                                R.id.iv_progress_bar,
//                                                R.anim.fade_out_1000,
//                                                false
//                                            )
//                                            startAnimation(
//                                                this@MainActivity,
//                                                R.id.la_animation,
//                                                R.anim.fade_out_1000,
//                                                false
//                                            )
//                                        }
//                                        if (MainApplication.CountDown <= 20) {
//                                            EnableConnectButton = true
//                                        }
//                                        if (MainApplication.CountDown <= 1) {
//                                            ConnectionTimer!!.cancel()
//                                            startAnimation(
//                                                this@MainActivity,
//                                                R.id.tv_main_count_down,
//                                                R.anim.fade_out_500,
//                                                false
//                                            )
//                                            startAnimation(
//                                                this@MainActivity,
//                                                R.id.iv_progress_bar,
//                                                R.anim.fade_out_500,
//                                                false
//                                            )
//                                            startAnimation(
//                                                this@MainActivity,
//                                                R.id.la_animation,
//                                                R.anim.fade_out_500,
//                                                false
//                                            )
//                                            try {
////                                                stop_vpn()
//                                                val handlerToday1 = Handler()
//                                                handlerToday1.postDelayed({
//                                                    startAnimation(
//                                                        this@MainActivity,
//                                                        R.id.ll_main_data,
//                                                        R.anim.slide_down_800,
//                                                        false
//                                                    )
//                                                }, 500)
//                                                val handlerData = Handler()
//                                                handlerData.postDelayed({
//                                                    startAnimation(
//                                                        this@MainActivity,
//                                                        R.id.ll_main_today,
//                                                        R.anim.slide_up_800,
//                                                        true
//                                                    )
//                                                }, 1000)
//                                                startAnimation(
//                                                    this@MainActivity,
//                                                    R.id.la_animation,
//                                                    R.anim.fade_in_1000,
//                                                    true
//                                                )
//                                                binding.laAnimation.cancelAnimation()
//                                                binding.laAnimation.setAnimation(R.raw.ninjainsecure)
//                                                binding.laAnimation.playAnimation()
//                                                MainApplication.ShowDailyUsage = true
//                                            } catch (e: Exception) {
//                                                val params = Bundle()
//                                                params.putString("device_id", MainApplication.device_id)
//                                                params.putString("exception", "MA3$e")
//                                                LogManager.logEvent(params)
//                                            }
//                                            MainApplication.isStart = false
//                                        }
//                                    }
//
//                                    override fun onFinish() {}
//                                }
//                            } catch (e: Exception) {
//                                val params = Bundle()
//                                params.putString("device_id", MainApplication.device_id)
//                                params.putString("exception", "MA4$e")
//                                LogManager.logEvent(params)
//                            }
//                            ConnectionTimer!!.start()
//                            EnableConnectButton = false
//                            MainApplication.isStart = true
//
//
//                        } catch (e: Exception) {
//                            val params = Bundle()
//                            params.putString("device_id", MainApplication.device_id)
//                            params.putString("exception", "MA5$e")
//                            LogManager.logEvent(params)
//                        }
//                    }
//                }
//            } else {
//                if (EnableConnectButton) {
//                    try {
////                        stop_vpn()
//                        try {
//                            ConnectionTimer!!.cancel()
//                        } catch (e: Exception) {
//                            val params = Bundle()
//                            params.putString("device_id", MainApplication.device_id)
//                            params.putString("exception", "MA6$e")
//                            LogManager.logEvent(params)
//                        }
//                        try {
//                            binding.ivProgressBar.visibility = View.INVISIBLE
//                            binding.tvMainCountDown.visibility = View.INVISIBLE
//                        } catch (e: Exception) {
//                            val params = Bundle()
//                            params.putString("device_id", MainApplication.device_id)
//                            params.putString("exception", "MA7$e")
//                            LogManager.logEvent(params)
//                        }
//                        val handlerToday1 = Handler()
//                        handlerToday1.postDelayed({
//                            startAnimation(
//                                this@MainActivity,
//                                R.id.ll_main_data,
//                                R.anim.slide_down_800,
//                                false
//                            )
//                            binding.llMainData.visibility = View.INVISIBLE
//                        }, 500)
//                        val handlerData = Handler()
//                        handlerData.postDelayed({
//                            startAnimation(
//                                this@MainActivity,
//                                R.id.ll_main_today,
//                                R.anim.slide_up_800,
//                                true
//                            )
//                        }, 1000)
//                        startAnimation(
//                            this@MainActivity,
//                            R.id.la_animation,
//                            R.anim.fade_in_1000,
//                            true
//                        )
//                        binding.laAnimation.cancelAnimation()
//                        binding.laAnimation.setAnimation(R.raw.ninjainsecure)
//                        binding.laAnimation.playAnimation()
//                        val ConnectionTime = Data.settingsStorage.getString("connection_time", "0")
//                        if (ConnectionTime!!.toLong() >= 20) {
//                            Data.settingsStorage.putString("connection_time", "0")
//                            val rate = Data.settingsStorage.getString("rate", "false")
//                            if (rate == "false") {
//                                val handler = Handler()
//                                handler.postDelayed({
//                                    val servers =
//                                        Intent(this@MainActivity, ReviewActivity::class.java)
//                                    startActivity(servers)
//                                    overridePendingTransition(
//                                        R.anim.anim_slide_in_right,
//                                        R.anim.anim_slide_out_left
//                                    )
//                                }, 500)
//                            }
//                        }
//                        MainApplication.ShowDailyUsage = true
//                    } catch (e: Exception) {
//                        val params = Bundle()
//                        params.putString("device_id", MainApplication.device_id)
//                        params.putString("exception", "MA6$e")
//                        LogManager.logEvent(params)
//                    }
//                    MainApplication.isStart = false
//                }
//            }
//        }
//        r.run()
    }

    private fun hasInternetConnection(): Boolean {
        var haveConnectedWifi = false
        var haveConnectedMobile = false
        try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.allNetworkInfo
            for (ni in netInfo) {
                if (ni.typeName.equals(
                        "WIFI",
                        ignoreCase = true
                    )
                ) if (ni.isConnected) haveConnectedWifi = true
                if (ni.typeName.equals(
                        "MOBILE",
                        ignoreCase = true
                    )
                ) if (ni.isConnected) haveConnectedMobile = true
            }
        } catch (e: Exception) {
            val params = Bundle()
            params.putString("device_id", MainApplication.device_id)
            params.putString("exception", "MA10$e")
            LogManager.logEvent(params)
        }
        return haveConnectedWifi || haveConnectedMobile
    }

//    override fun updateState(
//        state: String,
//        logmessage: String,
//        localizedResId: Int,
//        level: ConnectionStatus
//    ) {
//        runOnUiThread {
//            if (state == "CONNECTED") {
//                MainApplication.isStart = true
//                MainApplication.connection_status = 2
//                val handlerData = Handler()
//                handlerData.postDelayed({
//                    startAnimation(this@MainActivity, R.id.la_animation, R.anim.fade_in_1000, true)
//                    binding.laAnimation.cancelAnimation()
//                    binding.laAnimation.setAnimation(R.raw.ninjasecure)
//                    binding.laAnimation.playAnimation()
//                }, 1000)
//                EnableConnectButton = true
//            }
//        }
//    }

//    override fun updateByteCount(ins: Long, outs: Long, diffIns: Long, diffOuts: Long) {
//        val Total = ins + outs
//        runOnUiThread {
//            // size
//            if (Total < 1000) {
//                binding.tvDataText.text = Data.default_byte_txt
//                binding.tvDataName.text = Data.update_count_txt
//            } else if (Total <= 1000000) {
//                binding.tvDataText.text = (Total / 1000).toString() + Data.KB
//                binding.tvDataName.text = Data.update_count_txt
//            } else {
//                binding.tvDataText.text = (Total / 1000000).toString() + Data.MB
//                binding.tvDataName.text = Data.update_count_txt
//            }
//        }
//    }

    // change animation main state
    private fun mainAnimationState(status: Int) { // la_animation_connected
        toast("EJRA")
        startAnimation(
            this,
            R.id.la_animation,
            R.anim.fade_in_1000,
            true
        )
        binding.laAnimation.cancelAnimation()
        val animationResource = when (status) {
            0 -> R.raw.ninjainsecure // disconnected
            1 -> R.raw.conneting // connecting
            else -> R.raw.connected_wifi // connected
        }
        binding.laAnimation.setAnimation(animationResource)
        when (status) {
            0 -> {
                // تنظیم اندازه انیمیشن به 300dp
//                val newWidth = 200.dpToPx() // تبدیل dp به پیکسل
//                val newHeight = 200.dpToPx()
                binding.laAnimation.scaleX = 1f
                binding.laAnimation.scaleY = 1f
//                binding.laAnimation.requestLayout()
            } // disconnected
            1 -> {
                // تنظیم اندازه انیمیشن به 300dp
//                val newWidth = 250.dpToPx() // تبدیل dp به پیکسل
//                val newHeight = 250.dpToPx()
                binding.laAnimation.scaleX = 1.5f
                binding.laAnimation.scaleY = 1.5f
//                binding.laAnimation.requestLayout()
            }// connecting
            else -> {
                // تنظیم اندازه انیمیشن به 300dp
//                val newWidth = 300.dpToPx() // تبدیل dp به پیکسل
//                val newHeight = 300.dpToPx()
                binding.laAnimation.scaleX = 2.5f
                binding.laAnimation.scaleY = 2.5f
//                binding.laAnimation.requestLayout()
            } // connected
        }
        binding.laAnimation.playAnimation()
    }

//    private fun Int.dpToPx(): Int {
//        return (this * Resources.getSystem().displayMetrics.density).toInt()
//    }

    // v2ray
    private fun startV2Ray() {
        if (mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER).isNullOrEmpty()) {
            return
        }
        showCircle()
        toast(R.string.toast_services_start)
        V2RayServiceManager.startV2Ray(this)
        hideCircle()
    }

    fun restartV2Ray() {
        if (mainViewModel.isRunning.value == true) {
            Utils.stopVService(this)
        }
        Observable.timer(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startV2Ray()
            }
    }

    private fun showCircle() {
        // connection
//        mainAnimationState(1)
        binding.fabProgressCircle.show()
    }

    private fun hideCircle() {
        try {
            Observable.timer(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    try {
//                        mainAnimationState(3);
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

    private fun initializeApp() {
        // اگر اولین بار استفاده می‌شود، اجرای کد مربوط به نصب برنامه
        if (isFirstRun()) {

            val testOne =
                "ss://Y2hhY2hhMjAtaWV0Zi1wb2x5MTMwNToycWdqck1WVmJ3WWROZDNGdHZ0dHNM@51.142.158.58:63072#180_+@%F0%9D%97%94%F0%9D%97%A5%F0%9D%97%9A%F0%9D%97%A2%F0%9D%97%A7%F0%9D%97%94%F0%9D%97%AD\n" +
                        "vless://e1a928be-e289-4aef-b92e-4106fabdf42f@198.41.202.5:80?security=&type=ws&path=/?ed%3D2048&host=mahi.kosnanatfilterchi.eu.org&encryption=none#%F0%9F%87%BA%F0%9F%87%B8+@FreakConfig\n" +
                        "vless://9bcf1249-836e-4fcf-a28b-355dc959e324@104.18.14.229:2087?security=tls&sni=mtn.0bbg.cfd&fp=firefox&type=grpc&serviceName=zula.ir&encryption=none#%F0%9F%87%A9%F0%9F%87%AA+@FreakConfig\n" +
                        "vless://8dcae9dc-7ec3-4065-8d24-18eda4d7c7b5@159.69.198.253:2087?security=tls&sni=de7.korda.top&fp=chrome&type=grpc&serviceName=de7.korda.top&encryption=none#%F0%9F%87%A9%F0%9F%87%AA+@FreakConfig\n" +
                        "vless://e1a928be-e289-4aef-b92e-4106fabdf42f@172.67.204.84:80?security=&type=ws&path=/?ed%3D2048&host=mahi.kosnanatfilterchi.eu.org&encryption=none#%F0%9F%87%A6%F0%9F%87%B7+@FreakConfig"

            try {
                importBatchConfig(testOne)
                // ذخیره اطلاعات یکبار برای اجراهای بعدی
                saveFirstRunFlag()
//                toast("Welcome!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fabOnClick() {
        when (defaultItemDialog) {
            1 -> connectToOpenVpn()
            0 -> connectToV2ray()
            else -> Toast.makeText(
                this@MainActivity,
                "کانفیگ مورد نظر یافت نشد!",
                Toast.LENGTH_SHORT
            ).show()
        }


    }

    private fun importBatchConfig(server: String?, subid: String = "") {
        val subid2 = if (subid.isNullOrEmpty()) {
            mainViewModel.subscriptionId
        } else {
            subid
        }
        val append = subid.isNullOrEmpty()

        var count = AngConfigManager.importBatchConfig(server, subid2, append)
        if (count <= 0) {
            count = AngConfigManager.importBatchConfig(Utils.decode(server!!), subid2, append)
        }
        if (count <= 0) {
            count = AngConfigManager.appendCustomConfigServer(server, subid2)
        }
        if (count > 0) {
//            toast(R.string.toast_success)
            mainViewModel.reloadServerList()
        } else {
//            toast(R.string.toast_failure)
        }
    }

    private fun isFirstRun(): Boolean {
        val mmkv = MMKV.defaultMMKV()
        return !mmkv.decodeBool("isFirstRun", false)
    }

    private fun saveFirstRunFlag() {
        val mmkv = MMKV.defaultMMKV()
        mmkv.encode("isFirstRun", true)
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
                setTestState(getString(R.string.connection_connected))
                binding.layoutTest.isFocusable = true
            } else {
                if (!Utils.getDarkModeStatus(this)) {
//                    fab.setImageResource(R.drawable.ic_stat_name)
                }
//                fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_fab_grey))
                setTestState(getString(R.string.connection_not_connected))
                binding.layoutTest.isFocusable = false
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

            R.id.splitTun -> {
                Toast.makeText(this@MainActivity, "بزودی ...", Toast.LENGTH_SHORT).show()
            }

            R.id.logout -> {
                Toast.makeText(this@MainActivity, "بزودی ...", Toast.LENGTH_SHORT).show()
            }

            R.id.feedback -> {
                Toast.makeText(this@MainActivity, "بزودی ...", Toast.LENGTH_SHORT).show()
            }

            R.id.aboutMe -> {
                startActivity(Intent(this, ContactActivity::class.java))
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
            }
        }
//        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    //
    fun startAnimation(ctx: Context?, view: Int, animation: Int, show: Boolean) {
        val Element = findViewById<View>(view)
        if (show) {
            Element.visibility = View.VISIBLE
        } else {
            Element.visibility = View.INVISIBLE
        }
        val anim = AnimationUtils.loadAnimation(ctx, animation)
        Element.startAnimation(anim)
    }

    companion object {
        @JvmField
        var defaultItemDialog = 0 // 0 --> V2ray, 1 --> OpenVpn

        @JvmField
        val ENCRYPT_DATA = EncryptData()
    }
}
