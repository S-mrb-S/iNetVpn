package sp.inetvpn.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

public class LogManager {
    private static Context appContext;

    public static void setAppContext(Context context) {
        appContext = context.getApplicationContext();
    }

    public static void logEvent(Bundle params) {
        String logKey = String.valueOf(System.currentTimeMillis());
        Toast.makeText(appContext, "error found!", Toast.LENGTH_SHORT).show();


//        SharedPreferences sp_settings;
//        sp_settings = getSharedPreferences("daily_usage", 0);
//        long connection_today = sp_settings.getLong(TODAY + "_connections", 0);
//        long connection_total = sp_settings.getLong("total_connections", 0);
//        SharedPreferences.Editor editor = sp_settings.edit();
//        editor.putLong(TODAY + "_connections", connection_today + 1);
//        editor.putLong("total_connections", connection_total + 1);
//        editor.apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setMessage(" \n یک مشکلی در برنامه به وجود اومد: \n" +
                logKey + "\n" +
                params.getString("device_id") + "\n" +
                params.getString("exception"));

        builder.setNegativeButton(
                "OK",
                (dialogInterface, i) -> {
                }
        );

        // ایجاد AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
