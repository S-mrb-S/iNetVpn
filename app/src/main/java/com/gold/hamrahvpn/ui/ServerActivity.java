package com.gold.hamrahvpn.ui;

import static com.gold.hamrahvpn.ui.MainActivity.ENCRYPT_DATA;
import static com.gold.hamrahvpn.ui.MainActivity.defaultItemDialog;
import static com.gold.hamrahvpn.util.Data.KEY_GRID;
import static com.gold.hamrahvpn.util.Data.KEY_app_details;
import static com.gold.hamrahvpn.util.SafeParcelable.NULL;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gold.hamrahvpn.R;
import com.gold.hamrahvpn.recyclerview.MainAdapter;
import com.gold.hamrahvpn.recyclerview.cmp.OpenVpnServerList;
import com.gold.hamrahvpn.util.FinishActivityListener;
import com.gold.hamrahvpn.util.LogManager;
import com.gold.hamrahvpn.util.MmkvManager;
import com.tencent.mmkv.MMKV;
import com.xray.lite.ui.MainAngActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.gold.hamrahvpn.MainApplication;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

/**
 * by MehraB832
 */
public class ServerActivity extends Activity implements FinishActivityListener {
    RecyclerView recyclerView; // list
    private MainAdapter adapter;
//    ProfileManager pm;
    ImageView iv_server_refresh;
    String AppDetails = "NULL", FileDetails = "NULL";
    String[][] ServerArray = new String[40][8];
    public static String[][] FileArray = new String[40][2];
    MMKV appValStorage = MmkvManager.getAppValStorage();

    // 100
    @Override
    public void onBackPressed() {
        finishActivity(false);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);

//        pm = ProfileManager.getInstance(ServerActivity.this);

        iv_server_refresh = findViewById(R.id.iv_server_refresh);

        String AppValues = appValStorage.getString(KEY_app_details, NULL);
        String AppDetails = ENCRYPT_DATA.decrypt(AppValues);

        if (AppDetails.isEmpty()) {
            getConnectionString getConnectionString = new getConnectionString();
            getConnectionString.GetAppDetails();
        } else {
            ServersList Servers = new ServersList();
            Servers.Load();
        }

        LinearLayout ll_server_back = findViewById(R.id.ll_server_back);
        ll_server_back.setOnClickListener(v -> finishActivity(false));

