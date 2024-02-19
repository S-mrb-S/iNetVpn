package de.blinkt.openvpn.core;

//import static com.gold.hamrahvpn.util.Data.settingsStorage;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

//import com.tencent.mmkv.MMKV;

import java.util.Calendar;
import java.util.Random;
//import androidx.preference.PreferenceManager;
//import com.gold.hamrahvpn.BuildConfig;
//import com.gold.hamrahvpn.R;
//import com.gold.hamrahvpn.util.LogManager;

public class App extends /*com.orm.SugarApp*/ Application {
//    public static boolean isStart;
//    private Boolean firstRun = false;
//    public static int connection_status = 0;
//    public static boolean hasFile = false;
//    public static boolean abortConnection = false;
//    public static long CountDown;
//    public static boolean ShowDailyUsage = true;
//    public static String device_id;
//    public static long device_created;
    public static final String CHANNEL_ID = "com.gold.hamrahvpn";
//    public static final int NOTIFICATION_ID = new Random().nextInt(601) + 200;
    NotificationManager manager;

//    public static final String PREF_LAST_VERSION = "pref_last_version";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        // MMKV
//        MMKV.initialize(this);
//        LogManager.setAppContext(this);
        // V2ray
//        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean firstRun = defaultSharedPreferences.getInt(PREF_LAST_VERSION, 0) != BuildConfig.VERSION_CODE;
//        if (firstRun) {
//            defaultSharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE).apply();
//        }

        // device id
//        device_id = settingsStorage.getString("device_id", "NULL");
//        if (device_id.equals("NULL")) {
//            device_id = getUniqueKey();
//            settingsStorage.putString("device_id", device_id);
//            settingsStorage.putString("device_created", String.valueOf(System.currentTimeMillis()));
//        }

        PRNGFixes.apply();
        StatusListener mStatus = new StatusListener();
        mStatus.init(getApplicationContext());

    }

    private void createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "comgoldhamrahvpn",
                        NotificationManager.IMPORTANCE_LOW
                );

                serviceChannel.setSound(null, null);
                manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(serviceChannel);
            }
        } catch (Exception e) {
            //Log.e("error", e.getStackTrace()[0].getMethodName());
        }
    }

//    private String getUniqueKey() {
//        Calendar now = Calendar.getInstance();
//        int year = now.get(Calendar.YEAR);
//        int month = now.get(Calendar.MONTH);
//        int day = now.get(Calendar.DAY_OF_MONTH);
//        int hour = now.get(Calendar.HOUR_OF_DAY);
//        int minute = now.get(Calendar.MINUTE);
//        int second = now.get(Calendar.SECOND);
//        int millis = now.get(Calendar.MILLISECOND);
//        String Time = getResources().getString(R.string.get_time, year, month, day, hour, minute, second, millis);
//
//        String str_api = String.valueOf(android.os.Build.VERSION.SDK_INT); // API
//        String str_model = String.valueOf(Build.MODEL); // Model
//        String str_manufacturer = String.valueOf(Build.MANUFACTURER); // Manufacturer
//        String version;
//        try {
//            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            version = pInfo.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            version = "00";
//        }
//
//        Log.e("key", Time + str_manufacturer + str_api + str_model + version);
//        return Time + str_manufacturer + str_api + str_model + version;
//    }


}
