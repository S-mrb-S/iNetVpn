package com.gold.hamrahvpn.ui;

import static com.gold.hamrahvpn.util.Data.appValStorage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.gold.hamrahvpn.R;
import com.gold.hamrahvpn.util.LogManager;
import com.google.android.material.textfield.TextInputEditText;

import com.gold.hamrahvpn.MainApplication;
import android.os.Handler;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText txtUsername, txtPassword;
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

        btn_welcome_later = findViewById(R.id.btn_welcome_later);

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
                checkText(inputText, "Username");
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
                checkText(inputText, "Password");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // اعمال محدودیت تعداد کلمات
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(50) {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                // شمارش تعداد کلمات
                int wordCount = countWords(dest.toString());

                // اگر تعداد کلمات بیشتر از حد مجاز است، ورودی را قبول نکن
                if (wordCount > 50) {
                    return "";
                }

                return null;
            }
        };

        txtUsername.setFilters(filters);
        txtPassword.setFilters(filters);

        btn_welcome_later.setOnClickListener(view -> {
            if (isTextForLogin) {
                saveAndFinish();
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

    private int countWords(String text) {
        String trimText = text.trim();
        if (trimText.isEmpty()) {
            return 0;
        } else {
            return trimText.split("\\s+").length; // شمارش تعداد کلمات با استفاده از فاصله ها
        }
    }

    private void checkText(String newInput, String typeInput) {
        String inputUserText = txtUsername.getText().toString();
        String inputPassText = txtPassword.getText().toString();
        if (newInput.isEmpty()) {
            setActionInputText(false);
        } else {
            Boolean inputBool = !inputPassText.isEmpty() && !inputUserText.isEmpty();
            setActionInputText(inputBool);
        }
    }

    private void saveAndFinish() {
        appValStorage.encode("isLoginBool", true); // no check
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
    }

    private void setActionInputText(Boolean isLogin) {
        Log.d("ACTION", isLogin.toString());
        if (isLogin) {
            btn_welcome_later.setBackgroundResource(R.drawable.round_input_active);
            isTextForLogin = true;
        } else {
            btn_welcome_later.setBackgroundResource(R.drawable.round_input_default);
            isTextForLogin = false;
        }
    }
}