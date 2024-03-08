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
import com.tencent.mmkv.MMKV
import com.xray.lite.ui.MainSettingsV2ray
import com.xray.lite.ui.SettingsActivity
import sp.inetvpn.BuildConfig
import sp.inetvpn.Data.Data
import sp.inetvpn.MainApplication
import sp.inetvpn.R
import sp.inetvpn.databinding.ActivityUsageBinding
import sp.inetvpn.util.LogManager
import sp.inetvpn.util.MmkvManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * by Mehrab
 */
class UsageActivity : Activity() {
    private lateinit var binding: ActivityUsageBinding

    // mmkv
    private var prefUsageStorage: MMKV = MmkvManager.getDUStorage()
    private var settingsStorage: MMKV = MmkvManager.getSettingsStorage()

    // today
    private val today: Date = Calendar.getInstance().time
    private val df: SimpleDateFormat = SimpleDateFormat("dd-MMM-yyyy")
    private val todayFormat: String = df.format(today)

    // yesterday
    private val cal1: Calendar = Calendar.getInstance()
    private val yesterdayVal: String = df.format(Date(cal1.timeInMillis))

    // three days
    private val cal2: Calendar = Calendar.getInstance()
    private val threeDays: String = df.format(Date(cal2.timeInMillis))
    private val week = Calendar.getInstance()[Calendar.WEEK_OF_YEAR].toString()
    private val month = Calendar.getInstance()[Calendar.MONTH].toString()
    private val year = Calendar.getInstance()[Calendar.YEAR].toString()

    // today
    private val timeToday = prefUsageStorage.getLong(todayFormat + "_time", 0)
    private val timeYesterday = prefUsageStorage.getLong(yesterdayVal + "_time", 0)
    private val timeTotal = prefUsageStorage.getLong("total_time", 0)

    private var todayTime: String = ""
    private var yesterdayTime: String = ""
    private var totalTime: String = ""

    // connections
    private val connectionsToday = prefUsageStorage.getLong(todayFormat + "_connections", 0)
    private val connectionsYesterday = prefUsageStorage.getLong(yesterdayVal + "_connections", 0)
    private val connectionsTotal = prefUsageStorage.getLong("total_connections", 0)

    // get today total usage
    private val todayUsage = prefUsageStorage.getLong(todayFormat, 0)
    private val yesterdayUsage = prefUsageStorage.getLong(yesterdayVal, 0)
    private val dayThreeUsage = prefUsageStorage.getLong(threeDays, 0)
    private val weekUsage = prefUsageStorage.getLong(week + year, 0)
    private val monthUsage = prefUsageStorage.getLong(month + year, 0)

    private var isDeviceH: Boolean = "huawei".equals(Build.MANUFACTURER, ignoreCase = true)

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        cal1.add(Calendar.DATE, -1)
        cal2.add(Calendar.DATE, -2)

