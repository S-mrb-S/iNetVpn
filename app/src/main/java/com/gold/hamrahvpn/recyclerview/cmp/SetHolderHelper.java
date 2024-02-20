package com.gold.hamrahvpn.recyclerview.cmp;

import static com.gold.hamrahvpn.recyclerview.MainAdapter.context;
import static com.gold.hamrahvpn.recyclerview.MainAdapter.finishActivityListener;
import static com.gold.hamrahvpn.ui.ServerActivity.FileArray;
import static com.gold.hamrahvpn.util.Data.connectionStorage;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.gold.hamrahvpn.R;
import com.gold.hamrahvpn.recyclerview.MainAdapter;
import com.gold.hamrahvpn.util.CountryListManager;
import com.gold.hamrahvpn.util.Data;
import com.gold.hamrahvpn.util.EncryptData;
import com.gold.hamrahvpn.util.LogManager;

import com.gold.hamrahvpn.MainApplication;

/**
 * Dev by MehraB832 --> github
 *
 * @MehraB832
 */
public class SetHolderHelper {

    public static void setItemHolder(OpenVpnServerList OpenVpnServerList, @NonNull MainAdapter.ViewHolder holder) {
        if (OpenVpnServerList != null) {

            holder.tv_country.setText(OpenVpnServerList.GetCity());
            CountryListManager.OpenVpnSetServerList(OpenVpnServerList.GetImage(), holder.iv_flag);

            holder.ll_item.setOnClickListener(v -> {
                holder.ll_item.setBackgroundColor(context.getResources().getColor(R.color.colorStatsBox));
                holder.tv_country.setTextColor(context.getResources().getColor(R.color.colorTextHint));
                EncryptData En = new EncryptData();
                try {
                    connectionStorage.putString("id", OpenVpnServerList.GetID());
                    connectionStorage.putString("file_id", OpenVpnServerList.GetFileID());
                    connectionStorage.putString("file", En.encrypt(FileArray[Integer.parseInt(OpenVpnServerList.GetFileID())][1]));
                    connectionStorage.putString("city", OpenVpnServerList.GetCity());
                    connectionStorage.putString("country", OpenVpnServerList.GetCountry());
                    connectionStorage.putString("image", OpenVpnServerList.GetImage());
                    connectionStorage.putString("ip", OpenVpnServerList.GetIP());
                    connectionStorage.putString("active", OpenVpnServerList.GetActive());
                    connectionStorage.putString("signal", OpenVpnServerList.GetSignal());
                    Data.hasFile = true;
                    Data.abortConnection = true;
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", MainApplication.device_id);
                    params.putString("exception", "SA6" + e);
                    LogManager.logEvent(params);
                }
                finishActivityListener.finishActivity(true);
            });

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

    public static void setBackgroundHolder(View itemView, int position) {
        int ID = Integer.parseInt(connectionStorage.getString("id", "1"));

        if (position == ID) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.colorStatsBox));
        }
    }
}
