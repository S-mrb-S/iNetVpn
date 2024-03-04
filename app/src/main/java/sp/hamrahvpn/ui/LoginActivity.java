package sp.hamrahvpn.ui;

import static sp.hamrahvpn.util.Data.appValStorage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import sp.hamrahvpn.MainApplication;
import sp.hamrahvpn.R;
import sp.hamrahvpn.handler.CheckLoginFromApi;
import sp.hamrahvpn.util.LogManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText txtUsername, txtPassword;
    TextView statusIsLogin;
    Boolean isTextForLogin = false;
    Button btn_welcome_later;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // hide toolbar!
        setSupportActionBar(null);

        txtUsername = findViewById(R.id.inputUsername);
        txtPassword = findViewById(R.id.inputPassword);
        statusIsLogin = findViewById(R.id.statusIsLogin);

        btn_welcome_later = findViewById(R.id.btn_welcome_later);


//        final TextView textView = findViewById(R.id.textView);

//        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
//
//        final ValueAnimator animator = ValueAnimator.ofFloat(screenWidth - 20, -screenWidth -50);
//        animator.setDuration(10000); // مدت زمان حرکت به میلی‌ثانیه
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
//                float value = (float) valueAnimator.getAnimatedValue();
//                textView.setTranslationX(value);
//            }
//        });
//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                // وقتی انیمیشن به پایان رسید، متن را به سمت راست مجدداً ببرید و انیمیشن را شروع کنید
//                textView.setTranslationX(screenWidth);
//                animator.start();
//            }
//        });
//
//        animator.start();

//        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
//        final float initialX = screenWidth;
//
//        final ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "translationX", initialX, -screenWidth);
//        animator.setDuration(5000); // مدت زمان حرکت به میلی‌ثانیه
//        animator.setRepeatCount(ObjectAnimator.INFINITE); // برای چرخش بی‌پایان
//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                // هنگام پایان انیمیشن، تغییر متن را انجام دهید
//                super.onAnimationEnd(animation);
//                textView.setTranslationX(initialX);
//            }
//        });
//
//        animator.start();

        Handler handler = new Handler();

        handler.postDelayed(() -> startAnimation(LoginActivity.this, R.id.ll_main_layout_login, R.anim.slide_up_800, true), 500);

        txtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // هنگام تغییر مقدار
                String inputText = s.toString();
                checkText(inputText);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // هنگام تغییر مقدار
                String inputText = s.toString();
                checkText(inputText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                statusIsLogin.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.colorBubble));
            }
        });

//        // اعمال محدودیت تعداد کلمات
//        InputFilter[] filters = new InputFilter[1];
//        filters[0] = new InputFilter.LengthFilter(50) {
//            @Override
//            public CharSequence filter(CharSequence source, int start, int end,
//                                       Spanned dest, int dstart, int dend) {
//                // شمارش تعداد کلمات
//                int wordCount = countWords(dest.toString());
//
//                // اگر تعداد کلمات بیشتر از حد مجاز است، ورودی را قبول نکن
//                if (wordCount > 50) {
//                    return "";
//                }
//
//                return null;
//            }
//        };

//        txtUsername.setFilters(filters);
//        txtPassword.setFilters(filters);

        btn_welcome_later.setOnClickListener(view -> {
            if (isTextForLogin) {
                Handler handlerS = new Handler();
                handlerS.postDelayed(this::saveAndFinish, 1000);
            }
        });

    }

    public void startAnimation(Context ctx, int view, int animation, boolean show) {
    View element = findViewById(view);
    if (show) {
        element.setVisibility(View.VISIBLE);
    } else {
        element.setVisibility(View.INVISIBLE);
    }
    Animation anim = AnimationUtils.loadAnimation(ctx, animation);
    element.startAnimation(anim);
}

//    private int countWords(String text) {
//        String trimText = text.trim();
//        if (trimText.isEmpty()) {
//            return 0;
//        } else {
//            return trimText.split("\\s+").length; // شمارش تعداد کلمات با استفاده از فاصله ها
//        }
//    }

    private void checkText(String newInput) {
        String inputUserText = Objects.requireNonNull(txtUsername.getText()).toString();
        String inputPassText = Objects.requireNonNull(txtPassword.getText()).toString();
        if (newInput.isEmpty()) {
            setActionInputText(false);
        } else {
            Boolean inputBool = !inputPassText.isEmpty() && !inputUserText.isEmpty();
            setActionInputText(inputBool);
        }
    }

    private void saveAndFinish() {
        statusIsLogin.setText("");
        setActionInputText(false);
        String inputUserText = Objects.requireNonNull(txtUsername.getText()).toString();
        String inputPassText = Objects.requireNonNull(txtPassword.getText()).toString();

        CheckLoginFromApi.checkIsLogin(LoginActivity.this, inputUserText, inputPassText, (isLogin, message) -> {
            appValStorage.encode("isLoginBool", isLogin);

            if (isLogin){
                try {
                    Intent Main = new Intent(LoginActivity.this, MainActivity.class);
                    Main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(Main);
                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", MainApplication.device_id);
                    params.putString("exception", "MAA1" + e);
                    LogManager.logEvent(params);
                } finally {
                    finish();
                }
            }else{
                statusIsLogin.setText(message);
                statusIsLogin.setTextColor(ContextCompat.getColor(this, R.color.colorPingRed));
                setActionInputText(true);
//        Handler handler = new Handler();
//        handler.postDelayed(this::endThisActivityWithCheck, 100);
            }
        });
    }

    private void setActionInputText(Boolean isLogin) {
        if (isLogin) {
            btn_welcome_later.setBackgroundResource(R.drawable.round_input_active);
            isTextForLogin = true;
        } else {
            btn_welcome_later.setBackgroundResource(R.drawable.round_input_default);
            isTextForLogin = false;
        }
    }
}