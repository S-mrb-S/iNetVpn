package sp.inetvpn.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sp.inetvpn.R;
import sp.inetvpn.model.SplitList;
import sp.inetvpn.util.manageDisableList;

/**
 * Created by Jay on 24-02-2018.
 * Edited by Mehrab on 04-2024
 */
public class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.ViewHolder> {
    public List<SplitList> splitList;
    private final Context context;

    public SplitAdapter(Context context, List<SplitList> lstStudent) {
        this.context = context;
        this.splitList = lstStudent;
    }

//    public void toggleSelection(boolean isChecked) {
//        if (!splitList.isEmpty()) {
//            Log.d("SPLIT", "SIZE " + splitList.size());
//            for (int i = 0; i < splitList.size(); i++) {
//                Log.d("S: ", "S " + splitList.get(i).getAppName());
//                splitList.get(i).setSelected(isChecked);
//            }
//        }
//    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView recycler_name;
        public TextView recycler_package_name;
        public CheckBox recycler_checkbox;
        public ImageView split_icon_list;

        public ViewHolder(View view) {
            super(view);
            //type cast not required for level 26 and above but still...!
            recycler_name = view.findViewById(R.id.recycler_name);
            recycler_package_name = view.findViewById(R.id.recycler_package_name);
            recycler_checkbox = view.findViewById(R.id.recycler_checkbox);
            split_icon_list = view.findViewById(R.id.split_icon_list);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.split_list_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (splitList != null) {
            //in some cases, it will prevent unwanted situations
            holder.recycler_name.setText(splitList.get(position).getAppName());
            holder.recycler_package_name.setText(splitList.get(position).getPackageName());
            holder.split_icon_list.setImageDrawable(splitList.get(position).getSplitIconList());
            holder.recycler_checkbox.setChecked(splitList.get(position).isSelected());
            holder.recycler_checkbox.setTag(splitList.get(position));

//            holder.recycler_checkbox.setOnClickListener(v -> {
//                CheckBox cb = (CheckBox) v;
//                boolean isChecked = cb.isChecked();
//                Toast.makeText(context, "changhe", Toast.LENGTH_SHORT).show();
//                // اعمال تغییرات مورد نیاز بر اساس isChecked
//            });

            holder.recycler_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                Toast.makeText(context, "changhe", Toast.LENGTH_SHORT).show();
//                Log.d("uu", "uu: " + splitList.get(position).getAppName());
                if (isChecked) {
                    splitList.get(position).setSelected(true);//Checked

//                    Log.d("On", "On: " + splitList.get(position).getAppName());
                    manageDisableList.removePackage(splitList.get(position).getPackageName());
//                    Toast.makeText(context, "on: " + splitList.get(position).getAppName(), Toast.LENGTH_SHORT).show();
                } else {
                    splitList.get(position).setSelected(false);//Unchecked

//                    Log.d("Un", "un: " + splitList.get(position).getAppName());
                    manageDisableList.addPackage(splitList.get(position).getPackageName());
//                    Toast.makeText(context, "un: " + splitList.get(position).getAppName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (splitList == null) ? 0 : splitList.size();
    }
}