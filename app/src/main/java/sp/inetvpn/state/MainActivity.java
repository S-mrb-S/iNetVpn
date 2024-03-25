package sp.inetvpn.state;

import android.content.Context;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import sp.inetvpn.R;
import sp.inetvpn.data.GlobalData;
import sp.inetvpn.databinding.ActivityMainBinding;
import sp.inetvpn.util.Animations;

public class MainActivity {
    private final sp.inetvpn.ui.MainActivity context;
    private final ActivityMainBinding binding;
    private final sp.inetvpn.setup.MainActivity MainActivity;

    public MainActivity(Context context, ActivityMainBinding binding, sp.inetvpn.setup.MainActivity mainActivity) {
        this.context = (sp.inetvpn.ui.MainActivity) context;
        this.binding = binding;
        this.MainActivity = mainActivity;
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

    /**
     * connection state
     */
    private Animation fadeIn1000 = null;
    private Animation fadeOut1000 = null;
    private Boolean isSetupFirst = true;

    // static
    public static int vpnState =
            0; // 0 --> ninja (no connect) \\ 1 --> loading (circle loading) (connecting) \\ 2 --> connected (wifi (green logo))

    public static int footerState =
            1; // 0 --> v2ray test layout \\ 1 --> main_today


    public void handlerSetupFirst() {
        // set default
        MainActivity.handleCountryImage();
        handleNewVpnState();
        handleNewFooterState();
        showBubbleHomeAnimation();
    }

    public void saveIsStart(Boolean isStart) {
        GlobalData.isStart = isStart;
    }

    public void setNewVpnState(int newState) {
        vpnState = newState;

        handleNewVpnState();
    }

    public void setNewFooterState(int newState) {
        footerState = newState;

        handleNewFooterState();
    }

    public void handleNewVpnState() {
        // cancel animation first (fade in)
        if (!isSetupFirst) {
            Animations.startAnimation(context, R.id.la_animation, R.anim.fade_in_1000, true);
            // stop animation
            binding.laAnimation.cancelAnimation();
        }

        // set new animation
        int animationResource;
        switch (vpnState) {
            case 0:
                animationResource = R.raw.ninjainsecure; // disconnected
                break;
            case 1:
                animationResource = R.raw.loading_circle; // connecting
                break;
            case 2:
                animationResource = R.raw.connected_wifi; // connected
                break;
            default:
                animationResource = R.raw.ninjainsecure; // ??
                break;
        }
        binding.laAnimation.setAnimation(animationResource);

        switch (vpnState) {
            case 0:
                saveIsStart(false);
                // disconnected
                binding.btnConnection.setText(GlobalData.disconnected_btn);
                binding.btnConnection.setBackground(ContextCompat.getDrawable(context, R.drawable.button_connect));

                // scale main animation
                binding.laAnimation.setScaleX(1f);
                binding.laAnimation.setScaleY(1f);

                // bubble
                binding.tvMessageTopText.setText(GlobalData.disconnected_txt);
                binding.tvMessageBottomText.setText(GlobalData.disconnected_txt2);
                break;
            case 1:
                // connecting
                binding.btnConnection.setText(GlobalData.connecting_btn);
                binding.btnConnection.setBackground(ContextCompat.getDrawable(context, R.drawable.button_retry));

                // scale
                binding.laAnimation.setScaleX(0.5f);
                binding.laAnimation.setScaleY(0.5f);

                // bubble
                if (GlobalData.defaultItemDialog == 1) {
                    binding.tvMessageTopText.setText(GlobalData.connecting_txt + " " + MainActivity.imageCity);
                } else {
                    binding.tvMessageTopText.setText(GlobalData.connecting_txt);
                }
                binding.tvMessageBottomText.setText("");
                break;
            case 2:
                saveIsStart(true);
                // connected
                binding.btnConnection.setText(GlobalData.connected_btn);
                binding.btnConnection.setBackground(ContextCompat.getDrawable(context, R.drawable.button_disconnect));

                // scale
                binding.laAnimation.setScaleX(1.5f);
                binding.laAnimation.setScaleY(1.5f);

                // bubble
                if (GlobalData.defaultItemDialog == 1) {
                    binding.tvMessageTopText.setText(GlobalData.connected_txt + " " + MainActivity.imageCity);
                } else {
                    binding.tvMessageTopText.setText(GlobalData.connected_txt);
                }
                binding.tvMessageBottomText.setText("اتصال شما امن است");
                break;
            default:
                // ??
                break;
        }

        // play again
        binding.laAnimation.playAnimation();
    }

    private void handleNewFooterState() {
        if (!isSetupFirst) {
            // cancel all footer data here
            // ??
        }

        switch (footerState) {
            case 0:
                // layout test (v2ray)
                Handler handlerData0 = new Handler();
                handlerData0.postDelayed(() -> Animations.startAnimation(
                        context,
                        R.id.ll_main_layout_test,
                        R.anim.slide_up_800,
                        true
                ), 1000);
                break;
            case 1:
                Handler handlerData1 = new Handler();
                handlerData1.postDelayed(() -> Animations.startAnimation(
                        context,
                        R.id.ll_main_today,
                        R.anim.slide_up_800,
                        true
                ), 1000);
                break;
        }

        MainActivity.handleCountryImage();
    }

    private void showBubbleHomeAnimation() {
        if (isSetupFirst) {
            isSetupFirst = false;

            fadeIn1000 = AnimationUtils.loadAnimation(context, R.anim.fade_in_1000);
            fadeOut1000 = AnimationUtils.loadAnimation(context, R.anim.fade_out_1000);
            binding.llTextBubble.setAnimation(fadeIn1000);

            Handler handlerToday = new Handler();
            handlerToday.postDelayed(() -> {
                Animations.startAnimation(
                        context,
                        R.id.linearLayoutMainHome,
                        R.anim.anim_slide_down,
                        true
                );
                Animations.startAnimation(
                        context,
                        R.id.linearLayoutMainServers,
                        R.anim.anim_slide_down,
                        true
                );
            }, 1000);
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

    public void handleWaitWhenConnect() {
        binding.tvMessageTopText.setText(GlobalData.connecion_wait_txt);

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
