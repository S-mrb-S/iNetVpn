package sp.inetvpn.ui;

import static sp.inetvpn.Data.GlobalData.KEY_GRID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import sp.inetvpn.Data.GlobalData;
import sp.inetvpn.R;
import sp.inetvpn.databinding.ActivityServersBinding;
import sp.inetvpn.interfaces.NavItemClickListener;
import sp.inetvpn.model.OpenVpnServerList;

/*/===========================================================
  by MehrabSp
//===========================================================*/
public class ServersActivity extends Activity implements NavItemClickListener {
    private ServersAdapter adapter;

    private ActivityServersBinding binding;
    String[][] ServerArray = new String[40][8];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServersBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ServersList Servers = new ServersList();
        Servers.Load();

        binding.headerLayout.llBack.setOnClickListener(v -> this.onBackPressed());
    }

    private void ShowNoServerLayout() {
        binding.boolShowListServer.setVisibility(View.VISIBLE);
    }

    class ServersList {

        ServersList() {
        }

        void Load() {
            List<OpenVpnServerList> openVpnServerListItemList = new ArrayList<>();

            if (GlobalData.GetAllOpenVpnContent != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(GlobalData.GetAllOpenVpnContent);
                    boolean result = jsonResponse.getBoolean("result");

                    if (result) {
                        // دسترسی به مقادیر داخل data
                        JSONArray dataArray = jsonResponse.getJSONArray("data");

                        for (int x = 0; x < dataArray.length(); x++) {

                            JSONObject dataObject = dataArray.getJSONObject(x);
                            String id = dataObject.getString("id");

                            String tag = dataObject.getString("tag");
                            String name = dataObject.getString("name");
                            String connection = dataObject.getString("connection");

                            ServerArray[x][0] = id;
                            ServerArray[x][1] = connection;
                            ServerArray[x][2] = name; //
                            ServerArray[x][3] = "Germany";
                            ServerArray[x][4] = tag;
                            ServerArray[x][5] = "51.68.191.75";
                            ServerArray[x][6] = "true";
                            ServerArray[x][7] = "a";

                            OpenVpnServerList OpenVpnServerList = getOpenVpnServerList(x);
                            openVpnServerListItemList.add(OpenVpnServerList);

                        }
                    } else {
                        ShowNoServerLayout();
                    }

                } catch (JSONException e) {
                    ShowNoServerLayout();
                }
            } else {
                ShowNoServerLayout();
            }

            adapter = new ServersAdapter(ServersActivity.this, openVpnServerListItemList);

            // new adapter
            binding.lsServersList.setLayoutManager(getLayoutManager());
            binding.lsServersList.setItemAnimator(new FadeInAnimator());

            AnimationAdapter defaultAdapter = new AlphaInAnimationAdapter(adapter);
            defaultAdapter.setFirstOnly(true);
            defaultAdapter.setDuration(500);
            defaultAdapter.setInterpolator(new OvershootInterpolator(0.5f));
            binding.lsServersList.setAdapter(defaultAdapter);

        }
    }

    @NonNull
    private OpenVpnServerList getOpenVpnServerList(int x) {
        OpenVpnServerList OpenVpnServerList = new OpenVpnServerList();
        OpenVpnServerList.SetID(ServerArray[x][0]);
        OpenVpnServerList.SetFileID(ServerArray[x][1]);
        OpenVpnServerList.SetCity(ServerArray[x][2]);
        OpenVpnServerList.SetCountry(ServerArray[x][3]);
        OpenVpnServerList.SetImage(ServerArray[x][4]);
        OpenVpnServerList.SetIP(ServerArray[x][5]);
        OpenVpnServerList.SetActive(ServerArray[x][6]);
        OpenVpnServerList.SetSignal(ServerArray[x][7]);
        return OpenVpnServerList;
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        boolean useGrid = getIntent().getBooleanExtra(KEY_GRID, true);
        return useGrid
                ? new GridLayoutManager(this, 1) // 2
                : new LinearLayoutManager(this);
    }

    /**
     * On navigation item click, close activity and change server
     *
     * @param index: server index
     */
    @Override
    public void clickedItem(int index) {
        resetList();
        this.onBackPressed();
//        Log.d("POS", String.valueOf(index));
//        changeServerLocal.newServer();
//        changeServerLocal.newServer(serverLists.get(index));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetList() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

}
/*/===========================================================
  by MehrabSp
//===========================================================*/