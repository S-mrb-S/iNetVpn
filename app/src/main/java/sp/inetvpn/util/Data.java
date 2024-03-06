package sp.inetvpn.util;

import com.tencent.mmkv.MMKV;

/**
 * MehrabSp
 */
public class Data {

    // default text
    public static final String default_usage_permissions_txt = "اجازه دهید برنامه همیشه در پس‌زمینه اجرا شود؟";
    public static final String default_usage_permissions_backg_txt = "اجازه دادن به iNet VPN برای اجرای همیشه در برنامه پس‌زمینه ممکن است مصرف حافظه را کاهش دهد";
    public static final String NA = "خالی";
    // dialog
    public static final String[] item_options = {"V2ray", "OpenVpn"};
    public static final String item_txt = "نوع پروتکل";
    // splash screen (loading)
    public static final String get_info_from_app = "دریافت اطلاعات برنامه";
    public static final String get_details_from_file = "در حال دریافت اطلاعات اتصال";
    // (main) set connection button
    public static final String disconnected_btn = "روشن شدن";
    public static final String connecting_btn = "درحال اتصال";
    public static final String connecting_cancel_btn = "لغو";
    public static final String connected_btn = "قطع اتصال";
    public static final String connected_error_btn = "برنامه های VPN را حذف کنید";
    // (main) set message text
    public static final String disconnected = "اتصال قطع شد";
    public static final String disconnected_txt = "اتصال اماده است";
    public static final String disconnected_txt2 = "برای روشن شدن ضربه بزنید !";
    public static final String connecting_txt = "در حال اتصال به";
    public static final String connected_txt = "اتصال برقرار شد";
    public static final String connected_catch_txt = "اتصال امکان پذیر نیست";
    public static final String connected_catch_check_internet_txt = "اینترنت خود را بررسی کنید";
    public static final String default_ziro_txt = "صفر";
    // usage
    public static final String KB = "کیلوبایت";
    public static final String MB = "مگابایت";
    public static final String default_byte_txt = default_ziro_txt + ' ' + KB;
    // usage
    public static final String Version_txt = "نسخه برنامه";
    public static final String device_time_txt = "نصب شده در";
    public static final String minute_ago = "دقیقه اخیر";
    public static final String minutes_ago = "دقیقه پیش";
    public static final String hour_ago = "ساعت اخیر";
    public static final String hours_ago = "ساعت های اخیر";
    public static final String day_ago = "روز اخیر";
    public static final String days_ago = "روز های اخیر";
    public static final String seconds_ago = "ثانیه پیش";
    // mmkv id
    public static final String ID_settings_data = "settings_data";
    public static final String ID_connection_data = "connection_data";
    public static final String ID_log_data = "log_data";
    public static final String ID_app_values = "app_values";
    public static final String ID_PREF_USAGE = "daily_usage";
    public static String TODAY, WEEK, MONTH, YEAR;
    public static final String ID_app_details = "app_details";
    // recyclerview
    public static final String KEY_GRID = "GRID";

    // mmkv
    public static MMKV connectionStorage = MmkvManager.getConnectionStorage(),
            settingsStorage = MmkvManager.getSettingsStorage(),
            appValStorage = MmkvManager.getAppValStorage(),
            prefUsageStorage = MmkvManager.getDUStorage();

    // App
    public static boolean isStart = false;

    // api
    public static final String ApiAdress = "https://panel.se2ven.sbs/api";
    public static final String ApiKey = "RuOq4gdOYT-rgdgrd4tedgr";
    public static final String ApiLoginName = "getuser";
    public static final String ApiOpenVpnName = "getallopenvpn";
    public static final String ApiV2rayName = "getallv2ray";
    public static final String ApiFeedBack = "addcontacts";
    public static final String ApiGetVersion = "getversion";
    //
    public static String GetAllOpenVpnContent;
    public static int defaultItemDialog = 0; // 0 --> V2ray, 1 --> OpenVpn
    public static boolean cancelFast = false;

}
