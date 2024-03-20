package sp.inetvpn.api;

import static sp.inetvpn.data.GlobalData.appValStorage;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sp.inetvpn.MainApplication;
import sp.inetvpn.data.ApiData;
import sp.inetvpn.data.GlobalData;
import sp.inetvpn.handler.VolleySingleton;

/**
 * by MehrabSp
 * این کلاس در اکتیویتی لاگین و لانچر استفاده میشود
 *  کار این اکتیویتی گرفتن دو مقدار یوزرنیم و پسورد هست و با ان اطلاعات را به ایپی ارسال و توکن و سایر اطلاعات کاربر را دریافت و ذخیره میکند
 *  وقتی خروجی این کلاس استفاده شده درست شود یعنی اطلاعات از جمله توکن و اطلاعات کامل کاربر به همراه همان یوزرنیم و پسورد ذخیره میشود
 */
public class CheckLoginFromApi {

    public interface LoginCallback {
        void onLoginResult(boolean isLogin, String message);
    }

    private static Boolean checkLogin = false;
    private static String message = "مقداری دریافت نشد";

    public static void checkIsLogin(Context context, String username, String password, LoginCallback callback) {
        VolleySingleton volleySingleton = new VolleySingleton(context);

        // ریختن مقادیر و دادن ان به ایپی به صورت جیسون
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("Domain", ApiData.domin);
            jsonBody.put("UserName", username);
            jsonBody.put("Password", password);
            jsonBody.put("DeviceID", MainApplication.device_id);
        } catch (Exception e) {
            message = "[Error] json body";
            callback.onLoginResult(checkLogin, message);
            return;
        }

