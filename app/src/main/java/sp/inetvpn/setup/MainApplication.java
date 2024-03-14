package sp.inetvpn.setup;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.tencent.mmkv.MMKV;

import java.util.Calendar;

import de.blinkt.openvpn.core.App;
import sp.inetvpn.R;
import sp.inetvpn.util.LogManager;

public class MainApplication {
    private final Context context;

    public MainApplication(Context context) {
        this.context = context;
    }

    public void setupClass() {
        // openvpn-client
        MMKV.initialize(context);

        App.setOpenVpn(context, "sp.inetvpn", "spinetvpn", "iNet");

        LogManager.setAppContext(context);
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
