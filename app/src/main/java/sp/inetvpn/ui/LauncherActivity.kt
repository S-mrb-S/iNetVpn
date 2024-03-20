package sp.inetvpn.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import sp.inetvpn.MainApplication
import sp.inetvpn.R
import sp.inetvpn.api.CheckLoginFromApi
import sp.inetvpn.api.GetAllServers
import sp.inetvpn.data.GlobalData
import sp.inetvpn.databinding.ActivityLauncherBinding
import sp.inetvpn.util.Animations
import sp.inetvpn.util.CheckInternetConnection
import sp.inetvpn.util.LogManager

class LauncherActivity : AppCompatActivity() {
    private var binding: ActivityLauncherBinding? = null
    private var isLoginBool = false
    private var username: String? = null
    private var password: String? = null
    private var backPressedTime: Long = 0

    override fun onResume() {
        super.onResume()
        try {
            Animations.startAnimation(
                this@LauncherActivity,
                R.id.animation_layout,
                R.anim.slide_up_800,
                true
            )
            Handler(Looper.getMainLooper()).post {
                // کد انیمیشن و عملیات مربوطه اینجا قرار می‌گیرد
                Animations.startAnimation(this, R.id.ll_welcome_details, R.anim.slide_up_800, true)
            }

            try {
                isLoginBool = GlobalData.appValStorage.decodeBool("isLoginBool", false)
                username = GlobalData.appValStorage.decodeString("UserName", null)
                password = GlobalData.appValStorage.decodeString("Password", null)
                if (username == null && password == null) {
                    isLoginBool = false
                }
            } catch (e: Exception) {
                val params = Bundle()
                params.putString("device_id", MainApplication.device_id)
                params.putString("exception", "WAA9$e")
                LogManager.logEvent(params)
            } finally {
                checkInternetLayer()
            }

        } catch (e: Exception) {
            Toast.makeText(this@LauncherActivity, "ERROR::", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
    }

    private fun checkInternetLayer() {
        if (CheckInternetConnection.netCheck(this)) {
            checkLayer2()
        } else {
            binding!!.animationLayout.tvStatus.text = GlobalData.disconnected
            threadCheckInternet()
        }
    }

    private fun checkLayer2() {
        if (isLoginBool) {
            Toast.makeText(this, "قبلا لاگین کردین. دریافت توکن و سرور ها", Toast.LENGTH_SHORT)
                .show()
            CheckLoginFromApi.checkIsLogin(
                this@LauncherActivity, username, password
            ) { isLogin: Boolean, _ ->
                if (isLogin) {
                    getServers()
                    Toast.makeText(this, "توکن جدید دریافت شد!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "توکن جدید دریافت نشد!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val welcome = Intent(this@LauncherActivity, LoginActivity::class.java)
            startActivity(welcome)
            overridePendingTransition(R.anim.fade_in_1000, R.anim.fade_out_500)
            finish()
        }
    }

    // refresh ui
    private fun threadCheckInternet() {
        object : Thread() {
            var isShowText = true
            var isThread = false
            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        sleep(1000) // ui refresh
                        if (isThread) {
                            break
                        }
                        runOnUiThread {
                            if (isShowText) {
                                isShowText = false
                                binding!!.animationLayout.tvStatus.text =
                                    "هنگام اتصال به سرور به مشکل خوردیم.. لطفا از اتصال خود اطمینان حاصل کنید!"
                            }
                            if (CheckInternetConnection.netCheck(this@LauncherActivity)) {
                                checkInternetLayer()
                                isThread = true
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    isThread = true
                    checkInternetLayer()
                }
            }
        }.start()
    }

    private fun getServers() {
        Log.d("CALL", "CALL SERVERS")
        binding!!.animationLayout.tvStatus.text = GlobalData.get_info_from_app
        GetAllServers.getAllServers(this@LauncherActivity) { isContent: Boolean?, message: String? ->
            if (isContent == true) {
                validResult()
            } else {
                if (message != null) {
                    handleException(message)
                } else {
                    handleException("اطلاعات به درستی تبدیل نشدن!")
                }
            }
        }
    }

    private fun handleException(message: String) {
        binding!!.animationLayout.tvStatus.text = message
        checkInternetLayer()
    }

    private fun validResult() {
        try {
            binding!!.animationLayout.tvStatus.text = GlobalData.get_details_from_file
        } finally {
            endThisActivityWithCheck()
        }
    }

    private fun endThisActivityWithCheck() {
        try {
                val main = Intent(this@LauncherActivity, MainActivity::class.java)
                startActivity(main)
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
        } finally {
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        // چک کردن آیا کاربر در بازه‌ای کمتر از 2 ثانیه دکمه برگشت را زده است
        if (currentTime - backPressedTime < 2000) {
            super.onBackPressed()
        } else {
            // نمایش پیام Toast
            Toast.makeText(this, "برای خروج دوباره دکمه برگشت را بزنید", Toast.LENGTH_SHORT).show()
            // ذخیره زمان فعلی
            backPressedTime = currentTime
        }
    }
}
