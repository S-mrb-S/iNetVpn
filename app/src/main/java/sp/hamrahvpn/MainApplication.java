package sp.hamrahvpn;

import static sp.hamrahvpn.Data.GlobalData.settingsStorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;
import androidx.work.Configuration;

import com.tencent.mmkv.MMKV;

import java.util.Calendar;

import de.blinkt.openvpn.core.App;
import sp.hamrahvpn.util.LogManager;

public class MainApplication extends MultiDexApplication implements Configuration.Provider {
    public static final String PREF_LAST_VERSION = "pref_last_version";
    public static MainApplication application;
    public static String device_id;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        application = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // openvpn-client
        App.setOpenVpn(this, "sp.hamrahvpn", "sphamrahvpn", "iNet");

        if (BuildConfig.DEBUG)
            StrictMode.enableDefaults();

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = defaultSharedPreferences.getInt(PREF_LAST_VERSION, 0) != BuildConfig.VERSION_CODE;
        if (firstRun)
            defaultSharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE).apply();
        MMKV.initialize(this);

        LogManager.setAppContext(this);
        // device id
        device_id = settingsStorage.getString("device_id", "NULL");
        if (device_id.equals("NULL")) {
            device_id = getUniqueKey();
            settingsStorage.putString("device_id", device_id);
            settingsStorage.putString("device_created", String.valueOf(System.currentTimeMillis()));
        }
    }

    // UsageActivity
    private String getUniqueKey() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);
        String Time = getResources().getString(R.string.get_time, year, month, day, hour, minute, second, millis);

        String str_api = String.valueOf(android.os.Build.VERSION.SDK_INT); // API
        String str_model = String.valueOf(Build.MODEL); // Model
        String str_manufacturer = String.valueOf(Build.MANUFACTURER); // Manufacturer
        String version;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "00";
        }

        Log.e("key", Time + str_manufacturer + str_api + str_model + version);
        return Time + str_manufacturer + str_api + str_model + version;
    }

    // work 1.8.1 manager
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().setDefaultProcessName(BuildConfig.APPLICATION_ID + ":bg").build();
    }
}

