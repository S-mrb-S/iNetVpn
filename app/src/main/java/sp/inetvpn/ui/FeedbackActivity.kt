package sp.inetvpn.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import sp.inetvpn.R
import sp.inetvpn.api.SendFeedback
import sp.inetvpn.databinding.ActivityFeedbackBinding
import sp.inetvpn.util.CheckInternetConnection

class FeedbackActivity : Activity() {
    private var binding: ActivityFeedbackBinding? = null

    var feedback: String? = null
    var more: String? = null
    var email: String? = null
    private var connecting: String? = null
    private var crashed: String? = null
    private var servers: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
        binding!!.btnAboutContactSubmit.setOnClickListener {
            // advertising
            if (CheckInternetConnection.netCheck(this)) {
                connecting = if (binding!!.checkboxAboutContactConnecting.isChecked) {
                    "true"
                } else {
                    "false"
                }

                // speed
                crashed = if (binding!!.checkboxAboutContactCrashed.isChecked) {
                    "true"
                } else {
                    "false"
                }

                // connecting
                servers = if (binding!!.checkboxAboutContactServers.isChecked) {
                    "true"
                } else {
                    "false"
                }
                val paramsFeed = Bundle()
                paramsFeed.putString("Connecting", connecting)
                paramsFeed.putString("Crashed", crashed)
                paramsFeed.putString("Servers", servers)
                feedback = paramsFeed.toString()
                more = binding!!.etAboutContactOtherProblems.getText().toString()
                email = binding!!.etAboutContactEmail.getText().toString()

                SendContactLog().start()
                binding!!.btnAboutContactSubmit.text = "ارسال شد"
                binding!!.btnAboutContactSubmit.setEnabled(false)
                onBackPressed()
            }
        }
        binding!!.headerLayout.llBack.setOnClickListener { onBackPressed() }
    }

    internal inner class SendContactLog : Thread() {
        override fun run() {
            SendFeedback.sendFeedBack(this@FeedbackActivity, feedback, more, email)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)
    }
}
