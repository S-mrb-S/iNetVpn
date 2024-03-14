package sp.inetvpn.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import sp.inetvpn.R
import sp.inetvpn.databinding.ActivityAboutmeBinding

/**
 * MehrabSp
 */
class AboutMeActivity : Activity() {
    private var binding: ActivityAboutmeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutmeBinding.inflate(layoutInflater)
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
