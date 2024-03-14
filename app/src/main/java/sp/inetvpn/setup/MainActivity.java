package sp.inetvpn.setup;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.xray.lite.AppConfig;
import com.xray.lite.util.AngConfigManager;
import com.xray.lite.util.MmkvManager;
import com.xray.lite.util.Utils;
import com.xray.lite.viewmodel.MainViewModel;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import sp.inetvpn.R;
import sp.inetvpn.data.GlobalData;
import sp.inetvpn.databinding.ActivityMainBinding;
import sp.inetvpn.handler.CheckVipUser;
import sp.inetvpn.handler.GetAllV2ray;
import sp.inetvpn.util.CountryListManager;
/**
 * Setup for MainActivity
 * by MehrabSp
 */
public class MainActivity {
    private final sp.inetvpn.ui.MainActivity context;
    private final ActivityMainBinding binding;
    private final MainViewModel mainViewModel;

    public MainActivity(Context context, ActivityMainBinding binding, MainViewModel mainViewModel) {
        this.context = (sp.inetvpn.ui.MainActivity) context;
        this.binding = binding;
        this.mainViewModel = mainViewModel;
    }

    public void setupAll() {
        CheckVipUser.checkInformationUser(context);

        setupDrawer();

        sendNotifPermission();

        initializeApp();
    }
    /**
     * Main Drawer
     */
    private void setupDrawer() {
        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(context, binding.drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
        // set listener
        NavigationView navigationView = context.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(context);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                context,
                binding.drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.drawerLayout.useCustomBehavior(GravityCompat.START); //assign custom behavior for "Left" drawer
        binding.drawerLayout.useCustomBehavior(GravityCompat.END); //assign custom behavior for "Right" drawer
        binding.drawerLayout.setRadius(
                GravityCompat.START,
                25f
        ); //set end container's corner radius (dimension)
    }

    private void sendNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            new RxPermissions(context)
                    .request(Manifest.permission.POST_NOTIFICATIONS)
                    .subscribe((Action1<? super Boolean>) v -> {
                        if (!v) Toast.makeText(
                                context,
                                "Denied",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        }
    }

    /**
     * public
     */
    public String imageCountry =
            GlobalData.connectionStorage.getString("image", GlobalData.NA);
    public String imageCity =
            GlobalData.connectionStorage.getString("city", GlobalData.NA);

    public void handleCountryImage() {
        if (GlobalData.defaultItemDialog == 0) {
            CountryListManager.OpenVpnSetServerList(
                    "v2ray",
                    binding.ivServers
            ); // v2ray
        } else {
            CountryListManager.OpenVpnSetServerList(imageCountry, binding.ivServers);
        }
    }

    public void setNewImage() {
        imageCountry = GlobalData.connectionStorage.getString("image", GlobalData.NA);
        imageCity = GlobalData.connectionStorage.getString("city", GlobalData.NA);
    }

    /**
     * v2ray setup
     */
    // save default config for v2ray
    // save default v2ray config from api
    private void initializeApp() {
        MmkvManager.INSTANCE.removeAllServer();
        GetAllV2ray.setRetV2ray((Context) context, (GetAllV2ray.V2rayCallback) retV2ray -> {
            try {
                importBatchConfig(retV2ray, "");
            } catch (Exception e) {
                Toast.makeText(context, "داده های سرور v2ray ذخیره نشد!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * import config for v2ray
     */
    private void importBatchConfig(String server, String subid) {
        if (subid.isEmpty()) {
            subid = mainViewModel.getSubscriptionId();
        }
        boolean append = subid.isEmpty();
        AngConfigManager angConfigManager = AngConfigManager.INSTANCE; // فرضاً اگر متد getInstance برای ایجاد یک نمونه از AngConfigManager استفاده می‌شد

        int count = angConfigManager.importBatchConfig(server, subid, append);
        if (count <= 0) {
            count = angConfigManager.importBatchConfig(Utils.INSTANCE.decode(server), subid, append);
        }
        if (count <= 0) {
            count = angConfigManager.appendCustomConfigServer(server, subid);
        }
        if (count > 0) {
            mainViewModel.reloadServerList();
        } else {
            Toast.makeText(context, "داده های سرور v2ray ذخیره نشد!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loading circle for V2ray
     */
    public void showCircle() {
        // connection
        binding.fabProgressCircle.show();
    }

    //
    public void hideCircle() {
        try {
            Observable.timer(300, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((v) -> {
                        try {
                            if (binding.fabProgressCircle.isShown()) {
                                binding.fabProgressCircle.hide();
                            }
                        } catch (Exception e) {
                            Log.w(AppConfig.ANG_PACKAGE, e);
                        }
                    });
        } catch (Exception e) {
            Log.d(AppConfig.ANG_PACKAGE, e.toString());
        }
    }

    public void setTestState(String content) {
        binding.tvTestState.setText(content);
    }

    // LayoutTest for V2ray
    public void layoutTest() {
        if (Boolean.TRUE.equals(mainViewModel.isRunning().getValue())) {
            setTestState(context.getString(R.string.connection_test_testing));
            mainViewModel.testCurrentServerRealPing();
        } else {
            // handle error here
            setTestState(context.getString(R.string.connection_test_fail));
        }
    }

}
