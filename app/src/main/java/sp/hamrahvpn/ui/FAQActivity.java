package sp.hamrahvpn.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import sp.hamrahvpn.R;

/**
 * @MehraB832
 */
public class FAQActivity extends Activity {

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        LinearLayout ll_about_contact_close = findViewById(R.id.ll_contact_back);
        ll_about_contact_close.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        });

    }

}
