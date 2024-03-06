package sp.inetvpn.handler;

import android.content.Intent;
import android.widget.Toast;

import sp.inetvpn.R;
import sp.inetvpn.ui.LoginActivity;
import sp.inetvpn.ui.MainActivity;
import sp.inetvpn.util.CheckInternetConnection;
import sp.inetvpn.util.Data;

public class CheckVipUser {

    static CheckInternetConnection isOnline = new CheckInternetConnection();

    public static void checkInformationUser(MainActivity context) {
        if (isOnline.netCheck(context)) {

            String uL = Data.appValStorage.getString("usernameLogin", null);
            String uU = Data.appValStorage.getString("usernamePassword", null);

            CheckLoginFromApi.checkIsLogin(
                    context,
                    uL,
                    uU,
                    (getApi, v) -> {
                        try {
                            if (!getApi) {
                                Toast.makeText(context, "اشتراک شما به پایان رسیده است", Toast.LENGTH_SHORT).show();
                                context.startActivity(new Intent(context, LoginActivity.class));
                                context.overridePendingTransition(R.anim.fade_in_1000, R.anim.fade_out_500);
                                context.finish();
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "مشکلی در بررسی وجود دارد", Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

}
