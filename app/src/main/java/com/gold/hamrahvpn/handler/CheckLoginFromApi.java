package com.gold.hamrahvpn.handler;

import static com.gold.hamrahvpn.util.Data.appValStorage;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gold.hamrahvpn.util.Data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * by MehrabSp
 */
public class CheckLoginFromApi {

    public interface LoginCallback {
        void onLoginResult(boolean isLogin, String message);
    }

    private static Boolean checkLogin = false;
    private static String resLogin = null;
    private static String message = null;

    public static void checkIsLogin(Context context, String username, String password, LoginCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        //for POST requests, only the following line should be changed to
        StringRequest sr = new StringRequest(Request.Method.POST, Data.ApiAdress,
                response -> {
                    resLogin = response;
                    Log.d("RES AGAIN", response);
                    checkLogin = checkUsernameAndPassword(username, password);
                    checkLogin = saveInformation();
                    callback.onLoginResult(checkLogin, message);
                },
                error -> {
                    callback.onLoginResult(checkLogin, message);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("query", Data.ApiLoginName);
                params.put("username", username);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", Data.ApiKey);
                return params;
            }
        };
        queue.add(sr);
    }

    private static boolean checkUsernameAndPassword(String username, String password) {
        boolean res = false;
        message = "پسورد یا یوزرنیم اشتباه است";
        if (resLogin != null) {
            try {
                JSONObject jsonResponse = new JSONObject(resLogin);
//                res = jsonResponse.getBoolean("result");

                JSONObject dataObject = jsonResponse.getJSONObject("data");
                String objectName = dataObject.keys().next();

                JSONObject object = dataObject.getJSONObject(objectName);
                String normalPassword = object.getJSONObject("attrs").getString("normal_password");
                String normalUsername = object.getJSONObject("attrs").getString("normal_username");

                boolean passIs = normalPassword.equals(password);
                boolean userIs = normalUsername.equals(username);

                if (passIs && userIs) {
                    appValStorage.putString("usernameLogin", username);
                    appValStorage.putString("usernamePassword", password);
                    res = true;
                } else {
                    message = "پسورد یا یوزرنیم اشتباه است";
                }

            } catch (JSONException e) {
                return false;
            }
        }
        return res;
    }

    private static boolean saveInformation() {
        boolean res = false;
        if (resLogin != null) {
            try {
                JSONObject jsonResponse = new JSONObject(resLogin);
                boolean resultApi = jsonResponse.getBoolean("result");

                if (resultApi) {
                    JSONObject dataObject = jsonResponse.getJSONObject("data");
                    String objectName = dataObject.keys().next();

                    /**
                     Pass, user
                     User id
                     */

                    JSONObject object = dataObject.getJSONObject(objectName);
                    JSONObject attrs = object.getJSONObject("attrs");

                    String first_login = null, nearest_exp_date = null, first_connection = null, expiration = null;

                    String basic_info = object.getJSONObject("basic_info").getString("group_name"); // service type
                    String user_id = attrs.getString("user_id"); // userid
                    int days = (int) 1.1;

                    try {
                        first_login = attrs.getString("first_login"); // first connect
                        first_connection = attrs.getString("first_connection");
                        nearest_exp_date = attrs.getString("nearest_exp_date"); // تاریخ انفضا
                        expiration = attrs.getString("expiration");
                        days = attrs.getInt("days"); // userid
                    } catch (JSONException ignored) {
                    }

                    if (checkLogin) {
                        if (days != 0) {
                            appValStorage.putString("basic_info", basic_info);
                            appValStorage.putString("first_login", first_login); //
                            appValStorage.putString("first_connection", first_connection); //
                            appValStorage.putString("nearest_exp_date", nearest_exp_date); //
                            appValStorage.putString("user_id", user_id);
                            appValStorage.putString("expiration", expiration);
                            appValStorage.putInt("days", days);
                            res = true;
                        } else {
                            message = "تاریخ انقضا شما به پایان رسیده است!";
                        }
                    }

                } else {
                    message = "مشخصات درست نیست!";
                }

            } catch (JSONException e) {
                appValStorage.putString("res_then_error", resLogin);
                message = "مشکلی در ارسال داده ها وجود دارد!";
                return false;
            }
        } else {
            message = "داده ای از طرف سرور یافت نشد!";
        }
        return res;
    }
}
