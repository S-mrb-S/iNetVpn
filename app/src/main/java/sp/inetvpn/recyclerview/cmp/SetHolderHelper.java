package sp.inetvpn.recyclerview.cmp;

//import static sp.inetvpn.ui.ServerActivity.FileArray;

import static sp.inetvpn.Data.GlobalData.connectionStorage;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import sp.inetvpn.MainApplication;
import sp.inetvpn.R;
import sp.inetvpn.interfaces.NavItemClickListener;
import sp.inetvpn.model.OpenVpnServerList;
import sp.inetvpn.recyclerview.MainAdapter;
import sp.inetvpn.util.CountryListManager;
import sp.inetvpn.util.LogManager;

/**
 * by MehrabSp on 2024
 */
public class SetHolderHelper {
    private final sp.inetvpn.model.OpenVpnServerList OpenVpnServerList;
    private final MainAdapter.ViewHolder holder;
    private final int position;
    private final NavItemClickListener navItemClickListener;
    private final Context context;

    public SetHolderHelper(Context context, OpenVpnServerList openVpnServerList, MainAdapter.ViewHolder viewHolder, int itemPosition) {
        this.OpenVpnServerList = openVpnServerList;
        this.holder = viewHolder;
        this.position = itemPosition;
        this.navItemClickListener = (NavItemClickListener) context;
        this.context = context;
    }

    public void setAllHolder() {
        setBackgroundHolder();
        setItemHolder();
        setItemListener();
    }

    private void setItemHolder() {
        if (OpenVpnServerList != null) {
            holder.tv_country.setText(OpenVpnServerList.GetCity());
            CountryListManager.OpenVpnSetServerList(OpenVpnServerList.GetImage(), holder.iv_flag);
            switch (OpenVpnServerList.GetSignal()) {
                case "a":
                    holder.iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_full);
                    break;
                case "b":
                    holder.iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_normal);
                    break;
                case "c":
                    holder.iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_medium);
                    break;
                default:
                    holder.iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_low);
                    break;
            }
        } else {
            holder.showBool.setVisibility(View.VISIBLE);
        }

    }

    // select item background
    private void setBackgroundHolder() {
        try {
            int ID = Integer.parseInt(connectionStorage.getString("id", "1"));
            if (position == ID) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorStatsBlue));
            }
        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("exception", "SMA6" + e);
            LogManager.logEvent(params);
        }

    }

    private void setItemListener() {
        try {
            holder.ll_item.setOnClickListener(v -> {
                navItemClickListener.clickedItem(position);

                holder.ll_item.setBackgroundColor(context.getResources().getColor(R.color.colorStatsBlue));
                holder.tv_country.setTextColor(context.getResources().getColor(R.color.colorTextHint));
//            EncryptData En = new EncryptData();
                try {
                    connectionStorage.putString("id", OpenVpnServerList.GetID());
//                    connectionStorage.putString("file_id", OpenVpnServerList.GetFileID());

//                    connectionStorage.putString("fileLocal", "client-114-udp.ovpn");
//                    connectionStorage.putString("file", File);
                    connectionStorage.putString("file", OpenVpnServerList.GetFileID()); // ovpn file
//                    Log.d("NEW FILE", OpenVpnServerList.GetFileID());

                    connectionStorage.putString("city", OpenVpnServerList.GetCity());
                    connectionStorage.putString("country", OpenVpnServerList.GetCountry());
                    connectionStorage.putString("image", OpenVpnServerList.GetImage());
                    connectionStorage.putString("ip", OpenVpnServerList.GetIP());
                    connectionStorage.putString("active", OpenVpnServerList.GetActive());
                    connectionStorage.putString("signal", OpenVpnServerList.GetSignal());
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", MainApplication.device_id);
                    params.putString("exception", "SA6" + e);
                    LogManager.logEvent(params);
                }
            });

        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", MainApplication.device_id);
            params.putString("exception", "SAS6" + e);
            LogManager.logEvent(params);
        }

    }
}
