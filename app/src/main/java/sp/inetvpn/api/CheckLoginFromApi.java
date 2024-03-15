package sp.inetvpn.api;

import android.content.Context;
import android.util.Log;

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
 */
public class CheckLoginFromApi {

    public interface LoginCallback {
        void onLoginResult(boolean isLogin, String message);
    }

    private static Boolean checkLogin = false;
    private static JSONObject resLogin = null;
    private static String message = null;

    public static void checkIsLogin(Context context, String username, String password, LoginCallback callback) {
        VolleySingleton volleySingleton = new VolleySingleton(context);

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

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, ApiData.ApiLoginUserAdress, jsonBody,
                response -> {
                    Log.d("RES s", String.valueOf(response));
                    resLogin = response;
                    checkLogin = checkUsernameAndPassword();
                    checkLogin = saveInformation(context);
                    callback.onLoginResult(checkLogin, message);
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

    private static boolean checkUsernameAndPassword() {
        boolean res = false;
        message = "ورود امکان پذیر نمی باشد";
        if (resLogin != null) {
            try {
                // مقدار Status را دریافت می‌کنیم
                int status = resLogin.getInt("Status");

                if (status == 0) {
                    message = "";
                    res = true;
                    Log.d("res", "true");
                } else if (status == -1) {
                    message = "پسورد یا یوزرنیم اشتباه است";
                }

            } catch (JSONException e) {
                message = "[Error] json response";
            }
        }
        return res;
    }

    private static boolean saveInformation(Context context) {
        boolean res = false;
        if (resLogin != null && checkLogin) {
            try {
                // مقدار Token را دریافت می‌کنیم
                String token = resLogin.getJSONArray("Data").getJSONObject(0).getString("Token");
                VolleySingleton volleySingleton = new VolleySingleton(context);

                JSONObject jsonBody = new JSONObject();

                JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, ApiData.ApiLoginCheckAdress, jsonBody,
                        response -> {
                            Log.d("RES sr", String.valueOf(response));
                            try {

                                // مقدار Status را دریافت می‌کنیم
                                int status = response.getInt("Status");
                                // مقدار CreationTime را دریافت می‌کنیم
                                String creationTime = response.getJSONArray("Data").getJSONObject(0).getString("CreationTime");
                                // مقدار ExternalUser را دریافت می‌کنیم
                                String externalUser = response.getJSONArray("Data").getJSONObject(0).getString("ExternalUser");


                            } catch (JSONException e) {
                                message = "[Error] from smart check";
                            }

                        },
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
                volleySingleton.addToRequestQueue(sr);

            } catch (JSONException e) {
                message = "[Error] json response";
            }
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

        return res;
    }
}
