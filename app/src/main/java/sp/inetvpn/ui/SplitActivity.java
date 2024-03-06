package sp.inetvpn.ui;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xray.lite.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import sp.inetvpn.R;
import sp.inetvpn.databinding.ActivitySplitBinding;
import sp.inetvpn.listview.CustomAdapter;
import sp.inetvpn.model.SplitList;

/**
 * by Mehrab on 2024
 * Java && Kotlin
 * Easy
 */
public class SplitActivity extends BaseActivity {
    private ActivitySplitBinding binding;
    private CustomAdapter adapter;
    private final List<SplitList> splitLists = new ArrayList<>();

    @Override
    protected void onResume() {
        super.onResume();

        Thread thread = new Thread(() -> runOnUiThread(() -> {
            loadLazyData();

            adapter = new CustomAdapter(SplitActivity.this, splitLists);
            binding.splitRecyclerView.setAdapter(adapter);

//            binding.animationSplitView.cancelAnimation();
            binding.llSplitLoading.setVisibility(View.GONE);
        }));
        thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplitBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(null);

        binding.animationSplitView.setAnimation(R.raw.loading_circle);
        binding.animationSplitView.playAnimation();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        binding.splitRecyclerView.setLayoutManager(mLayoutManager);
        binding.splitRecyclerView.setItemAnimator(new FadeInAnimator());
        binding.splitRecyclerView.setNestedScrollingEnabled(false);

        binding.cbSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLayoutSelectAll(buttonView, isChecked);
            }
        });

        binding.saveButtonSplit.setOnClickListener(v -> this.onBackPressed());

        binding.llContactBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        });
    }

    void setLayoutSelectAll(CompoundButton buttonView, boolean isChecked) {
        if (adapter != null) {
            adapter.toggleSelection(isChecked);
//                    binding.tvSelect.setText(isChecked ? "Deselect All" : "Select All");
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * بارگذاری داده‌های تنبل
     */
    private void loadLazyData() {
        try {
            final PackageManager pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo packageInfo : packages) {
                SplitList SplitList = new SplitList();

                SplitList.setAppName(pm.getApplicationLabel(packageInfo).toString());
                SplitList.setSplitIconList(packageInfo.loadIcon(pm));
                SplitList.setPackageName(packageInfo.packageName);

                splitLists.add(SplitList);  // تولید داده‌ها برای هر لیست به صورت تنبل

//                    runOnUiThread(() -> adapter.notifyItemInserted(splitLists.size() - 1));

            }

        } catch (Exception ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }
}