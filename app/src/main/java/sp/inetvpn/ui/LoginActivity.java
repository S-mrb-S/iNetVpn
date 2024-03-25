package sp.inetvpn.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import sp.inetvpn.MainApplication;
import sp.inetvpn.R;
import sp.inetvpn.api.CheckLoginFromApi;
import sp.inetvpn.api.GetAllServers;
import sp.inetvpn.util.Animations;
import sp.inetvpn.util.LogManager;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText txtUsername, txtPassword;
    TextView statusIsLogin;
    Boolean isTextForLogin = false;
    Button btn_welcome_later;
    boolean isStart = false;

    @Override
    protected void onResume() {
        super.onResume();

        if (!isStart) {
            Handler handler = new Handler();
            handler.postDelayed(() -> Animations.startAnimation(LoginActivity.this, R.id.ll_main_layout_login, R.anim.slide_up_800, true), 500);

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

            btn_welcome_later.setOnClickListener(view -> {
                if (isTextForLogin) {
                    Handler handlerS = new Handler();
                    handlerS.postDelayed(this::saveAndFinish, 1000);
                }
            });

            isStart = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = findViewById(R.id.inputUsername);
        txtPassword = findViewById(R.id.inputPassword);
        statusIsLogin = findViewById(R.id.statusIsLogin);
        btn_welcome_later = findViewById(R.id.btn_welcome_later);

    }

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
            if (isLogin) {
                try {
                    Toast.makeText(this, "Login success, get servers", Toast.LENGTH_SHORT).show();
                    getServers();
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", MainApplication.device_id);
                    params.putString("exception", "MAA1" + e);
                    LogManager.logEvent(params);
                }
            } else {
                statusIsLogin.setText(message);
                statusIsLogin.setTextColor(ContextCompat.getColor(this, R.color.colorPingRed));
                setActionInputText(true);
            }
        });
    }

    private void getServers() {
        GetAllServers.getAllServers(LoginActivity.this, (isContent, message) -> {
            if (isContent) {
                try {
                    Intent Main = new Intent(LoginActivity.this, MainActivity.class);
                    Main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(Main);
                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                } finally {
                    finish();
                }
            } else {
                statusIsLogin.setText(message);
                statusIsLogin.setTextColor(ContextCompat.getColor(this, R.color.colorPingRed));
                setActionInputText(true);
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}