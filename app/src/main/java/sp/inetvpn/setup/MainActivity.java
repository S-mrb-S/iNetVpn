package sp.inetvpn.setup;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.xray.lite.AppConfig;
import com.xray.lite.ui.MainAngActivity;
import com.xray.lite.ui.adapters.MainRecyclerAdapter;
import com.xray.lite.util.AngConfigManager;
import com.xray.lite.util.MmkvManager;
import com.xray.lite.util.Utils;
import com.xray.lite.viewmodel.MainViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import sp.inetvpn.BuildConfig;
import sp.inetvpn.R;
import sp.inetvpn.api.CheckVipUser;
import sp.inetvpn.api.GetVersionApi;
import sp.inetvpn.data.GlobalData;
import sp.inetvpn.data.UserData;
import sp.inetvpn.databinding.ActivityMainBinding;
import sp.inetvpn.ui.FeedbackActivity;
import sp.inetvpn.ui.InfoActivity;
import sp.inetvpn.ui.LoginActivity;
import sp.inetvpn.ui.SplitActivity;
import sp.inetvpn.ui.UsageActivity;
import sp.inetvpn.util.ManageDisableList;

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
        setupViewModel();
        copyAssets();
        sendNotifPermission();
        initializeApp();

        // Load default config type and save.
        GlobalData.defaultItemDialog =
                GlobalData.settingsStorage.getInt("default_connection_type", 0);
        GlobalData.cancelFast =
                GlobalData.settingsStorage.getBoolean("cancel_fast", false);

        ManageDisableList.restoreList(); // disable list
        setupClickListener();
    }

    private void setupClickListener() {
        binding.llProtocolMain.setOnClickListener((v) -> setupMainDialog());
        binding.linearLayoutMainHome.setOnClickListener((v) -> binding.drawerLayout.openDrawer(GravityCompat.START));

        binding.linearLayoutMainServers.setOnClickListener((v) -> {
            if (GlobalData.defaultItemDialog == 0) {
                context.startAngActivity();
            } else {
                context.startServersActivity();
            }
        });

        binding.btnConnection.setOnClickListener((v) -> context.handleButtonConnect());

        binding.layoutTest.setOnClickListener((v) -> layoutTest());
    }

    /**
     * Set config dialog
     */
    private void setupMainDialog() {
        if (!GlobalData.isStart) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(GlobalData.item_txt);
            builder.setSingleChoiceItems(
                    GlobalData.item_options,
                    GlobalData.defaultItemDialog,
                    (dialog, which) -> {
                        GlobalData.settingsStorage.putInt("default_connection_type", which);
                        new Handler().postDelayed(dialog::dismiss, 300);
                        GlobalData.defaultItemDialog = which;
                        context.setFooterFromOtherClass(which);
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            context.showToast("لطفا اول اتصال را قطع کنید");
        }
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
            binding.ivServers.setImageResource(R.drawable.ic_qu_switch_24dp);
        } else {
            Picasso.get().load(imageCountry).into(binding.ivServers);
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
        importBatchConfig(UserData.V2rayServers, "");
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

    private MainRecyclerAdapter adapter = null;

    private MainRecyclerAdapter getAdapter() {
        if (adapter == null) {
            adapter = new MainRecyclerAdapter(new MainAngActivity());
        }
        return adapter;
    }

    private void setupViewModel() {
        mainViewModel.getUpdateTestResultAction().observe(context, this::setTestState);
        mainViewModel.isRunning().observe(context, isRunning -> {
            getAdapter().setRunning(isRunning);
            if (isRunning) {
                context.setStateFromOtherClass(2);
                setTestState(context.getString(R.string.connection_connected));
                binding.layoutTest.setFocusable(true);
            } else {
                if (GlobalData.defaultItemDialog == 0) {
                    context.setStateFromOtherClass(0);
                    setTestState(context.getString(R.string.connection_not_connected));
                    binding.layoutTest.setFocusable(false);
                }
            }
            hideCircle();
        });
        mainViewModel.startListenBroadcast();
    }

    private void copyAssets() {
        String extFolder = Utils.INSTANCE.userAssetPath(context);
        try {
            String[] geo = {"geosite.dat", "geoip.dat"};
            String[] assetFiles = context.getAssets().list("");
            for (String file : geo) {
                if (Arrays.asList(Objects.requireNonNull(assetFiles)).contains(file)) {
                    File target = new File(extFolder, file);
                    if (!target.exists()) {
                        InputStream input = context.getAssets().open(file);
                        OutputStream output = new FileOutputStream(target);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = input.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }
                        output.flush();
                        output.close();
                        input.close();
                        Log.i(AppConfig.ANG_PACKAGE, "Copied from apk assets folder to " + target.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(AppConfig.ANG_PACKAGE, "asset copy failed", e);
        }
    }

    /**
     * navigation main
     */
    public void navigationListener(MenuItem item) {

        int itemId = item.getItemId();

        // switch doesn't work
        if (itemId == R.id.settings) {
            context.startActivity(new Intent(context, UsageActivity.class));
            context.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        } else if (itemId == R.id.getUpdate) {
            try {
                GetVersionApi.setRetVersion(context, retVersion -> {
                    try {
                        if (retVersion != BuildConfig.VERSION_CODE) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://panel.se2ven.sbs/api/update"));
                            context.startActivity(intent);
                        } else {
                            context.showToast("برنامه شما به اخرین ورژن اپدیت هست!");
                        }
                    } catch (Exception e) {
                        context.showToast("برنامه شما به اخرین ورژن اپدیت هست!!");
                    }
                });
            } catch (ActivityNotFoundException activityNotFound) {
                context.showToast("اپدیتی یافت نشد");
            } catch (Exception ignored) {
            }
        } else if (itemId == R.id.splitTun) {
            if (!GlobalData.isStart) {
                context.startActivityForResult(new Intent(context, SplitActivity.class), 33);
                context.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            } else {
                context.showToast("لطفا اول اتصال را قطع کنید");
            }
        } else if (itemId == R.id.info) {
            context.startActivity(new Intent(context, InfoActivity.class));
            context.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        } else if (itemId == R.id.logout) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            GlobalData.appValStorage.encode("isLoginBool", false);
            context.startActivity(new Intent(context, LoginActivity.class));
            context.overridePendingTransition(R.anim.fade_in_1000, R.anim.fade_out_500);
            context.finish();
        } else if (itemId == R.id.feedback) {
            context.startActivity(new Intent(context, FeedbackActivity.class));
            context.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
        }

    }

}