        LinearLayout ll_server_retry = findViewById(R.id.ll_server_refresh);
        ll_server_retry.setOnClickListener(v -> {
            getConnectionString getConnectionString = new getConnectionString();
            getConnectionString.GetAppDetails();
        });

    }

    private class getConnectionString {
        private void GetAppDetails() {
            iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_process));
            RequestQueue queue = Volley.newRequestQueue(ServerActivity.this);
            queue.getCache().clear();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://raw.githubusercontent.com/gayanvoice/gayankuruppu.github.io/source-json/appdetails.json",
                    Response -> AppDetails = Response, error -> {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "SA1" + error.toString());
                LogManager.logEvent(params);
            });
            queue.add(stringRequest);
            queue.addRequestFinishedListener((RequestQueue.RequestFinishedListener<String>) request -> {
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_cloud));
                    GetFileDetails();
                }, 2000);
                iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_cloud));
                GetFileDetails();
            });
        }

        private void GetFileDetails() {
            iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_process));
            RequestQueue queue = Volley.newRequestQueue(ServerActivity.this);
            queue.getCache().clear();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://raw.githubusercontent.com/gayanvoice/gayankuruppu.github.io/source-json/filedetails.json",
                    Response -> FileDetails = Response, error -> {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "SA2" + error.toString());
                LogManager.logEvent(params);
            });
            queue.add(stringRequest);
            queue.addRequestFinishedListener((RequestQueue.RequestFinishedListener<String>) request -> {
                try {
                    appValStorage.putString("app_details", ENCRYPT_DATA.encrypt(AppDetails));
                    appValStorage.putString("file_details", ENCRYPT_DATA.encrypt(FileDetails));
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", MainApplication.device_id);
                    params.putString("exception", "SA3" + e);
                    LogManager.logEvent(params);
                }

                iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_cloud));
                ServersList Servers = new ServersList();
                Servers.Load();
            });
        }
    }

    class ServersList {

        ServersList() {
        }

        void Load() {
            AppDetails = ENCRYPT_DATA.decrypt(appValStorage.getString("app_details", NULL));
            FileDetails = ENCRYPT_DATA.decrypt(appValStorage.getString("file_details", NULL));
            int NumServers = 0;
            try {
                JSONObject json_response = new JSONObject(AppDetails);
                JSONArray jsonArray = json_response.getJSONArray("free");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_object = jsonArray.getJSONObject(i);
                    ServerArray[i][0] = json_object.getString("id");
                    ServerArray[i][1] = json_object.getString("file");
                    ServerArray[i][2] = json_object.getString("city");
                    ServerArray[i][3] = json_object.getString("country");
                    ServerArray[i][4] = json_object.getString("image");
                    ServerArray[i][5] = json_object.getString("ip");
                    ServerArray[i][6] = json_object.getString("active");
                    ServerArray[i][7] = json_object.getString("signal");
                    NumServers = NumServers + 1;
                }

            } catch (JSONException e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "SA4" + e);
                LogManager.logEvent(params);
                TextView showBool = findViewById(R.id.boolShowListServer);
                showBool.setVisibility(View.VISIBLE);
            }

            try {
                JSONObject jsonResponse = new JSONObject(FileDetails);
                JSONArray jsonArray = jsonResponse.getJSONArray("ovpn_file");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    FileArray[i][0] = jsonObject.getString("id");
                    FileArray[i][1] = jsonObject.getString("file");
                }

            } catch (JSONException e) {
                Bundle params = new Bundle();
                params.putString("device_id", MainApplication.device_id);
                params.putString("exception", "SA5" + e);
                LogManager.logEvent(params);
                TextView showBool = findViewById(R.id.boolShowListServer);
                showBool.setVisibility(View.VISIBLE);
            }

            List<OpenVpnServerList> openVpnServerListItemList = new ArrayList<>();

            for (int x = 0; x < NumServers; x++) {
                OpenVpnServerList OpenVpnServerList = new OpenVpnServerList();
                OpenVpnServerList.SetID(ServerArray[x][0]);
                OpenVpnServerList.SetFileID(ServerArray[x][1]);
                OpenVpnServerList.SetCity(ServerArray[x][2]);
                OpenVpnServerList.SetCountry(ServerArray[x][3]);
                OpenVpnServerList.SetImage(ServerArray[x][4]);
                OpenVpnServerList.SetIP(ServerArray[x][5]);
                OpenVpnServerList.SetActive(ServerArray[x][6]);
                OpenVpnServerList.SetSignal(ServerArray[x][7]);
                openVpnServerListItemList.add(OpenVpnServerList);
            }

            adapter = new MainAdapter(ServerActivity.this, openVpnServerListItemList, ServerActivity.this);

            recyclerView = findViewById(R.id.ls_servers_list);
            // new adapter
            recyclerView.setLayoutManager(getLayoutManager());
            recyclerView.setItemAnimator(new FadeInAnimator());

            AnimationAdapter defaultAdapter = new AlphaInAnimationAdapter(adapter);
            defaultAdapter.setFirstOnly(true);
            defaultAdapter.setDuration(500);
            defaultAdapter.setInterpolator(new OvershootInterpolator(0.5f));
            recyclerView.setAdapter(defaultAdapter);
        }
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        boolean useGrid = getIntent().getBooleanExtra(KEY_GRID, true);
        return useGrid
                ? new GridLayoutManager(this, 1) // 2
                : new LinearLayoutManager(this);
    }

    @Override
    public void finishActivity(Boolean finishWithResetList) {
        if (finishWithResetList) {
            resetList();
        }
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    private void resetList() {
        adapter.notifyDataSetChanged();
    }

}
