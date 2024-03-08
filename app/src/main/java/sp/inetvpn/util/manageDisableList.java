package sp.inetvpn.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

import sp.inetvpn.Data.GlobalData;

public class manageDisableList {

    public static void saveList() {
        String appsListStr = "";

        if (!GlobalData.disableAppsList.isEmpty()) {
            appsListStr = TextUtils.join(",", GlobalData.disableAppsList);
        }

        GlobalData.settingsStorage.encode("disableAppsList", appsListStr);
    }

    public static void restoreList() {
        GlobalData.disableAppsList.clear();

        String appsListStr = GlobalData.settingsStorage.decodeString("disableAppsList", "");
        assert appsListStr != null;

        // تبدیل رشته به لیست
        if (!appsListStr.isEmpty()) {
            ArrayList<String> disableAppsList = new ArrayList<>(Arrays.asList(appsListStr.split(",")));
            GlobalData.disableAppsList.addAll(disableAppsList);
        }
    }

    public static void addPackage(String packageName) {
        GlobalData.disableAppsList.add(packageName);
        saveList();
    }

    public static void removePackage(String packageName) {
        GlobalData.disableAppsList.remove(packageName);
        saveList();
    }

    public static boolean isSavePackage(String packageName) {
        return GlobalData.disableAppsList.contains(packageName);
    }
}
