package sp.inetvpn.handler;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import sp.inetvpn.util.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * by MehrabSp
 */
public class GetAllOpenVpn {

    public interface OpenVCallback {
        void onOpenVResult(String retOpenV);
    }

    public static void setRetOpenV(Context context, OpenVCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST, Data.ApiAdress,
                callback::onOpenVResult,
                error -> callback.onOpenVResult(null)) {
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

}

