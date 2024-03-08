package sp.inetvpn.ui;

import static sp.inetvpn.Data.GlobalData.appValStorage;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import sp.inetvpn.R;
import sp.inetvpn.databinding.ActivityLogBinding;
import sp.inetvpn.util.LogManager;

public class LogActivity extends AppCompatActivity {

    private ActivityLogBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityLogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

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