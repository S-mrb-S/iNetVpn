package sp.hamrahvpn.handler;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import sp.hamrahvpn.Data.GlobalData;

/**
 * by MehrabSp
 */
public class GetAllOpenVpn {

    public interface OpenVCallback {
        void onOpenVResult(String retOpenV);
    }

    public static void setRetOpenV(Context context, OpenVCallback callback) {
        VolleySingleton volleySingleton = new VolleySingleton(context);
        StringRequest sr = new StringRequest(Request.Method.POST, GlobalData.ApiAdress,
                callback::onOpenVResult,
                error -> callback.onOpenVResult(null)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("query", GlobalData.ApiOpenVpnName);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", GlobalData.ApiKey);
                return params;
            }
        };
        volleySingleton.addToRequestQueue(sr);
    }
}
