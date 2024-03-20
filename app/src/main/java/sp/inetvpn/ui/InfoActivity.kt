package sp.inetvpn.ui

import android.os.Bundle
import android.view.View
import com.xray.lite.ui.BaseActivity
import sp.inetvpn.R
import sp.inetvpn.data.GlobalData
import sp.inetvpn.databinding.ActivityInfoBinding

class InfoActivity : BaseActivity() {
    private var binding: ActivityInfoBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        val basicInfo = GlobalData.appValStorage.getString("basic_info", "خالی")
        val firstConnection = GlobalData.appValStorage.getString("first_connection", "خالی")
        val expiration = GlobalData.appValStorage.getString("expiration", "خالی")
        val userId = GlobalData.appValStorage.decodeString("UserName", "خالی")
        val days = GlobalData.appValStorage.getInt("days", 0)

        binding!!.userId.text = userId
        binding!!.basicInfo.text = basicInfo

//        text_for_c --< v
//        layout_info_c --> gone
//        if (days.toDouble() != 1.1 || expiration != null) {
            binding!!.textForC.visibility = View.GONE
            binding!!.layoutInfoC.visibility = View.VISIBLE
            binding!!.firstLogin.text = firstConnection
            binding!!.nearestExpDate.text = expiration
            binding!!.days.text = days.toString()
//        }
        binding!!.llContactBack.setOnClickListener {
            this.onBackPressed()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)
    }
}