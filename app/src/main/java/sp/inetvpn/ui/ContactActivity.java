package sp.inetvpn.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import sp.inetvpn.R;
import sp.inetvpn.databinding.ActivityContactBinding;
import sp.inetvpn.handler.SendFeedback;
import sp.inetvpn.util.CheckInternetConnection;

public class ContactActivity extends Activity {
    private ActivityContactBinding binding;

    String feedback, more, email;
    String Connecting, Crashed, Servers;

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.btnAboutContactSubmit.setOnClickListener(v -> {
            // advertising
            if (CheckInternetConnection.netCheck(this)) {

                if (binding.checkboxAboutContactConnecting.isChecked()) {
                    Connecting = "true";
                } else {
                    Connecting = "false";
                }

                // speed
                if (binding.checkboxAboutContactCrashed.isChecked()) {
                    Crashed = "true";
                } else {
                    Crashed = "false";
                }

                // connecting
                if (binding.checkboxAboutContactServers.isChecked()) {
                    Servers = "true";
                } else {
                    Servers = "false";
                }

                Bundle paramsFeed = new Bundle();
                paramsFeed.putString("Connecting", Connecting);
                paramsFeed.putString("Crashed", Crashed);
                paramsFeed.putString("Servers", Servers);

                feedback = String.valueOf(paramsFeed);
                more = binding.etAboutContactOtherProblems.getText().toString();
                email = binding.etAboutContactEmail.getText().toString();

                SendContactLog Object = new SendContactLog();
                Object.start();

                binding.btnAboutContactSubmit.setText("ارسال شد");
                binding.btnAboutContactSubmit.setEnabled(false);

            }
        });

        LinearLayout ll_about_contact_close = findViewById(R.id.ll_contact_back);
        ll_about_contact_close.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        });

    }

    class SendContactLog extends Thread {
        @Override
        public void run() {
            SendFeedback.sendFeedBack(ContactActivity.this, feedback, more, email);
        }

    }

}
