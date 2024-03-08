package sp.inetvpn.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

import sp.inetvpn.Data.Data;

public class manageDisableList {

    public static void saveList() {
        String appsListStr = "";

        if (!Data.disableAppsList.isEmpty()) {
            appsListStr = TextUtils.join(",", Data.disableAppsList);
        }

        Data.settingsStorage.encode("disableAppsList", appsListStr);
    }

    public static void restoreList() {
        Data.disableAppsList.clear();

        String appsListStr = Data.settingsStorage.decodeString("disableAppsList", "");
        assert appsListStr != null;

        // تبدیل رشته به لیست
        if (!appsListStr.isEmpty()) {
            ArrayList<String> disableAppsList = new ArrayList<>(Arrays.asList(appsListStr.split(",")));
            Data.disableAppsList.addAll(disableAppsList);
        }
    }

    public static void addPackage(String packageName) {
        Data.disableAppsList.add(packageName);
        saveList();
    }

    public static void removePackage(String packageName) {
        Data.disableAppsList.remove(packageName);
        saveList();
    }

    public static boolean isSavePackage(String packageName) {
        return Data.disableAppsList.contains(packageName);
    }
}
