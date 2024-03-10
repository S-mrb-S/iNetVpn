package sp.hamrahvpn.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import sp.hamrahvpn.R
import sp.hamrahvpn.databinding.ActivityFaqBinding

/**
 * MehrabSp
 */
class FAQActivity : Activity() {
    private var binding: ActivityFaqBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        binding!!.headerLayout.llBack.setOnClickListener {
            this.onBackPressed()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)
    }
}
