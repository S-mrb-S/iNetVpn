package com.gold.hamrahvpn.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.gold.hamrahvpn.R;

public class ContactActivity extends Activity {
//    Button btn_about_contact_submit;
//    TextView tv_contact_title, tv_about_about_contact_problem, tv_about_contact_other_problems, tv_about_contact_email;
//    CheckBox checkbox_about_contact_advertising, checkbox_about_contact_speed, checkbox_about_contact_connecting, checkbox_about_contact_servers, checkbox_about_contact_crashed;
//    EditText et_about_contact_other_problems, et_about_contact_email;

//    String advertise, speed, connecting, working, crashed, other, email;
//    private FirebaseAnalytics LogManager;

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

//        checkbox_about_contact_advertising = findViewById(R.id.checkbox_about_contact_advertising);
//        checkbox_about_contact_speed = findViewById(R.id.checkbox_about_contact_speed);
//        checkbox_about_contact_connecting = findViewById(R.id.checkbox_about_contact_connecting);
//        checkbox_about_contact_servers = findViewById(R.id.checkbox_about_contact_servers);
//        checkbox_about_contact_crashed = findViewById(R.id.checkbox_about_contact_crashed);
//
//        et_about_contact_other_problems = findViewById(R.id.et_about_contact_other_problems);
//        et_about_contact_email = findViewById(R.id.et_about_contact_email);
//
//        btn_about_contact_submit = findViewById(R.id.btn_about_contact_submit);

//        Typeface RobotoMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
//        Typeface RobotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
//        Typeface RobotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");


//        tv_contact_title.setTypeface(RobotoMedium);
//        tv_about_about_contact_problem.setTypeface(RobotoMedium);
//        tv_about_contact_other_problems.setTypeface(RobotoMedium);
//        tv_about_contact_email.setTypeface(RobotoMedium);
//
//        checkbox_about_contact_advertising.setTypeface(RobotoRegular);
//        checkbox_about_contact_speed.setTypeface(RobotoRegular);
//        checkbox_about_contact_connecting.setTypeface(RobotoRegular);
//        checkbox_about_contact_servers.setTypeface(RobotoRegular);
//        checkbox_about_contact_crashed.setTypeface(RobotoRegular);
//
//        et_about_contact_other_problems.setTypeface(RobotoRegular);
//        et_about_contact_email.setTypeface(RobotoRegular);
//        btn_about_contact_submit.setTypeface(RobotoBold);

//        btn_about_contact_submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //http://sposcdn.com/buzzvpn/contact_log.php?ip=0000:0000:0000:0000&advertise=adv&speed=speed&connecting=connect&working=working&crashed=crashed&other=otherdata&email=someemail
//                // advertising
//                if (hasInternetConnection()) {
//                    if (checkbox_about_contact_advertising.isChecked()) {
//                        advertise = "true";
//                    } else {
//                        advertise = "false";
//                    }
//
//                    // speed
//                    if (checkbox_about_contact_speed.isChecked()) {
//                        speed = "true";
//                    } else {
//                        speed = "false";
//                    }
//
//                    // connecting
//                    if (checkbox_about_contact_connecting.isChecked()) {
//                        connecting = "true";
//                    } else {
//                        connecting = "false";
//                    }
//
//                    // working
//                    if (checkbox_about_contact_servers.isChecked()) {
//                        working = "true";
//                    } else {
//                        working = "false";
//                    }
//
//                    // crashed
//                    if (checkbox_about_contact_crashed.isChecked()) {
//                        crashed = "true";
//                    } else {
//                        crashed = "false";
//                    }
//
//                    other = et_about_contact_other_problems.getText().toString();
//                    email = et_about_contact_email.getText().toString();
//
//                    if (other == null && other.isEmpty()) {
//                        other = "NULL-other";
//                    }
//                    if (email == null && email.isEmpty()) {
//                        email = "NULL-email";
//                    }
//
//                    SendContactLog Object = new SendContactLog();
//                    Object.start();
//
//                    btn_about_contact_submit.setText("Submitting");
//                    btn_about_contact_submit.setEnabled(false);
//
//                } else {
//
//                }
//            }
//        });

