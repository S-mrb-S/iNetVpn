package com.gold.hamrahvpn.recyclerview;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gold.hamrahvpn.MainApplication;
import com.gold.hamrahvpn.R;
import com.gold.hamrahvpn.model.OpenVpnServerList;
import com.gold.hamrahvpn.recyclerview.cmp.SetHolderHelper;
import com.gold.hamrahvpn.util.LogManager;

import java.util.List;

/**
 * Created by Daichi Furiya / Wasabeef on 2020/08/26.
 * From animators library
 * Edited by MehrabSp
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    private final List<OpenVpnServerList> dataSet;
    private final Context context;

    public MainAdapter(Context context, List<OpenVpnServerList> dataSet) {
        this.context = context;
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.server_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            final OpenVpnServerList OpenVpnServerList = dataSet.get(position);
            SetHolderHelper holderHelper = new SetHolderHelper(context, OpenVpnServerList, holder, position);
            holderHelper.setAllHolder();
        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("exception", "BV0" + e);
            LogManager.logEvent(params);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_country;
        public View vitem;
        public TextView showBool = itemView.findViewById(R.id.boolShowListServer);
        public ImageView iv_flag;
        public ImageView iv_signal_strength;
        public final LinearLayout ll_item = itemView.findViewById(R.id.ll_item);

        public ViewHolder(View itemView) {
            super(itemView);
            vitem = itemView;
            tv_country = itemView.findViewById(R.id.tv_country);
            iv_flag = itemView.findViewById(R.id.iv_flag);
            iv_signal_strength = itemView.findViewById(R.id.iv_signal_strength);
        }
    }
}

