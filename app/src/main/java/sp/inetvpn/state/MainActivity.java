package sp.inetvpn.state;

import android.content.Context;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import sp.inetvpn.R;
import sp.inetvpn.data.GlobalData;
import sp.inetvpn.databinding.ActivityMainBinding;

public class MainActivity {
    private final sp.inetvpn.ui.MainActivity context;
    private final ActivityMainBinding binding;

    public MainActivity(Context context, ActivityMainBinding binding) {
        this.context = (sp.inetvpn.ui.MainActivity) context;
        this.binding = binding;
    }

    /**
     * Footer
     */
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
    String today = df.format(Calendar.getInstance().getTime());

    public void restoreTodayTextTv() {
        long longUsageToday = GlobalData.prefUsageStorage.getLong(today, 0);
        if (longUsageToday < 1000) {
            binding.tvDataTodayText.setText(GlobalData.default_ziro_txt + " " + GlobalData.KB);
        } else if (longUsageToday <= 1000000) {
            binding.tvDataTodayText.setText((longUsageToday / 1000) + GlobalData.KB);
        } else {
            binding.tvDataTodayText.setText((longUsageToday / 1000000) + GlobalData.MB);
        }
    }

    public void handleErrorWhenConnect() {
        binding.tvMessageTopText.setText(GlobalData.connected_catch_txt);
        binding.tvMessageBottomText.setText(GlobalData.connected_catch_check_internet_txt);

//        binding.btnConnection.text = Data.connecting_btn
        binding.btnConnection.setBackground(ContextCompat.getDrawable(
                context,
                R.drawable.button_retry
        ));
    }

    public void handleAUTH() {
        binding.tvMessageTopText.setText("درحال ورود به سرور");
        binding.tvMessageBottomText.setText("لطفا منتظر بمانید");

        binding.btnConnection.setText("لغو");
        binding.btnConnection.setBackground(ContextCompat.getDrawable(
                context,
                R.drawable.button_retry
        ));
    }

}
