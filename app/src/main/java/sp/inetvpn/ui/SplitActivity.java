package sp.inetvpn.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xray.lite.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import sp.inetvpn.R;
import sp.inetvpn.databinding.ActivitySplitBinding;
import sp.inetvpn.model.SplitList;
import sp.inetvpn.recyclerview.SplitAdapter;
import sp.inetvpn.util.manageDisableList;

/**
 * by Mehrab on 2024
 * Java && Kotlin
 * Easy
 */
public class SplitActivity extends BaseActivity {
    private ActivitySplitBinding binding;
    private SplitAdapter adapter;
    private final List<SplitList> splitLists = new ArrayList<>();
    private final Intent returnIntent = new Intent();

    @Override
    protected void onResume() {
        super.onResume();

        Thread thread = new Thread(() -> runOnUiThread(() -> {
            loadData();

            adapter = new SplitAdapter(SplitActivity.this, splitLists);
            binding.splitRecyclerView.setAdapter(adapter);

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

//        binding.cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> setLayoutSelectAll(isChecked));

        binding.saveButtonSplit.setOnClickListener(v -> {
            returnIntent.putExtra("restart", true);
            this.onBackPressed();
        });
        binding.llContactBack.setOnClickListener(v -> this.onBackPressed());
    }

//    @SuppressLint("NotifyDataSetChanged")
//    void setLayoutSelectAll(boolean isChecked) {
//        if (adapter != null) {
//            adapter.toggleSelection(isChecked);
//            adapter.notifyDataSetChanged();
//        }
//    }

    private void loadData() {
        try {
            PackageManager packageManager = getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);

            ExecutorService executor = Executors.newFixedThreadPool(appList.size());
            List<Future<SplitList>> futures = new ArrayList<>();

            for (ResolveInfo info : appList) {
                futures.add(executor.submit(() -> {
                    String packageName = info.activityInfo.packageName;
                    String appName = "";
                    Drawable iconList = info.activityInfo.loadIcon(packageManager);
                    try {
                        ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
                        appName = packageManager.getApplicationLabel(appInfo).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

//                    Log.d("ADD", "ADD LIST");

                    SplitList SplitList = new SplitList();
                    SplitList.setAppName(appName);
                    SplitList.setSplitIconList(iconList);
                    SplitList.setSelected(!manageDisableList.isSavePackage(packageName));
                    SplitList.setPackageName(packageName);
                    return SplitList;
                }));
            }

            executor.shutdown();
            for (Future<SplitList> future : futures) {
                try {
                    splitLists.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }
}