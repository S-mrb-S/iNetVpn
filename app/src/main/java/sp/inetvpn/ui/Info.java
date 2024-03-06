package sp.inetvpn.ui;

import static sp.inetvpn.util.Data.appValStorage;

import android.os.Bundle;
import android.view.View;

import sp.inetvpn.R;
import sp.inetvpn.databinding.ActivityInfoBinding;
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
        String first_connection = appValStorage.getString("first_connection", null);
        String expiration = appValStorage.getString("expiration", null);
        String user_id = appValStorage.getString("usernameLogin", null);
        int days = appValStorage.getInt("days", 0);

        binding.userId.setText(user_id);
        binding.basicInfo.setText(basic_info);

//        text_for_c --< v
//        layout_info_c --> gone
        if (days != 1.1 || expiration != null) {
            binding.textForC.setVisibility(View.GONE);
            binding.layoutInfoC.setVisibility(View.VISIBLE);

            binding.firstLogin.setText(first_connection);
            binding.nearestExpDate.setText(expiration);
            binding.days.setText(String.valueOf(days));
        }

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