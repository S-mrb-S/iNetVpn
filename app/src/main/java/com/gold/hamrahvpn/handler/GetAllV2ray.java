package com.gold.hamrahvpn.handler;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gold.hamrahvpn.util.Data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * by MehrabSp
 */
public class GetAllV2ray {

    public interface V2rayCallback {
        void onV2rayResult(String retV2ray);
    }

    private static String resV2ray = null;
    private static String retV2ray = null;

    public static void setRetV2ray(Context context, V2rayCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        //for POST requests, only the following line should be changed to
        StringRequest sr = new StringRequest(Request.Method.POST, Data.ApiAdress,
                response -> {
                    resV2ray = response;
                    retV2ray = checkV2ray();
                    callback.onV2rayResult(retV2ray);
                },
                error -> {
                    callback.onV2rayResult(retV2ray);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("query", Data.ApiV2rayName);
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

    private static String checkV2ray() {
        String res = "";
        if (resV2ray != null) {
            try {
                JSONObject jsonResponse = new JSONObject(resV2ray);
                // دسترسی به مقدار result
                boolean result = jsonResponse.getBoolean("result");

                if (result) {
                    // دسترسی به مقادیر داخل data
                    JSONArray dataArray = jsonResponse.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        int id = dataObject.getInt("id");
                        String country = dataObject.getString("country");
                        String tag = dataObject.getString("tag");
                        String connection = dataObject.getString("connetion");

                        res = connection + "\n" + res;
                    }
                }

            } catch (JSONException e) {
                return res;
            }
        }
        return res;
    }
}

