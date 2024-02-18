package com.gold.hamrahvpn.ui;

import static com.gold.hamrahvpn.ui.MainActivity.ENCRYPT_DATA;
import static com.gold.hamrahvpn.util.Data.disconnected;
import static com.gold.hamrahvpn.util.Data.get_details_from_file;
import static com.gold.hamrahvpn.util.Data.get_info_from_app;
import static com.gold.hamrahvpn.util.Data.isAppDetails;
import static com.gold.hamrahvpn.util.Data.isConnectionDetails;
import static com.gold.hamrahvpn.util.FileUtil.readAssetFile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gold.hamrahvpn.R;
import com.gold.hamrahvpn.util.Data;
import com.gold.hamrahvpn.util.LogManager;
import com.gold.hamrahvpn.util.MmkvManager;
import com.tencent.mmkv.MMKV;
import com.xray.lite.ui.BaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

import com.gold.hamrahvpn.MainApplication;

public class LauncherActivity extends BaseActivity {
    TextView tv_welcome_status, tv_welcome_app;
    String StringGetAppURL, AppDetails, StringGetConnectionURL, FileDetails;
    MMKV appAppDetailsStorage = MmkvManager.getADStorage();
    MMKV connectionStorage = MmkvManager.getConnectionStorage();
    MMKV appValStorage = MmkvManager.getAppValStorage();
    Boolean isLoginBool = false;
    //    TextView tv_welcome_title, tv_welcome_description, tv_welcome_size, tv_welcome_version;
    private long backPressedTime;
    int Random;

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

        StringGetAppURL = "https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/appdetails.json";
        StringGetConnectionURL = "https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/filedetails.json";
        tv_welcome_status = findViewById(R.id.tv_welcome_status);
        tv_welcome_app = findViewById(R.id.tv_welcome_app);

