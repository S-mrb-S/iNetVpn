package sp.inetvpn.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.xray.lite.ui.MainSettingsV2ray
import com.xray.lite.ui.SettingsActivity
import sp.inetvpn.BuildConfig
import sp.inetvpn.Data.GlobalData
import sp.inetvpn.MainApplication
import sp.inetvpn.R
import sp.inetvpn.databinding.ActivityUsageBinding
import sp.inetvpn.util.ConnectionManager
import sp.inetvpn.util.LogManager
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * by Mehrab
 */
class UsageActivity : Activity() {
    private lateinit var binding: ActivityUsageBinding

    private val connectionManager = ConnectionManager()

    private var isDeviceH: Boolean = "huawei".equals(Build.MANUFACTURER, ignoreCase = true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // bindings
        setupBindingUsage()
        // error prone
        showUsageCuTitle()
        checkDeviceFun()

        when (GlobalData.defaultItemDialog) {
            1 -> {
                binding.settingUsage.openVpnC.isChecked = true
                binding.settingUsage.v2rayC.isChecked = false
            }

            0 -> {
                binding.settingUsage.openVpnC.isChecked = false
                binding.settingUsage.v2rayC.isChecked = true
            }
        }

//                startActivity(Intent(this, LogActivity::class.java))
//                startActivity(Intent(this, ContactActivity::class.java))
//                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)

        binding.settingAngMain.setOnClickListener {
            startActivity(Intent(this, MainSettingsV2ray::class.java))
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
        }

        binding.headerLayout.llForward.setOnClickListener {
            this.onBackPressed()
        }
        binding.linearLayoutBattery.setOnClickListener {
            checkLinearLayoutBattery()
        }
        binding.linearLayoutAboutme.setOnClickListener {
            openAboutMeActivity()
        }

        binding.switchUsageFastMode.setOnCheckedChangeListener { _, isChecked ->
            GlobalData.settingsStorage.putBoolean("cancel_fast", isChecked)
            GlobalData.cancelFast = isChecked
        }

        if (GlobalData.cancelFast) {
            binding.switchUsageFastMode.setChecked(true)
        } else {
            binding.switchUsageFastMode.setChecked(false)
        }

        // on below line we are adding check
        // change listener for our radio group.
        binding.settingUsage.radioPortocolGroup.setOnCheckedChangeListener { _, checkedId ->
            // on below line we are getting radio button from our group.
            val radioButton = findViewById<RadioButton>(checkedId)

            if (radioButton.text == "OpenVpn") {
                GlobalData.settingsStorage.putInt("default_connection_type", 1)
                GlobalData.defaultItemDialog = 1
            } else if (radioButton.text == "V2ray") {
                GlobalData.settingsStorage.putInt("default_connection_type", 0)
                GlobalData.defaultItemDialog = 0
            }
            // on below line we are displaying a toast message.
            Toast.makeText(
                this@UsageActivity,
                "تنظیم شد : " + radioButton.text,
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.settingUsage.openSettingV2.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun showUsageCuTitle() {
        var tvUsageCuTitle = GlobalData.NA
        try {
            val deviceCreated = GlobalData.settingsStorage.getString("device_created", "null")
            if (deviceCreated != "null") {
                val deviceTime = deviceCreated!!.toLong()
                val nowTime = System.currentTimeMillis()
                val elapsedTime = nowTime - deviceTime
                if (nowTime > deviceTime) {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
                    val hours = TimeUnit.MILLISECONDS.toHours(elapsedTime)
                    val days = TimeUnit.MILLISECONDS.toDays(elapsedTime)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
                    val timeString: String = if (elapsedTime in 120000..3599999) {
                        convertToFarsiNumber(minutes) + ' ' + GlobalData.minute_ago
                    } else if (elapsedTime in 3600000..7199999) {
                        convertToFarsiNumber(hours) + ' ' + GlobalData.hour_ago
                    } else if (elapsedTime in 7200000..86399999) {
                        convertToFarsiNumber(hours) + ' ' + GlobalData.hours_ago
                    } else if (elapsedTime in 86400000..172799999) {
                        convertToFarsiNumber(days) + ' ' + GlobalData.day_ago
                    } else if (elapsedTime >= 172800000) {
                        convertToFarsiNumber(days) + ' ' + GlobalData.days_ago
                    } else if (elapsedTime >= 60000) {
                        convertToFarsiNumber(minutes) + ' ' + GlobalData.minutes_ago
                    } else {
                        convertToFarsiNumber(seconds) + ' ' + GlobalData.seconds_ago
                    }
                    tvUsageCuTitle = GlobalData.device_time_txt + ' ' + timeString
                }
            }
        } catch (e: Exception) {
            val params = Bundle()
            params.putString("device_id", MainApplication.device_id)
            params.putString("exception", "UA1$e")
            LogManager.logEvent(params)
        } finally {
            binding.tvUsageCuTitle.text = tvUsageCuTitle
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupBindingUsage() {
        binding.tvUsageCuVersion.text = "${GlobalData.Version_txt} ${BuildConfig.VERSION_NAME}"

        if (todayUsage!! < 1000) {
            binding.dataUsage.tvUsageDataTodaySize.text = GlobalData.default_byte_txt
        } else if (todayUsage!! <= 1000000) {
            binding.dataUsage.tvUsageDataTodaySize.text =
                convertToFarsiNumber(todayUsage!! / 1000) + GlobalData.KB
        } else {
            binding.dataUsage.tvUsageDataTodaySize.text =
                convertToFarsiNumber(todayUsage!! / 1000000) + GlobalData.MB
        }

        if (yesterdayUsage!! == 0L) {
            binding.dataUsage.tvUsageDataYesterdaySize.text = GlobalData.NA
        } else if (yesterdayUsage!! < 1000) {
            binding.dataUsage.tvUsageDataYesterdaySize.text = GlobalData.default_byte_txt
        } else if (yesterdayUsage!! <= 1000000) {
            binding.dataUsage.tvUsageDataYesterdaySize.text =
                (yesterdayUsage!! / 1000).toString() + GlobalData.KB
        } else {
            binding.dataUsage.tvUsageDataYesterdaySize.text =
                (yesterdayUsage!! / 1000000).toString() + GlobalData.MB
        }

        binding.dataUsage.tvUsageDataDaythreeTitle.text = threeDays
        if (dayThreeUsage!! == 0L) {
            binding.dataUsage.tvUsageDataDaythreeSize.text = GlobalData.NA
        } else if (dayThreeUsage!! < 1000) {
            binding.dataUsage.tvUsageDataDaythreeSize.text = GlobalData.default_byte_txt
        } else if (dayThreeUsage!! <= 1000000) {
            binding.dataUsage.tvUsageDataDaythreeSize.text =
                convertToFarsiNumber(dayThreeUsage!! / 1000) + GlobalData.KB
        } else {
            binding.dataUsage.tvUsageDataDaythreeSize.text =
                convertToFarsiNumber(dayThreeUsage!! / 1000000) + GlobalData.MB
        }

        if (weekUsage!! == 0L) {
            binding.dataUsage.tvUsageDataThisweekSize.text = GlobalData.NA
        } else if (weekUsage!! < 1000) {
            binding.dataUsage.tvUsageDataThisweekSize.text = GlobalData.default_byte_txt
        } else if (weekUsage!! <= 1000000) {
            binding.dataUsage.tvUsageDataThisweekSize.text =
                convertToFarsiNumber(weekUsage!! / 1000) + GlobalData.KB
        } else {
            binding.dataUsage.tvUsageDataThisweekSize.text =
                convertToFarsiNumber(weekUsage!! / 1000000) + GlobalData.MB
        }


        if (monthUsage!! == 0L) {
            binding.dataUsage.tvUsageDataThismonthSize.text = GlobalData.NA
        } else if (monthUsage!! < 1000) {
            binding.dataUsage.tvUsageDataThismonthSize.text = GlobalData.default_byte_txt
        } else if (monthUsage!! <= 1000000) {
            binding.dataUsage.tvUsageDataThismonthSize.text =
                convertToFarsiNumber(monthUsage!! / 1000) + GlobalData.KB
        } else {
            binding.dataUsage.tvUsageDataThismonthSize.text =
                convertToFarsiNumber(monthUsage!! / 1000000) + GlobalData.MB
        }

        binding.timeUsage.tvUsageTimeTodayTime.text = todayTime

        binding.timeUsage.tvUsageTimeYesterdayTime.text = yesterdayTime

        binding.timeUsage.tvUsageTimeTotalTime.text = totalTime

        // connections
        binding.connectionsUsage.tvUsageConnectionTodaySize.text =
            connectionManager.getConnectionCountForToday().toString()

        binding.connectionsUsage.tvUsageConnectionYesterdaySize.text =
            connectionManager.getConnectionCountForYesterday().toString()

        binding.connectionsUsage.tvUsageConnectionTotalSize.text =
            connectionManager.getTotalConnections().toString()
    }

    private fun openAboutMeActivity() {
        startActivity(Intent(this, AboutMeActivity::class.java))
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
    }

    private fun checkLinearLayoutBattery() {
        try {
            if (isDeviceH) {
                val builder = AlertDialog.Builder(this@UsageActivity)
                builder.setTitle(GlobalData.default_usage_permissions_txt)
                    .setMessage(GlobalData.default_usage_permissions_backg_txt)
                    .setPositiveButton("Allow") { _, _ ->
                        try {
                            val intent = Intent()
                            intent.setComponent(
                                ComponentName(
                                    "com.huawei.systemmanager",
                                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                                )
                            )
                            startActivity(intent)
                        } catch (e: Exception) {
                            val params = Bundle()
                            params.putString("device_id", MainApplication.device_id)
                            params.putString("exception", "UA15$e")
                            LogManager.logEvent(params)
                        }
                    }.create().show()
                val params = Bundle()
                params.putString("device_id", MainApplication.device_id)
                params.putString("click", "amazon")
                params.putString("exception", "app_param_click")
                LogManager.logEvent(params)
            }
        } catch (e: Exception) {
            val params = Bundle()
            params.putString("device_id", MainApplication.device_id)
            params.putString("exception", "UA16$e")
            LogManager.logEvent(params)
        }
    }

    private fun checkDeviceFun() {
        try {
            if (isDeviceH) {
                binding.linearLayoutBattery.visibility = View.VISIBLE
                binding.linearLineLayoutBattery.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            val params = Bundle()
            params.putString("device_id", MainApplication.device_id)
            params.putString("exception", "UA14$e")
            LogManager.logEvent(params)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
    }

    companion object {
        fun convertToFarsiNumber(number: Long): String {
            val numberFormat = NumberFormat.getNumberInstance(Locale("fa"))
            return numberFormat.format(number)
        }
    }
}
