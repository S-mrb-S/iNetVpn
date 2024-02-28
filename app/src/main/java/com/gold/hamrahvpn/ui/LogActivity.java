package com.gold.hamrahvpn.ui;

import static com.gold.hamrahvpn.util.Data.appValStorage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gold.hamrahvpn.R;
import com.gold.hamrahvpn.databinding.ActivityLogBinding;
import com.gold.hamrahvpn.util.LogManager;

public class LogActivity extends AppCompatActivity {

    private ActivityLogBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityLogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(null);

        String LogM = LogManager.getAllLogs();
        String logM = appValStorage.getString("res_then_error", null);

        if (logM != null){
            binding.ipProblemBool.setText("True");
        }

        if (!LogM.equals("[]")){
            binding.logOne.setText(LogM);
        }

//        Log.d("LOG MANG KEYS", LogM);

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