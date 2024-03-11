package sp.inetvpn.ui;

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

import java.util.List;

import sp.inetvpn.MainApplication;
import sp.inetvpn.R;
import sp.inetvpn.model.OpenVpnServerList;
import sp.inetvpn.util.LogManager;

/**
 * Created by Daichi Furiya / Wasabeef on 2020/08/26.
 * From animators library
 * Edited by MehrabSp
 */
public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ViewHolder> {
    private final List<OpenVpnServerList> dataSet;
    private final Context context;

    public ServersAdapter(Context context, List<OpenVpnServerList> dataSet) {
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
            ServersAdapterHelper holderHelper = new ServersAdapterHelper(context, OpenVpnServerList, holder, position);
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
        public TextView showBool = itemView.findViewById(R.id.bool_show_list_server);
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

