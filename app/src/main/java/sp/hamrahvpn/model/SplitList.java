package sp.hamrahvpn.model;

import android.graphics.drawable.Drawable;

/**
 * Created by Jay on 24-02-2018.
 */

public class SplitList {
    private String AppName;
    private String PackageName;
    private Drawable SplitIconList;

    public String getAppName() {
        return AppName;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }


    public String getPackageName() {
        return PackageName;
    }

    public void setPackageName(String packageName) {
        PackageName = packageName;
    }


    public Drawable getSplitIconList() {
        return SplitIconList;
    }

    public void setSplitIconList(Drawable src) {
        SplitIconList = src;
    }

    //This private field to maintain to every row's state...!
    private boolean isSelected = true;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}