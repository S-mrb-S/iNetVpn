package com.gold.hamrahvpn.ui;

import static com.gold.hamrahvpn.util.Data.appValStorage;

import android.os.Bundle;
import android.view.View;

import com.gold.hamrahvpn.R;
import com.gold.hamrahvpn.databinding.ActivityInfoBinding;
import com.xray.lite.ui.BaseActivity;

public class Info extends BaseActivity {

    private ActivityInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(null);

        String basic_info = appValStorage.getString("basic_info", null);
        String first_login = appValStorage.getString("first_login", null);
        String nearest_exp_date = appValStorage.getString("nearest_exp_date", null);
        String user_id = appValStorage.getString("user_id", null);
        int days = appValStorage.getInt("days", 0);

        binding.basicInfo.setText(basic_info);
        binding.firstLogin.setText(first_login);
        binding.nearestExpDate.setText(nearest_exp_date);
        binding.userId.setText(user_id);
        binding.days.setText(String.valueOf(days));

        binding.llContactBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }
}