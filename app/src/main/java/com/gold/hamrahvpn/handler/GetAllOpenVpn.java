package com.gold.hamrahvpn.handler;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gold.hamrahvpn.model.OpenVpnServerList;
import com.gold.hamrahvpn.util.Data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * by MehrabSp
 */
public class GetAllOpenVpn {

    public interface OpenVCallback {
        void onOpenVResult(ArrayList<OpenVpnServerList> retOpenV);
    }

    private static String resOpenV = null;
    static ArrayList<OpenVpnServerList> servers = new ArrayList<>();

    public static void setRetOpenV(Context context, OpenVCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        //for POST requests, only the following line should be changed to
        StringRequest sr = new StringRequest(Request.Method.POST, Data.ApiAdress,
                response -> {
                    resOpenV = response;
//                    Log.d("OPENV", response);
                    checkOpenV();
                    callback.onOpenVResult(servers);
                },
                error -> {
                    callback.onOpenVResult(servers);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("query", Data.ApiOpenVpnName);
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

    private static void checkOpenV() {
        if (resOpenV != null) {
            try {
                JSONObject jsonResponse = new JSONObject(resOpenV);
                // دسترسی به مقدار result
                boolean result = jsonResponse.getBoolean("result");

                if (result) {
                    // دسترسی به مقادیر داخل data
                    JSONArray dataArray = jsonResponse.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        int id = dataObject.getInt("id");

                        String username = dataObject.getString("username");
                        String password = dataObject.getString("password");

                        String tag = dataObject.getString("tag");
                        String connection = dataObject.getString("conection");

                        servers.add(new OpenVpnServerList(
                                connection,
                                username,
                                password
                        ));

                    }
                }

            } catch (JSONException e) {}
        }
    }
}

