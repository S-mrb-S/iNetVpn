package sp.inetvpn.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.xray.lite.ui.BaseActivity
import org.json.JSONException
import org.json.JSONObject
import sp.inetvpn.Data.GlobalData
import sp.inetvpn.MainApplication
import sp.inetvpn.R
import sp.inetvpn.databinding.ActivityLauncherBinding
import sp.inetvpn.handler.GetAllOpenVpn
import sp.inetvpn.util.Animations
import sp.inetvpn.util.CheckInternetConnection
import sp.inetvpn.util.LogManager

class LauncherActivity : BaseActivity() {
    private var binding: ActivityLauncherBinding? = null
    var FileDetails: String? = null
    var isLoginBool = false
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
            } catch (e: Exception) {
                val params = Bundle()
                params.putString("device_id", MainApplication.device_id)
                params.putString("exception", "WAA9$e")
                LogManager.logEvent(params)
            } finally {
                checkInternetLayer()
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
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
            appDetails
        } else {
            binding!!.animationLayout.tvStatus.text = GlobalData.disconnected
            threadCheckInternet()
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

    private val appDetails: Unit
        get() {
            binding!!.animationLayout.tvStatus.text = GlobalData.get_info_from_app
            GetAllOpenVpn.setRetOpenV(this@LauncherActivity) { content: String? ->
                if (content != null) {
                    try {
                        val jsonResponse = JSONObject(content)
                        val result = jsonResponse.getBoolean("result")
                        if (result) {
                            handleValidResult(content)
                        } else {
                            handleInvalidResult()
                        }
                    } catch (e: JSONException) {
                        handleException("اطلاعات به درستی تبدیل نشدن!")
                    }
                } else {
                    handleEmptyContent()
                }
            }
        }

    // Methods for handling different scenarios
    private fun handleValidResult(content: String) {
        FileDetails = content
        GlobalData.GetAllOpenVpnContent = content
        getFileDetails()
    }

    private fun handleInvalidResult() {
        binding!!.animationLayout.tvStatus.text = "اطلاعات صحیح نمی‌باشد.."
        checkInternetLayer()
    }

    private fun handleEmptyContent() {
        binding!!.animationLayout.tvStatus.text = "اطلاعات دریافت خالی می‌باشد!"
        checkInternetLayer()
    }

    private fun handleException(message: String) {
        binding!!.animationLayout.tvStatus.text = message
        checkInternetLayer()
    }

    private fun getFileDetails() {
        try {
            binding!!.animationLayout.tvStatus.text = GlobalData.get_details_from_file
        } finally {
            endThisActivityWithCheck()
        }
    }

    private fun endThisActivityWithCheck() {
        try {
            if (isLoginBool) {
                val Main = Intent(this@LauncherActivity, MainActivity::class.java)
                startActivity(Main)
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
            } else {
                val Welcome = Intent(this@LauncherActivity, LoginActivity::class.java)
                startActivity(Welcome)
                overridePendingTransition(R.anim.fade_in_1000, R.anim.fade_out_500)
            }
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