        // ارسال درخواست به ایپی مدنظر
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, ApiData.ApiLoginUserAddress, jsonBody,
                response -> {
                    if (response == null) {
                        callback.onLoginResult(checkLogin, message);
                    } else {
//                    // مقدار برگشتی درست نمایش داده میشود و اگر مشکلی در دریافت اطلاعات یا توکن باشد این مقدار در تابع های دیگر نادرست میشود
                        checkLogin = checkUsernameAndPassword(response);
                        if (checkLogin) {
                            // save information
                            appValStorage.encode("UserName", username);
                            appValStorage.encode("Password", password);
                            // save, check, callback here
                            checkInformation(context, response, callback);
                        } else {
                            callback.onLoginResult(checkLogin, message);
                        }
                    }
                },
                error -> {
                    // Handle error
                    message = "[Error] from api";
                    callback.onLoginResult(checkLogin, message);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", GlobalData.ApiKey);
                // Add other headers if needed
                return headers;
            }
        };
        volleySingleton.addToRequestQueue(sr);
    }

    private static boolean checkUsernameAndPassword(JSONObject response) {
        boolean res = false;
        message = "ورود امکان پذیر نمی باشد";
            try {
                // مقدار Status را دریافت می‌کنیم
                int status = response.getInt("Status");

                if (status == 0) {
                    message = "وارد شدید";
                    res = true; // ذخیره مقدار برگشتی
                } else if (status == -1) {
                    message = "پسورد یا یوزرنیم اشتباه است";
                }

            } catch (JSONException e) {
                message = "[Error] json response";
            }
        return res;
    }

    private static void checkInformation(Context context, JSONObject response, LoginCallback callback) {
        // در اینجا اطلاعات درست بوده و توکن را بررسی میکنیم
            try {
                // مقدار Token را استخراج میکنیم
                String token = response.getJSONArray("Data").getJSONObject(0).getString("Token");
                // در اینجا توکن را داریم و اطلاعات درست هست. پس اطلاعات خود کاربر را بدست میاریم
                if (token.isEmpty()) {
                    message = "توکنی دریافت نشد";
                    checkLogin = false;
                }
                // ذخیره توکن
                ApiData.UserToken = token;
                // درخواست را ایجاد میکنیم
                VolleySingleton volleySingleton = new VolleySingleton(context);
                JsonObjectRequest sr = getJsonObjectRequest(token, callback);
                volleySingleton.addToRequestQueue(sr);

            } catch (JSONException e) {
                message = "[Error] json response";
            }

//        if (resLogin != null) {
//            try {
//                JSONObject jsonResponse = new JSONObject(resLogin);
//                boolean resultApi = jsonResponse.getBoolean("result");
//
//                if (resultApi) {
//                    JSONObject dataObject = jsonResponse.getJSONObject("data");
//                    String objectName = dataObject.keys().next();
//
//                    /**
//                     Pass, user
//                     User id
//                     */
//
//                    JSONObject object = dataObject.getJSONObject(objectName);
//                    JSONObject attrs = object.getJSONObject("attrs");
//
//                    String first_login = null, nearest_exp_date = null, first_connection = null, expiration = null;
//
//                    String basic_info = object.getJSONObject("basic_info").getString("group_name"); // service type
//                    String user_id = attrs.getString("user_id"); // userid
//                    int days = (int) 1.1;
//
//                    try {
//                        first_login = attrs.getString("first_login"); // first connect
//                        first_connection = attrs.getString("first_connection");
//                        nearest_exp_date = attrs.getString("nearest_exp_date"); // تاریخ انفضا
//                        expiration = attrs.getString("expiration");
//                        days = attrs.getInt("days"); // userid
//                    } catch (JSONException ignored) {
//                    }
//
//                    if (checkLogin) {
//                        if (days != 0) {
//                            appValStorage.putString("basic_info", basic_info);
//                            appValStorage.putString("first_login", first_login); //
//                            appValStorage.putString("first_connection", first_connection); //
//                            appValStorage.putString("nearest_exp_date", nearest_exp_date); //
//                            appValStorage.putString("user_id", user_id);
//                            appValStorage.putString("expiration", expiration);
//                            appValStorage.putInt("days", days);
//                            res = true;
//                        } else {
//                            message = "تاریخ انقضا شما به پایان رسیده است!";
//                        }
//                    }
//
//                } else {
//                    message = "مشخصات درست نیست!";
//                }
//
//            } catch (JSONException e) {
//                appValStorage.putString("res_then_error", resLogin);
//                message = "مشکلی در ارسال داده ها وجود دارد!";
//                return false;
//            }
//        } else {
//            message = "داده ای از طرف سرور یافت نشد!";
//        }

    }

    @NonNull
    private static JsonObjectRequest getJsonObjectRequest(String token, LoginCallback callback) {
        // بدنه خالی را ایجاد میکنیم
        JSONObject jsonBody = new JSONObject();

        // ایپی مورد نظر برای چک کردن توکن و دریافت اطلاعات کاربر
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, ApiData.ApiLoginCheckAddress, jsonBody,
                response -> saveInformation(response, callback),
                error -> {
                    // Handle error
                    message = "[Error] from api";
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                // Add other headers if needed
                return headers;
            }
        };
        return sr;
    }

    private static void saveInformation(JSONObject response, LoginCallback callback) {
            try {
                Log.d("RES USER", String.valueOf(response));
                // مقدار Status را دریافت می‌کنیم
                int status = response.getInt("Status");

                if (status != 0) {
                    checkLogin = false;
                    message = "اطلاعات دریافتی نادرست است";
                } else {
                    JSONObject res = response.getJSONArray("Data").getJSONObject(0);

                    if (res.getBoolean("IsActive")) {
                        // مقدار CreationTime را دریافت می‌کنیم
                        String creationTime = res.getString("CreationTime");
                        // مقدار ExternalUser را دریافت می‌کنیم
                        String externalUser = res.getString("ExternalUser");
                        Log.d("CT", creationTime);
                        Log.d("EU", externalUser);

                        appValStorage.putString("basic_info", res.getString("GroupName"));
                        appValStorage.putString("first_connection", res.getString("FirstLogin"));
                        appValStorage.putString("expiration", res.getString("ExpirationTime"));
                        appValStorage.putInt("days", Integer.parseInt(res.getString("RemainedTime")));
                    } else {
                        checkLogin = false;
                        message = "زمان استفاده شما تمام شده است";
                    }
                }

            } catch (JSONException e) {
                message = "[Error] from smart check";
            }

        // save bool here
        appValStorage.encode("isLoginBool", checkLogin);
        callback.onLoginResult(checkLogin, message);
    }
}
