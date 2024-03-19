package sp.inetvpn.ui;

import static sp.inetvpn.data.GlobalData.KEY_GRID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import sp.inetvpn.R;
import sp.inetvpn.data.UserData;
import sp.inetvpn.databinding.ActivityServersBinding;
import sp.inetvpn.interfaces.NavItemClickListener;

/*/===========================================================
  by MehrabSp
//===========================================================*/
public class ServersActivity extends Activity implements NavItemClickListener {

    private ActivityServersBinding binding;
    String[][] ServerArray = new String[40][8];
    private final Intent returnIntent = new Intent();

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
            if (!UserData.OpenVpnServerListItemList.isEmpty()) {

                ServersAdapter adapter = new ServersAdapter(ServersActivity.this, UserData.OpenVpnServerListItemList);

                // new adapter
                binding.lsServersList.setLayoutManager(getLayoutManager());
                binding.lsServersList.setItemAnimator(new FadeInAnimator());

                AnimationAdapter defaultAdapter = new AlphaInAnimationAdapter(adapter);
                defaultAdapter.setFirstOnly(true);
                defaultAdapter.setDuration(500);
                defaultAdapter.setInterpolator(new OvershootInterpolator(0.5f));
                binding.lsServersList.setAdapter(defaultAdapter);

            } else {
                ShowNoServerLayout();
            }

        }
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        boolean useGrid = getIntent().getBooleanExtra(KEY_GRID, true);
        return useGrid
                ? new GridLayoutManager(this, 1) // 2
                : new LinearLayoutManager(this);
    }

    /**
     * On navigation item click, close activity and change server
     */
    @Override
    public void clickedItem() {
        returnIntent.putExtra("restart", true);
        this.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

}
/*/===========================================================
  by MehrabSp
//===========================================================*/