        todayTime = String.format(
            getString(R.string.string_of_two_number),
            timeToday / (1000 * 60 * 60) % 24
        ) + ":" + String.format(
            getString(R.string.string_of_two_number),
            TimeUnit.MILLISECONDS.toMinutes(timeToday) % 60
        ) + ":" + String.format(
            getString(R.string.string_of_two_number),
            TimeUnit.MILLISECONDS.toSeconds(timeToday) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(timeToday)
            )
        )
        yesterdayTime = String.format(
            getString(R.string.string_of_two_number),
            timeYesterday / (1000 * 60 * 60) % 24
        ) + ":" + String.format(
            getString(R.string.string_of_two_number),
            TimeUnit.MILLISECONDS.toMinutes(timeYesterday) % 60
        ) + ":" + String.format(
            getString(R.string.string_of_two_number),
            TimeUnit.MILLISECONDS.toSeconds(timeYesterday) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(timeYesterday)
            )
        )
        totalTime = String.format(
            getString(R.string.string_of_two_number),
            timeTotal / (1000 * 60 * 60) % 24
        ) + ":" + String.format(
            getString(R.string.string_of_two_number),
            TimeUnit.MILLISECONDS.toMinutes(timeTotal) % 60
        ) + ":" + String.format(
            getString(R.string.string_of_two_number),
            TimeUnit.MILLISECONDS.toSeconds(timeTotal) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(timeTotal)
            )
        )

        // bindings
        setupBindingUsage()
        // error prone
        showUsageCuTitle()
        checkDeviceFun()

        when (Data.defaultItemDialog) {
            1 -> {
                binding.openVpnC.isChecked = true
                binding.v2rayC.isChecked = false
            }

            0 -> {
                binding.openVpnC.isChecked = false
                binding.v2rayC.isChecked = true
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
            openAboutActivity()
        }
        binding.linearLayoutBattery.setOnClickListener {
            checkLinearLayoutBattery()
        }
        binding.linearLayoutContact.setOnClickListener {
            openContactActivity()
        }

        binding.switchUsageFastMode.setOnCheckedChangeListener { _, isChecked ->
            Data.settingsStorage.putBoolean("cancel_fast", isChecked)
            Data.cancelFast = isChecked
        }

        if (Data.cancelFast) {
            binding.switchUsageFastMode.setChecked(true)
        } else {
            binding.switchUsageFastMode.setChecked(false)
        }

        // on below line we are adding check
        // change listener for our radio group.
        binding.radioPortocolGroup.setOnCheckedChangeListener { _, checkedId ->
            // on below line we are getting radio button from our group.
            val radioButton = findViewById<RadioButton>(checkedId)

            if (radioButton.text == "OpenVpn") {
                Data.settingsStorage.putInt("default_connection_type", 1)
                Data.defaultItemDialog = 1
            } else if (radioButton.text == "V2ray") {
                Data.settingsStorage.putInt("default_connection_type", 0)
                Data.defaultItemDialog = 0
            }
            // on below line we are displaying a toast message.
            Toast.makeText(
                this@UsageActivity,
                "تنظیم شد : " + radioButton.text,
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.openSettingV2.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
//                startActivity(Intent(this, LogActivity::class.java))
//                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)

//        LinearLayout linearLayoutPrivacyPolicy = findViewById(R.id.linearLayoutPrivacyPolicy);
//        TextView tv_usage_privacy_title = findViewById(R.id.tv_usage_privacy_title);
//        TextView tv_usage_privacy_decription = findViewById(R.id.tv_usage_privacy_decription);
//        tv_usage_privacy_title.typeface = RobotoMedium)
//        tv_usage_privacy_decription.typeface = RobotoRegular)
//        linearLayoutPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
////                    Bundle params = new Bundle();
////                    params.putString("device_id", MainApplication.device_id);
////                    params.putString("click", "privacy");
////                    LogManager.logEvent("app_param_click", params);
//
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(Uri.parse("https://gayanvoice.github.io/oml/buzz/privacypolicy.html"));
//                    startActivity(intent);
//                } catch (Exception e) {
////                    Bundle params = new Bundle();
////                    params.putString("device_id", MainApplication.device_id);
////                    params.putString("exception", "UA17" + e);
////                    LogManager.logEvent(params);
//                }
//            }
//        });
//        val tv_usage_dark_mode_title = binding.tvUsageDarkModeTitle
//        val switch_usage_dark_mode = binding.switchUsageDarkMode
//        switch_usage_dark_mode.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
//            try {
//                if (isChecked) {
//                    try {
//                        settingsStorage.encode("dark_mode", "true")
//                    } catch (e: Exception) {
//                        settingsStorage.encode("dark_mode", "false")
//                    }
//                } else {
//                    settingsStorage.encode("dark_mode", "false")
//                }
//            } catch (e: Exception) {
//                val params = Bundle()
//                params.putString("device_id", MainApplication.device_id)
//                params.putString("exception", "UA20$e")
//                LogManager.logEvent(params)
//            }
//        }
//        tv_usage_dark_mode_title.typeface = Data.RobotoMedium
//        switch_usage_dark_mode.typeface = Data.RobotoRegular
//        val DarkMode = settingsStorage.getString("dark_mode", "false")
//        switch_usage_dark_mode.isChecked = DarkMode == "true"
    }

    private fun showUsageCuTitle() {
        var tvUsageCuTitle = Data.NA
        try {
            val deviceCreated = settingsStorage.getString("device_created", "null")
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
                        convertToFarsiNumber(minutes) + ' ' + Data.minute_ago
                    } else if (elapsedTime in 3600000..7199999) {
                        convertToFarsiNumber(hours) + ' ' + Data.hour_ago
                    } else if (elapsedTime in 7200000..86399999) {
                        convertToFarsiNumber(hours) + ' ' + Data.hours_ago
                    } else if (elapsedTime in 86400000..172799999) {
                        convertToFarsiNumber(days) + ' ' + Data.day_ago
                    } else if (elapsedTime >= 172800000) {
                        convertToFarsiNumber(days) + ' ' + Data.days_ago
                    } else if (elapsedTime >= 60000) {
                        convertToFarsiNumber(minutes) + ' ' + Data.minutes_ago
                    } else {
                        convertToFarsiNumber(seconds) + ' ' + Data.seconds_ago
                    }
                    tvUsageCuTitle = Data.device_time_txt + ' ' + timeString
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
        binding.tvUsageCuVersion.text = "${Data.Version_txt} ${BuildConfig.VERSION_CODE}"

        if (todayUsage < 1000) {
            binding.tvUsageDataTodaySize.text = Data.default_byte_txt
        } else if (todayUsage <= 1000000) {
            binding.tvUsageDataTodaySize.text = convertToFarsiNumber(todayUsage / 1000) + Data.KB
        } else {
            binding.tvUsageDataTodaySize.text = convertToFarsiNumber(todayUsage / 1000000) + Data.MB
        }

        if (yesterdayUsage == 0L) {
            binding.tvUsageDataYesterdaySize.text = Data.NA
        } else if (yesterdayUsage < 1000) {
            binding.tvUsageDataYesterdaySize.text = Data.default_byte_txt
        } else if (yesterdayUsage <= 1000000) {
            binding.tvUsageDataYesterdaySize.text = (yesterdayUsage / 1000).toString() + Data.KB
        } else {
            binding.tvUsageDataYesterdaySize.text = (yesterdayUsage / 1000000).toString() + Data.MB
        }

        binding.tvUsageDataDaythreeTitle.text = threeDays
        if (dayThreeUsage == 0L) {
            binding.tvUsageDataDaythreeSize.text = Data.NA
        } else if (dayThreeUsage < 1000) {
            binding.tvUsageDataDaythreeSize.text = Data.default_byte_txt
        } else if (dayThreeUsage <= 1000000) {
            binding.tvUsageDataDaythreeSize.text =
                convertToFarsiNumber(dayThreeUsage / 1000) + Data.KB
        } else {
            binding.tvUsageDataDaythreeSize.text =
                convertToFarsiNumber(dayThreeUsage / 1000000) + Data.MB
        }

        if (weekUsage == 0L) {
            binding.tvUsageDataThisweekSize.text = Data.NA
        } else if (weekUsage < 1000) {
            binding.tvUsageDataThisweekSize.text = Data.default_byte_txt
        } else if (weekUsage <= 1000000) {
            binding.tvUsageDataThisweekSize.text = convertToFarsiNumber(weekUsage / 1000) + Data.KB
        } else {
            binding.tvUsageDataThisweekSize.text =
                convertToFarsiNumber(weekUsage / 1000000) + Data.MB
        }


        if (monthUsage == 0L) {
            binding.tvUsageDataThismonthSize.text = Data.NA
        } else if (monthUsage < 1000) {
            binding.tvUsageDataThismonthSize.text = Data.default_byte_txt
        } else if (monthUsage <= 1000000) {
            binding.tvUsageDataThismonthSize.text =
                convertToFarsiNumber(monthUsage / 1000) + Data.KB
        } else {
            binding.tvUsageDataThismonthSize.text =
                convertToFarsiNumber(monthUsage / 1000000) + Data.MB
        }


        binding.tvUsageTimeTodayTime.text = todayTime

        binding.tvUsageTimeYesterdayTime.text = yesterdayTime

        binding.tvUsageTimeTotalTime.text = totalTime

        // connections
        binding.tvUsageConnectionTodaySize.text = connectionsToday.toString()

        binding.tvUsageConnectionYesterdaySize.text = connectionsYesterday.toString()

        binding.tvUsageConnectionTotalSize.text = connectionsTotal.toString()
    }

    private fun openContactActivity() {
        startActivity(Intent(this, FAQActivity::class.java))
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
    }

    private fun openAboutActivity() {
        try {
            finish()
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
        } catch (e: Exception) {
            val params = Bundle()
            params.putString("device_id", MainApplication.device_id)
            params.putString("exception", "UA2$e")
            LogManager.logEvent(params)
        }
    }

    private fun checkLinearLayoutBattery() {
        try {
            if (isDeviceH) {
                val builder = AlertDialog.Builder(this@UsageActivity)
                builder.setTitle(Data.default_usage_permissions_txt)
                    .setMessage(Data.default_usage_permissions_backg_txt)
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

    companion object {
        fun convertToFarsiNumber(number: Long): String {
            val numberFormat = NumberFormat.getNumberInstance(Locale("fa"))
            return numberFormat.format(number)
        }
    }
}
