package sp.inetvpn.ui;

import static sp.inetvpn.Data.GlobalData.connectionStorage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sp.inetvpn.R;
import sp.inetvpn.interfaces.NavItemClickListener;
import sp.inetvpn.model.OpenVpnServerList;
import sp.inetvpn.util.CountryListManager;

/**
 * Created by Daichi Furiya / Wasabeef on 2020/08/26.
 * From animators library
 * Edited by MehrabSp
 */
public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ViewHolder> {
    private final List<OpenVpnServerList> dataSet;
    private final Context context;
    private int mSelectedPosition = RecyclerView.NO_POSITION;
    private final NavItemClickListener navItemClickListener;

    public ServersAdapter(Context context, List<OpenVpnServerList> dataSet) {
        this.context = context;
        this.dataSet = dataSet;
        this.navItemClickListener = (NavItemClickListener) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_recycler_servers, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tv_country, tv_name;
        public ImageView iv_flag;
        public LinearLayout ll_item;
        OpenVpnServerList openVpnServerList;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_country = itemView.findViewById(R.id.tv_country);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_flag = itemView.findViewById(R.id.iv_flag);
            ll_item = itemView.findViewById(R.id.ll_item);

            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            this.openVpnServerList = dataSet.get(position);

            // bind view
            tv_country.setText(openVpnServerList.GetImage());
            tv_name.setText(openVpnServerList.GetCountry());
            CountryListManager.OpenVpnSetServerList(openVpnServerList.GetImage(), iv_flag);

            // set item background
            mSelectedPosition = Integer.parseInt(connectionStorage.getString("id", "-1"));

            if (position == mSelectedPosition) {
                ll_item.setBackgroundColor(context.getResources().getColor(R.color.colorStatsBlue));
                tv_country.setTextColor(context.getResources().getColor(R.color.colorTextStats));
                tv_name.setTextColor(context.getResources().getColor(R.color.colorTextStats));
            } else {
                ll_item.setBackgroundColor(context.getResources().getColor(R.color.colorBackground));
                tv_country.setTextColor(context.getResources().getColor(R.color.colorText));
                tv_name.setTextColor(context.getResources().getColor(R.color.colorText));
            }

        }

        @Override
        public void onClick(View v) {
            connectionStorage.putString("id", openVpnServerList.GetID());
            connectionStorage.putString("file", openVpnServerList.GetFileContent());
            connectionStorage.putString("country", openVpnServerList.GetCountry());
            connectionStorage.putString("image", openVpnServerList.GetImage());
            int previousSelectedPosition = mSelectedPosition;
            mSelectedPosition = getAdapterPosition();
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(mSelectedPosition);
            navItemClickListener.clickedItem();
        }

    }

}