        try {
            startAnimation(LauncherActivity.this, R.id.ll_welcome_loading, R.anim.slide_up_800, true);
            Handler handler = new Handler();
            handler.postDelayed(() -> startAnimation(LauncherActivity.this, R.id.ll_welcome_details, R.anim.slide_up_800, true), 1000);

            try {
                isLoginBool = appValStorage.decodeBool("isLoginBool", false);
                Log.d("IS", isLoginBool.toString());
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WAA9" + e);
                LogManager.logEvent(params);
            }

            if (!isConnectionDetails) {
                if (!Data.isAppDetails) {
                    handler.postDelayed(this::getAppDetails, 200);
                } else {
                    endThisActivityWithCheck();
                }
            } else {
                endThisActivityWithCheck();
            }
        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("exception", "MGJA1" + e);
            LogManager.logEvent(params);

            runAnyWay();
        }

    }

    private void runAnyWay() {
        Handler handler = new Handler();
        handler.postDelayed(this::endThisActivityWithCheck, 4000);
    }

    void getAppDetails() {
        tv_welcome_status.setText(get_info_from_app);
        RequestQueue queue = Volley.newRequestQueue(LauncherActivity.this);
        queue.getCache().clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StringGetAppURL,
                Response -> {
                    Log.e("Response", Response);
                    AppDetails = Response;
                    Data.isAppDetails = true;
                }, error -> {
            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("exception", "WA2" + error.toString());
            LogManager.logEvent(params);

            Data.isAppDetails = false;
        });

        queue.add(stringRequest);
        queue.addRequestFinishedListener((RequestQueue.RequestFinishedListener<String>) request -> {
            if (isAppDetails) {
                getFileDetails();
            } else {
                tv_welcome_status.setText(disconnected);
                runAnyWay();
            }
        });
    }

    void getFileDetails() {
        tv_welcome_status.setText(get_details_from_file);

        try {
            Data.ovpnContents = readAssetFile(this, "client-all-tcp.ovpn");
        } finally {
            endThisActivityWithCheck();
        }
        RequestQueue queue = Volley.newRequestQueue(LauncherActivity.this);
        queue.getCache().clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StringGetConnectionURL,
                Response -> {
                    FileDetails = Response;
                    isConnectionDetails = true;
                }, error -> {
            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("exception", "WA3" + error.toString());
            LogManager.logEvent(params);

            isConnectionDetails = false;
        });
        queue.add(stringRequest);
        queue.addRequestFinishedListener((RequestQueue.RequestFinishedListener<String>) request -> {

            final int min = 0;
            final int max = 4;
            Random = new Random().nextInt((max - min) + 1) + min;

            String Ads = "NULL", cuVersion, upVersion = "NULL", upTitle = "NULL", upDescription = "NULL", upSize = "NULL";
            String ID = "NULL", FileID = "NULL", File = "NULL", City = "NULL", Country = "NULL", Image = "NULL",
                    IP = "NULL", Active = "NULL", Signal = "NULL";
//                String BlockedApps = "NULL";

            try {
                JSONObject jsonResponse = new JSONObject(AppDetails);
                Ads = jsonResponse.getString("ads");
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WA4" + e);
                LogManager.logEvent(params);
            }

            try {
                JSONObject jsonResponse = new JSONObject(AppDetails);
                JSONArray jsonArray = jsonResponse.getJSONArray("update");
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                upVersion = jsonObject.getString("version");
                upTitle = jsonObject.getString("title");
                upDescription = jsonObject.getString("description");
                upSize = jsonObject.getString("size");
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WA5" + e);
                LogManager.logEvent(params);
            }

            try {
                JSONObject json_response = new JSONObject(AppDetails);
                JSONArray jsonArray = json_response.getJSONArray("free");
                JSONObject json_object = jsonArray.getJSONObject(Random);
                ID = json_object.getString("id");
                FileID = json_object.getString("file");
                City = json_object.getString("city");
                Country = json_object.getString("country");
                Image = json_object.getString("image");
                IP = json_object.getString("ip");
                Active = json_object.getString("active");
                Signal = json_object.getString("signal");
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WA5" + e);
                LogManager.logEvent(params);
            }

            try {
                String ovpnContents = readAssetFile(getApplicationContext(), "client-all-tcp.ovpn");
                Toast.makeText(this, "try", Toast.LENGTH_SHORT).show();
                JSONObject json_response = new JSONObject(FileDetails);
                JSONArray jsonArray = json_response.getJSONArray("ovpn_file");
                JSONObject json_object = jsonArray.getJSONObject(Integer.parseInt(FileID));
                FileID = json_object.getString("id");
//                File = json_object.getString("file");
                File = ovpnContents;
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WA6" + e);
                LogManager.logEvent(params);
            }

            // save details
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                cuVersion = pInfo.versionName;
                if (cuVersion.isEmpty()) {
                    cuVersion = "0.0.0";
                }
                appAppDetailsStorage.putString("ads", Ads);
                appAppDetailsStorage.putString("up_title", upTitle);
                appAppDetailsStorage.putString("up_description", upDescription);
                appAppDetailsStorage.putString("up_size", upSize);
                appAppDetailsStorage.putString("up_version", upVersion);
                appAppDetailsStorage.putString("cu_version", cuVersion);
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WA7" + e);
                LogManager.logEvent(params);
            }
            try {
                connectionStorage.putString("id", ID);
                connectionStorage.putString("file_id", FileID);
                connectionStorage.putString("file", ENCRYPT_DATA.encrypt(File));
                connectionStorage.putString("city", City);
                connectionStorage.putString("country", Country);
                connectionStorage.putString("image", Image);
                connectionStorage.putString("ip", IP);
                connectionStorage.putString("active", Active);
                connectionStorage.putString("signal", Signal);
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WA8" + e);
                LogManager.logEvent(params);
            }

            try {
                appValStorage.putString("app_details", ENCRYPT_DATA.encrypt(AppDetails));
                appValStorage.putString("file_details", ENCRYPT_DATA.encrypt(FileDetails));
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "WA9" + e);
                LogManager.logEvent(params);
            }

            try {
                if (!isConnectionDetails) {
                    tv_welcome_status.setText(disconnected);
                    Toast.makeText(this, "هشدار شما افلاین هستید!", Toast.LENGTH_SHORT).show();
                }
//                } else {
//                    startAnimation(LauncherActivity.this, R.id.ll_welcome_loading, R.anim.fade_out_500, false);
//                    Handler handlerData = new Handler();
//                    handlerData.postDelayed(() -> startAnimation(LauncherActivity.this, R.id.ll_update_details, R.anim.slide_up_800, true), 1000);
//                }
            } finally {
                Toast.makeText(this, "End this", Toast.LENGTH_SHORT).show();
                endThisActivityWithCheck();
            }
        });
    }

    private void endThisActivityWithCheck() {
        try {
            if (isLoginBool) {
                Intent Main = new Intent(LauncherActivity.this, MainActivity.class);
                Main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(Main);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
            } else {
                Intent Welcome = new Intent(LauncherActivity.this, LoginActivity.class);
                Welcome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(Welcome);
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
