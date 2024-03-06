package sp.inetvpn.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mmkv.MMKV;

import java.util.Arrays;
import java.util.Objects;

public class LogManager {
    static MMKV logStorage = MmkvManager.getLogStorage();
    private static Context appContext;

    public static void setAppContext(Context context) {
        appContext = context.getApplicationContext();
    }

    public static void logEvent(Bundle params) {
        String logKey = String.valueOf(System.currentTimeMillis());
        Toast.makeText(appContext, "error found!", Toast.LENGTH_SHORT).show();
        Log.d("ERR Event", params.toString());
        logStorage.encode(logKey, params);
    }

    public static String getAllLogs() {
        return Arrays.toString(Objects.requireNonNull(logStorage.allKeys()));
    }
}
