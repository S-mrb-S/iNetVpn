package sp.hamrahvpn.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;

import sp.hamrahvpn.MainApplication;
import sp.hamrahvpn.R;
import sp.hamrahvpn.util.Animations;
import sp.hamrahvpn.util.LogManager;


public class ReviewActivity extends Activity {

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        LinearLayout ll_about_back = findViewById(R.id.ll_about_back);
        LottieAnimationView la_review = findViewById(R.id.la_review);
        Button btn_review_submit = findViewById(R.id.btn_review_submit);

        Animations.startAnimation(ReviewActivity.this, R.id.la_review, R.anim.slide_up_800, true);
        Animations.startAnimation(ReviewActivity.this, R.id.ll_about_back, R.anim.anim_slide_down, true);
        Animations.startAnimation(ReviewActivity.this, R.id.tv_review_title, R.anim.slide_up_800, true);
        Animations.startAnimation(ReviewActivity.this, R.id.tv_review_sup, R.anim.slide_up_800, true);

        Handler handler = new Handler();
        handler.postDelayed(() -> Animations.startAnimation(ReviewActivity.this, R.id.btn_review_submit, R.anim.slide_up_800, true), 500);

        handler = new Handler();
        handler.postDelayed(() -> Animations.startAnimation(ReviewActivity.this, R.id.tv_review_sub, R.anim.slide_up_800, true), 1000);

        ll_about_back.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        });

        la_review.setOnClickListener(view -> {
            SharedPreferences SharedAppDetails = getSharedPreferences("settings_data", 0);
            SharedPreferences.Editor Editor = SharedAppDetails.edit();
            Editor.putString("rate", "true");
            Editor.apply();

            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("click", "review-stars");
            params.putString("params", "app_param_click");
            LogManager.logEvent(params);

            finish();
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=sp.hamrahvpn"));
                startActivity(intent);
            } catch (ActivityNotFoundException activityNotFound) {
                params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "RA1" + activityNotFound.toString());
                LogManager.logEvent(params);

//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https:/")));
            } catch (Exception e) {
                params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "RA2" + e);
                LogManager.logEvent(params);
            }

        });

        btn_review_submit.setOnClickListener(view -> {
            SharedPreferences SharedAppDetails = getSharedPreferences("settings_data", 0);
            SharedPreferences.Editor Editor = SharedAppDetails.edit();
            Editor.putString("rate", "true");
            Editor.apply();

            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("click", "review-button");
            params.putString("params", "app_param_click");
            LogManager.logEvent(params);

            finish();
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=sp.hamrahvpn"));
                startActivity(intent);
            } catch (ActivityNotFoundException activityNotFound) {
                params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "RA2" + activityNotFound.toString());
                LogManager.logEvent(params);
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https:/")));
            } catch (Exception e) {
                params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "RA3" + e);
                LogManager.logEvent(params);
            }


        });
    }

}
