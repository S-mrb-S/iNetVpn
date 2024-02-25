package com.gold.hamrahvpn.ui;

import static com.gold.hamrahvpn.ui.MainActivity.ENCRYPT_DATA;
import static com.gold.hamrahvpn.util.Data.disconnected;
import static com.gold.hamrahvpn.util.Data.get_details_from_file;
import static com.gold.hamrahvpn.util.Data.get_info_from_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.gold.hamrahvpn.MainApplication;
import com.gold.hamrahvpn.R;
import com.gold.hamrahvpn.handler.GetAllOpenVpn;
import com.gold.hamrahvpn.util.CheckInternetConnection;
import com.gold.hamrahvpn.util.Data;
import com.gold.hamrahvpn.util.LogManager;
import com.xray.lite.ui.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LauncherActivity extends BaseActivity {
    TextView tv_welcome_status, tv_welcome_app;
    Thread thread;
    String FileDetails;
    Boolean isLoginBool = false;
    private long backPressedTime;
    CheckInternetConnection isOnline = new CheckInternetConnection();
    String ID = null, FileID = null, File = null, FilePass = null, FileUser = null, City = null, Country = null, Image = null,
            IP = null, Active = null, Signal = null;
//    int Random;

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        // چک کردن آیا کاربر در بازه‌ای کمتر از 2 ثانیه دکمه برگشت را زده است
        if (currentTime - backPressedTime < 2000) {
            super.onBackPressed();
        } else {
            // نمایش پیام Toast
            Toast.makeText(this, "برای خروج دوباره دکمه برگشت را بزنید", Toast.LENGTH_SHORT).show();
            // ذخیره زمان فعلی
            backPressedTime = currentTime;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        // hide toolbar!
        setSupportActionBar(null);

        tv_welcome_status = findViewById(R.id.tv_welcome_status);
        tv_welcome_app = findViewById(R.id.tv_welcome_app);

        try {
            startAnimation(LauncherActivity.this, R.id.ll_welcome_loading, R.anim.slide_up_800, true);
            Handler handler = new Handler();
            handler.postDelayed(() -> startAnimation(this, R.id.ll_welcome_details, R.anim.slide_up_800, true), 200);
            try {
                isLoginBool = Data.appValStorage.decodeBool("isLoginBool", false);
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WAA9" + e);
                LogManager.logEvent(params);
            }

        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("exception", "MGJA1" + e);
            LogManager.logEvent(params);

        } finally {
            checkInternetLayer();
        }
    }

//    private void runAnyWay() {
//        Handler handler = new Handler();
//        handler.postDelayed(this::endThisActivityWithCheck, 100);
//    }

    void checkInternetLayer() {
        if (isOnline.netCheck(this)) {
            getAppDetails();
        } else {
            tv_welcome_status.setText(disconnected);
            threadCheckInternet();
        }
    }

    void threadCheckInternet() {
        thread = new Thread() {
            boolean isShowText = true;
            boolean isThread = false;

            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {

                        Thread.sleep(1000); // ui refresh

                        if (isThread) {
                            break;
                        }

                        runOnUiThread(() -> {
                            if (isShowText) {
                                isShowText = false;
                                tv_welcome_status.setText("هنگام اتصال به سرور به مشکل خوردیم.. لطفا از اتصال خود اطمینان حاصل کنید!");
                            }

                            if (isOnline.netCheck(LauncherActivity.this)) {
                                checkInternetLayer();
                                isThread = true;
                            }
                        });

                    }
                } catch (InterruptedException e) {
                    isThread = true;
                    checkInternetLayer();
                }
            }
        };
        thread.start();
    }

    void getAppDetails() {
        tv_welcome_status.setText(get_info_from_app);

        GetAllOpenVpn.setRetOpenV(LauncherActivity.this, content -> {
            FileDetails = content;
            Data.GetAllOpenVpnContent = content;

            if (content != null) {
                try {

//                    JSONObject jsonObject = new JSONObject(jsonString);
//        JSONArray dataArray = jsonObject.getJSONArray("data");
//
//        for (int i = 0; i < dataArray.length(); i++) {
//            JSONObject dataObject = dataArray.getJSONObject(i);
//            int id = dataObject.getInt("id");
//            String tag = dataObject.getString("tag");
//            String fileName = dataObject.getString("fileName");
//            String connection = dataObject.getString("connection");
//
//            System.out.println("id: " + id);
//            System.out.println("tag: " + tag);
//            System.out.println("fileName: " + fileName);
//            System.out.println("connection: " + connection);
//        }


                    JSONObject jsonResponse = new JSONObject(content);

                    boolean result = jsonResponse.getBoolean("result");

                    if (result) {
                        // دسترسی به مقادیر داخل data
                        JSONArray dataArray = jsonResponse.getJSONArray("data");

                        for (int x = 0; x < dataArray.length(); x++) {
                            JSONObject dataObject = dataArray.getJSONObject(x);

                            String id = dataObject.getString("id");


                            String username = dataObject.getString("username");
                            String password = dataObject.getString("password");
//                                String tag = dataObject.getString("tag");
                            String connection = dataObject.getString("connection");


                            File = connection;
//                                Data.DefaultOpenVpnC = connection;
                            FilePass = password;
                            FileUser = username;

                            getFileDetails();

                        }
                    } else {
                        tv_welcome_status.setText(disconnected);
                    }

                } catch (JSONException e) {
                    tv_welcome_status.setText(disconnected);
                } finally {

                }
            } else {
                tv_welcome_status.setText(disconnected);
//                runAnyWay();

            }
        });

    }


    void getFileDetails() {
        tv_welcome_status.setText(get_details_from_file);

//            final int min = 0;
//            final int max = 4;
//            Random = new Random().nextInt((max - min) + 1) + min;

        // default
        ID = "0";
        FileID = "1";
        City = "Essen";
        Country = "Japan";
        Image = "germany";
        IP = "51.68.191.75";
        Active = "true";
        Signal = "a";

//                JSONObject json_response = new JSONObject(FileDetails);
//                JSONArray jsonArray = json_response.getJSONArray("free");
//                JSONObject json_object = jsonArray.getJSONObject(Random);

//        try {
//            String ovpnContents = readAssetFile(getApplicationContext(), "client-all-tcp.ovpn");
//            Toast.makeText(this, "try", Toast.LENGTH_SHORT).show();
//            JSONObject json_response = new JSONObject(FileDetails);
//            JSONArray jsonArray = json_response.getJSONArray("ovpn_file");
//            JSONObject json_object = jsonArray.getJSONObject(Integer.parseInt(FileID));
//            FileID = json_object.getString("id");
////                File = json_object.getString("file");
//            File = ovpnContents;
//        } catch (Exception e) {
//            Bundle params = new Bundle();
//            params.putString("device_id", MainApplication.device_id);
//            params.putString("exception", "WA6" + e);
//            LogManager.logEvent(params);
//        }

        // save details
//            try {
//                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//                cuVersion = pInfo.versionName;
//                if (cuVersion.isEmpty()) {
//                    cuVersion = "0.0.1";
//                }
//                Data.appAppDetailsStorage.putString("ads", Ads);
//                Data.appAppDetailsStorage.putString("up_title", upTitle);
//                Data.appAppDetailsStorage.putString("up_description", upDescription);
//                Data.appAppDetailsStorage.putString("up_size", upSize);
//                Data.appAppDetailsStorage.putString("up_version", upVersion);
//                Data.appAppDetailsStorage.putString("cu_version", cuVersion);
//            } catch (Exception e) {
//                Bundle params = new Bundle();
//                params.putString("device_id", MainApplication.device_id);
//                params.putString("exception", "WA7" + e);
//                LogManager.logEvent(params);
//            }
        try {
            Data.connectionStorage.putString("id", ID);
            Data.connectionStorage.putString("file_id", FileID);
            Data.connectionStorage.putString("file", File);
            Data.connectionStorage.putString("filePass", ENCRYPT_DATA.encrypt(FilePass));
            Data.connectionStorage.putString("fileUser", ENCRYPT_DATA.encrypt(FileUser));

//            Data.connectionStorage.putString("fileLocal", "client-114-tcp.ovpn");

            Data.connectionStorage.putString("city", City);
            Data.connectionStorage.putString("country", Country);
            Data.connectionStorage.putString("image", Image);
            Data.connectionStorage.putString("ip", IP);
            Data.connectionStorage.putString("active", Active);
            Data.connectionStorage.putString("signal", Signal);
        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("exception", "WA8" + e);
            LogManager.logEvent(params);
        }

        try {
            Data.appValStorage.putString("file_details", ENCRYPT_DATA.encrypt(FileDetails));
        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("exception", "WA9" + e);
            LogManager.logEvent(params);
        }

        try {
            if (FilePass == null && FileUser == null) {
                tv_welcome_status.setText(disconnected);
            }
        } finally {
            endThisActivityWithCheck();
        }

    }

    private void endThisActivityWithCheck() {
        try {
            if (isLoginBool) {
                Intent Main = new Intent(LauncherActivity.this, MainActivity.class);
                startActivity(Main);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
            } else {
                Intent Welcome = new Intent(LauncherActivity.this, LoginActivity.class);
                startActivity(Welcome);
                overridePendingTransition(R.anim.fade_in_1000, R.anim.fade_out_500);
            }
        } finally {
            finish();
        }
    }

    public void startAnimation(Context ctx, int view, int animation, boolean show) {
        final View Element = findViewById(view);
        if (show) {
            Element.setVisibility(View.VISIBLE);
        } else {
            Element.setVisibility(View.INVISIBLE);
        }
        Animation anim = AnimationUtils.loadAnimation(ctx, animation);
        Element.startAnimation(anim);
    }

}
