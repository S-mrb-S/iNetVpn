package sp.hamrahvpn.handler;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sp.hamrahvpn.Data.GlobalData;

/**
 * by MehrabSp
 */
public class GetVersionApi {

    public interface VersionCallback {
        void onVersionResult(int retVersion);
    }

    private static String resVersion = null;
    private static int retVersion = 0;

    public static void setRetVersion(Context context, VersionCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        //for POST requests, only the following line should be changed to
        StringRequest sr = new StringRequest(Request.Method.POST, GlobalData.ApiAdress,
                response -> {
//                    Log.d("SSS", response);
                    resVersion = response;
                    retVersion = checkV2ray();
                    callback.onVersionResult(retVersion);
                },
                error -> {
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("query", GlobalData.ApiGetVersion);
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

    private static int checkV2ray() {
        int res = 0;

        if (resVersion != null) {
            try {
                JSONObject jsonResponse = new JSONObject(resVersion);
                // دسترسی به مقدار result
                boolean result = jsonResponse.getBoolean("result");
                int versionCode = jsonResponse.getInt("versionCode");

                if (result) {
                    res = versionCode;
                }

            } catch (JSONException e) {
                return res;
            }
        }
        return res;
    }

}

