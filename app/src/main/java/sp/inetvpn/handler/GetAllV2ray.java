package sp.inetvpn.handler;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sp.inetvpn.data.GlobalData;

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
        StringRequest sr = new StringRequest(Request.Method.POST, GlobalData.ApiAdress,
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
                params.put("query", GlobalData.ApiV2rayName);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", GlobalData.ApiKey);
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
                        String connection = dataObject.getString("connection");

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


