package sp.inetvpn.api;

import sp.inetvpn.data.GlobalData;
import sp.inetvpn.ui.MainActivity;
import sp.inetvpn.util.CheckInternetConnection;

public class CheckVipUser {

    public static void checkInformationUser(MainActivity context) {
        if (CheckInternetConnection.netCheck(context)) {

            String uL = GlobalData.appValStorage.getString("usernameLogin", null);
            String uU = GlobalData.appValStorage.getString("usernamePassword", null);


//            CheckLoginFromApi.checkIsLogin(
//                    context,
//                    uL,
//                    uU,
//                    (getApi, v) -> {
//                        try {
//                            if (!getApi) {
//                                Toast.makeText(context, "اشتراک شما به پایان رسیده است", Toast.LENGTH_SHORT).show();
//                                context.startActivity(new Intent(context, LoginActivity.class));
//                                context.overridePendingTransition(R.anim.fade_in_1000, R.anim.fade_out_500);
//                                context.finish();
//                            }
//                        } catch (Exception e) {
//                            Toast.makeText(context, "مشکلی در بررسی وجود دارد", Toast.LENGTH_SHORT).show();
//                        }
//                    });

        }
    }

}
