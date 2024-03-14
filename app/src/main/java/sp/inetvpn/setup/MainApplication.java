package sp.inetvpn.setup;

import static sp.inetvpn.MainApplication.device_id;
import static sp.inetvpn.data.GlobalData.settingsStorage;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.tencent.mmkv.MMKV;

import java.util.Calendar;

import de.blinkt.openvpn.core.App;
import sp.inetvpn.BuildConfig;
import sp.inetvpn.R;
import sp.inetvpn.util.LogManager;

public class MainApplication {
    private final sp.inetvpn.MainApplication context;
    public static final String PREF_LAST_VERSION = "pref_last_version";

    public MainApplication(sp.inetvpn.MainApplication context) {
        this.context = context;
    }

    public void setupClass() {
        MMKV.initialize(context);
        // openvpn-client
        App.setOpenVpn(context, "sp.inetvpn", "spinetvpn", "iNet");
        LogManager.setAppContext(context);

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firstRun = defaultSharedPreferences.getInt(PREF_LAST_VERSION, 0) != BuildConfig.VERSION_CODE;
        if (firstRun)
            defaultSharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE).apply();

        // device id
        device_id = settingsStorage.getString("device_id", "NULL");
        if (device_id.equals("NULL")) {
            device_id = getUniqueKey();
            settingsStorage.putString("device_id", device_id);
            settingsStorage.putString("device_created", String.valueOf(System.currentTimeMillis()));
        }
    }

    // for UsageActivity, Time for when installed app
    public String getUniqueKey() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);
        String Time = context.getResources().getString(R.string.get_time, year, month, day, hour, minute, second, millis);

        String str_api = String.valueOf(android.os.Build.VERSION.SDK_INT); // API
        String str_model = String.valueOf(Build.MODEL); // Model
        String str_manufacturer = String.valueOf(Build.MANUFACTURER); // Manufacturer
        String version;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "00";
        }

        Log.e("key", Time + str_manufacturer + str_api + str_model + version);
        return Time + str_manufacturer + str_api + str_model + version;
    }
}
