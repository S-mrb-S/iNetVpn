package de.blinkt.openvpn.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * MehrabSp
 */
public class App { // extends /*com.orm.SugarApp*/ Application
    public static final String CHANNEL_ID = "com.gold.hamrahvpn";
    //    public static final int NOTIFICATION_ID = new Random().nextInt(601) + 200;
    static NotificationManager manager;

    public static void setOpenVpn(Context context) {
        createNotificationChannel(context);

        PRNGFixes.apply();
        StatusListener mStatus = new StatusListener();
        mStatus.init(context);
    }

    private static void createNotificationChannel(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "comgoldhamrahvpn",
                        NotificationManager.IMPORTANCE_LOW
                );

                serviceChannel.setSound(null, null);
                manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(serviceChannel);
            }
        } catch (Exception e) {
//            Log.e("error", e.getStackTrace()[0].getMethodName());
        }
    }

}
