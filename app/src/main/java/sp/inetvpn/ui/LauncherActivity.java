package sp.inetvpn.ui;

import static sp.inetvpn.Data.Data.disconnected;
import static sp.inetvpn.Data.Data.get_details_from_file;
import static sp.inetvpn.Data.Data.get_info_from_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.xray.lite.ui.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import sp.inetvpn.Data.Data;
import sp.inetvpn.MainApplication;
import sp.inetvpn.R;
import sp.inetvpn.databinding.ActivityLauncherBinding;
import sp.inetvpn.handler.GetAllOpenVpn;
import sp.inetvpn.util.Animations;
import sp.inetvpn.util.CheckInternetConnection;
import sp.inetvpn.util.LogManager;

public class LauncherActivity extends BaseActivity {
    private ActivityLauncherBinding binding;
    String FileDetails;
    Boolean isLoginBool = false;
    private long backPressedTime;

    @Override
    protected void onResume() {
        super.onResume();

        try {
            Animations.startAnimation(LauncherActivity.this, R.id.animation_layout, R.anim.slide_up_800, true);

            new Handler(Looper.getMainLooper()).post(() -> {
                // کد انیمیشن و عملیات مربوطه اینجا قرار می‌گیرد
                Animations.startAnimation(this, R.id.ll_welcome_details, R.anim.slide_up_800, true);
            });

            try {
                isLoginBool = Data.appValStorage.decodeBool("isLoginBool", false);
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WAA9" + e);
                LogManager.logEvent(params);
            } finally {
                checkInternetLayer();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(null);
    }

    private void checkInternetLayer() {
        if (CheckInternetConnection.netCheck(this)) {
            getAppDetails();
        } else {
            binding.animationLayout.tvStatus.setText(disconnected);
            threadCheckInternet();
        }
    }

    // refresh ui
    private void threadCheckInternet() {
        new Thread() {
            boolean isShowText = true;
            boolean isThread = false;

            @Override
            public void run() {
                try {
                    while (!this.isInterrupted()) {

                        Thread.sleep(1000); // ui refresh
                        if (isThread) {
                            break;
                        }

                        runOnUiThread(() -> {
                            if (isShowText) {
                                isShowText = false;
                                binding.animationLayout.tvStatus.setText("هنگام اتصال به سرور به مشکل خوردیم.. لطفا از اتصال خود اطمینان حاصل کنید!");
                            }

                            if (CheckInternetConnection.netCheck(LauncherActivity.this)) {
                                checkInternetLayer();
                                isThread = true;
                            }
                        });

                    }
                } catch (InterruptedException e) {
                    isThread = true;
                    checkInternetLayer();
                }
            }
        }.start();
    }

    void getAppDetails() {
        binding.animationLayout.tvStatus.setText(get_info_from_app);

        GetAllOpenVpn.setRetOpenV(LauncherActivity.this, content -> {
            if (content != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(content);
                    boolean result = jsonResponse.getBoolean("result");

                    if (result) {
                        handleValidResult(content);
                    } else {
                        handleInvalidResult();
                    }
                } catch (JSONException e) {
                    handleException("اطلاعات به درستی تبدیل نشدن!");
                }
            } else {
                handleEmptyContent();
            }
        });
    }

    // Methods for handling different scenarios
    private void handleValidResult(String content) {
        FileDetails = content;
        Data.GetAllOpenVpnContent = content;
        getFileDetails();
    }

    private void handleInvalidResult() {
        binding.animationLayout.tvStatus.setText("اطلاعات صحیح نمی‌باشد..");
        checkInternetLayer();
    }

    private void handleEmptyContent() {
        binding.animationLayout.tvStatus.setText("اطلاعات دریافت خالی می‌باشد!");
        checkInternetLayer();
    }

    private void handleException(String message) {
        binding.animationLayout.tvStatus.setText(message);
        checkInternetLayer();
    }


    void getFileDetails() {
        try {
            binding.animationLayout.tvStatus.setText(get_details_from_file);
        } finally {
            endThisActivityWithCheck();
        }
    }

    private void endThisActivityWithCheck() {

        try {
            if (isLoginBool) {
                Intent Main = new Intent(LauncherActivity.this, MainActivity.class);
                startActivity(Main);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
            } else {
                Intent Welcome = new Intent(LauncherActivity.this, LoginActivity.class);
                startActivity(Welcome);
                overridePendingTransition(R.anim.fade_in_1000, R.anim.fade_out_500);
            }

        } finally {
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        // چک کردن آیا کاربر در بازه‌ای کمتر از 2 ثانیه دکمه برگشت را زده است
        if (currentTime - backPressedTime < 2000) {
            super.onBackPressed();
        } else {
            // نمایش پیام Toast
            Toast.makeText(this, "برای خروج دوباره دکمه برگشت را بزنید", Toast.LENGTH_SHORT).show();
            // ذخیره زمان فعلی
            backPressedTime = currentTime;
        }
    }
}