        LinearLayout ll_about_contact_close = findViewById(R.id.ll_contact_back);
        ll_about_contact_close.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        });

    }

//    private boolean hasInternetConnection() {
//        boolean haveConnectedWifi = false;
//        boolean haveConnectedMobile = false;
//        try {
//            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
//            for (NetworkInfo ni : netInfo) {
//                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
//                    if (ni.isConnected())
//                        haveConnectedWifi = true;
//                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
//                    if (ni.isConnected())
//                        haveConnectedMobile = true;
//            }
//        } catch (Exception e) {
////            Bundle params = new Bundle();
////            params.putString("device_id", MainApplication.device_id);
////            params.putString("exception", "CA1" + e);
////            LogManager.logEvent(params);
//        }
//
//        return haveConnectedWifi || haveConnectedMobile;
//    }

//    class SendContactLog extends Thread {
//        @Override
//        public void run() {
//            String str_url = "http://sposcdn.com/buzzvpn/contact_log.php";
//            String str_post = null;
//            final String MyPREFERENCES = "MyPrefs";
//            final String MyKEY = "MyKEY";
//
//            EncryptData en = new EncryptData();
//            String str_ipv4;
//            SharedPreferences sharedPref = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
//            if (!sharedPref.contains(MyKEY)) {
//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putString(MyKEY, en.encrypt(getUniqueKey()));
//                editor.apply();
//                str_ipv4 = getUniqueKey();
//            } else {
//                str_ipv4 = en.decrypt(sharedPref.getString(MyKEY, "خالی"));
//            }
//
//            try {
////                str_post = URLEncoder.encode("ip", "UTF-8") + "=" + URLEncoder.encode(str_ipv4, "UTF-8")
////                        + "&" + URLEncoder.encode("advertise", "UTF-8") + "=" + URLEncoder.encode(advertise, "UTF-8")
////                        + "&" + URLEncoder.encode("speed", "UTF-8") + "=" + URLEncoder.encode(speed, "UTF-8")
////                        + "&" + URLEncoder.encode("connecting", "UTF-8") + "=" + URLEncoder.encode(connecting, "UTF-8")
////                        + "&" + URLEncoder.encode("working", "UTF-8") + "=" + URLEncoder.encode(working, "UTF-8")
////                        + "&" + URLEncoder.encode("crashed", "UTF-8") + "=" + URLEncoder.encode(crashed, "UTF-8")
////                        + "&" + URLEncoder.encode("other", "UTF-8") + "=" + URLEncoder.encode(other, "UTF-8")
////                        + "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
//            } catch (Exception e) { // SF5
////                Bundle params = new Bundle();
////                params.putString("device_id", MainApplication.device_id);
////                params.putString("exception", "CA4" + e);
////                LogManager.logEvent(params);
//            }
//
//            str_url = str_url + "?" + str_post;
//
//            RequestQueue queue = Volley.newRequestQueue(ContactActivity.this);
//            StringRequest stringRequest = new StringRequest(Request.Method.GET, str_url,
//                    new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
////                    Bundle params = new Bundle();
////                    params.putString("device_id", MainApplication.device_id);
////                    params.putString("exception", "CA2" + error.toString());
////                    LogManager.logEvent(params);
//                }
//            });
//            // Add the request to the RequestQueue.
//            queue.add(stringRequest);
//            queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
//                @Override
//                public void onRequestFinished(Request<String> request) {
//                    finish();
//                    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
//                }
//            });
//        }
//
//        private String getUniqueKey() {
//            long time = System.currentTimeMillis();
//            String str_api = String.valueOf(android.os.Build.VERSION.SDK_INT); // API
//            String str_model = String.valueOf(Build.MODEL); // Model
//            String str_manufacturer = String.valueOf(Build.MANUFACTURER); // Manufacturer
//            String version;
//            try {
//                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//                version = pInfo.versionName;
//            } catch (PackageManager.NameNotFoundException e) {
//                version = "00";
////                Bundle params = new Bundle();
////                params.putString("device_id", MainApplication.device_id);
////                params.putString("exception", "CA3" + e);
////                LogManager.logEvent(params);
//            }
//            return time + str_manufacturer + str_api + str_model + version;
//        }
//    }

}
