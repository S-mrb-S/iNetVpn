package sp.inetvpn;

import static sp.inetvpn.data.GlobalData.settingsStorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;
import androidx.work.Configuration;

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

        if (BuildConfig.DEBUG)
            StrictMode.enableDefaults();

        sp.inetvpn.setup.MainApplication setup = new sp.inetvpn.setup.MainApplication(this);
        setup.setupClass();

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = defaultSharedPreferences.getInt(PREF_LAST_VERSION, 0) != BuildConfig.VERSION_CODE;
        if (firstRun)
            defaultSharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE).apply();

        // device id
        device_id = settingsStorage.getString("device_id", "NULL");
        if (device_id.equals("NULL")) {
            device_id = setup.getUniqueKey();
            settingsStorage.putString("device_id", device_id);
            settingsStorage.putString("device_created", String.valueOf(System.currentTimeMillis()));
        }
    }

    // work 1.8.1 manager
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().setDefaultProcessName(BuildConfig.APPLICATION_ID + ":bg").build();
    }
}

