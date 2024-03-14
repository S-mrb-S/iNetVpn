package sp.inetvpn.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import sp.inetvpn.R
import sp.inetvpn.model.SplitList
import sp.inetvpn.util.ManageDisableList

/**
 * 文 件 名: AnimationAdapter
 * 创 建 人: Allen
 * 创建日期: 16/12/24 15:33
 * 邮   箱: AllenCoder@126.com
 * 修改时间：
 * 修改备注：
 */
/**
 * Created by Jay on 24-02-2018.
 * Edited by Mehrab on 04-2024
 */
class SplitAdapter :
    BaseQuickAdapter<List<SplitList>, QuickViewHolder>(listOf(SplitActivity.splitLists)) {
    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_recycler_split, parent)
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: List<SplitList>?) {
        val ss = SplitActivity.splitLists[position]
        //in some cases, it will prevent unwanted situations
            holder.getView<TextView>(R.id.recycler_name).text = ss.appName
            holder.getView<TextView>(R.id.recycler_package_name).text = ss.packageName
            holder.getView<ImageView>(R.id.split_icon_list).setImageDrawable(ss.splitIconList)
            holder.getView<CheckBox>(R.id.recycler_checkbox).setChecked(ss.isSelected)
            holder.getView<CheckBox>(R.id.recycler_checkbox).tag = ss

            holder.getView<CheckBox>(R.id.recycler_checkbox).setOnClickListener { v: View ->
                val cb = v as CheckBox
                val isChecked = cb.isChecked
                if (isChecked) {
                    ss.isSelected = true //Checked
                    ManageDisableList.removePackage(
                        ss.packageName
                    )
                } else {
                    ss.isSelected = false //Unchecked
                    ManageDisableList.addPackage(
                        ss.packageName
                    )
                }
            }
    }

    override fun getItemCount(items: List<List<SplitList>>): Int {
        return SplitActivity.splitLists.size
    }
}