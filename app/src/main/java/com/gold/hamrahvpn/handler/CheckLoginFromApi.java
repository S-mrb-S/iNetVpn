package com.gold.hamrahvpn.handler;

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
        void onLoginResult(boolean isLogin);
    }

    private static Boolean checkLogin = false;
    private static String resLogin = null;

    public static void checkIsLogin(Context context, String username, String password, LoginCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        //for POST requests, only the following line should be changed to
        StringRequest sr = new StringRequest(Request.Method.POST, Data.ApiAdress,
                response -> {
                    resLogin = response;
                    checkLogin = checkUsernameAndPassword(username, password);
                    callback.onLoginResult(checkLogin);
                },
                error -> {
                    callback.onLoginResult(checkLogin);
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
        if (resLogin != null) {
            try {
                JSONObject jsonResponse = new JSONObject(resLogin);
//                res = jsonResponse.getBoolean("result");

                JSONObject dataObject = jsonResponse.getJSONObject("data").getJSONObject("1");
                String normalPassword = dataObject.getJSONObject("attrs").getString("normal_password");
                String normalUsername = dataObject.getJSONObject("attrs").getString("normal_username");

                Log.d("P", normalPassword);
                Log.d("P", normalUsername);
                boolean passIs = normalPassword.equals(password);
                boolean userIs = normalUsername.equals(username);

                if (passIs && userIs) {
                    res = true;
                } else {
                    Log.d("S", "NO");
                    Log.d("S", String.valueOf(userIs));
                    Log.d("S", String.valueOf(passIs));
                }

            } catch (JSONException e) {
                return false;
            }
        } else {
            Log.d("ELS", "FALS");
        }
        return res;
    }
}
