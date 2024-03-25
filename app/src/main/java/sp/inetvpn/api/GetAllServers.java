package sp.inetvpn.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sp.inetvpn.data.ApiData;
import sp.inetvpn.data.UserData;
import sp.inetvpn.handler.NetworkTask;
import sp.inetvpn.handler.VolleySingleton;
import sp.inetvpn.model.OpenVpnServerList;

/**
 * by MehrabSp
 */
public class GetAllServers {

    public interface OpenVCallback {
        void onOpenVResult(Boolean retOpenV, @NonNull String message);
    }

    private static Boolean checkStatus = false;
    private static JSONObject resFetch = null;
    private static String message = null;
    private static int OSID = -666;

    public static void getAllServers(Context context, OpenVCallback callback) {
        if (ApiData.UserToken == null) {
            message = "[ERROR] A89P";
            callback.onOpenVResult(checkStatus, message);
            return;
        }

        VolleySingleton volleySingleton = new VolleySingleton(context);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("LanguageID", "");
            jsonBody.put("Query", "");
            jsonBody.put("Operand", "");
            jsonBody.put("PageNo", "");
            jsonBody.put("RowPerPage", "");
            jsonBody.put("SortIndex", "");
        } catch (Exception e) {
            message = "[ERROR] A89P";
            callback.onOpenVResult(checkStatus, message);
            return;
        }

        // Get OSID
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, ApiData.ApiGetOSAddress, jsonBody,
                response -> {
                    Log.d("RES FROM OPEN", String.valueOf(response));

                    try {
                        if (response != null) {
                            resFetch = response;
                            OSID = extractOSID();

                            if (OSID != 666) {
                                checkInformation(context, callback);
                            }

                        } else {
                            callback.onOpenVResult(checkStatus, message);
                        }
                    } catch (Exception ignore) {
                        callback.onOpenVResult(checkStatus, message);
                    }
//                    finally {
//                        callback.onOpenVResult(checkStatus, message);
//                    }
                },
                error -> {
                    // Handle error
                    message = "[Error] from api";
                    callback.onOpenVResult(checkStatus, message);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + ApiData.UserToken);
                // Add other headers if needed
                return headers;
            }
        };
        volleySingleton.addToRequestQueue(sr);
    }

    private static int extractOSID() {
        int osID = -666;
        try {
            // آرایه‌ی Data را از JSON بدست می‌آوریم
            JSONArray dataArray = resFetch.getJSONArray("Data");

            // برای هر آیتم در آرایه Data
            for (int i = 0; i < dataArray.length(); i++) {
                // درآیتم فعلی
                JSONObject item = dataArray.getJSONObject(i);
                // اگر مقدار Name آن Android باشد
                if (item.getString("Name").equals("Android")) {
                    osID = item.getInt("OSID");
                    checkStatus = true;
                    break;
                }
            }
        } catch (JSONException e) {
            message = "[ERROR] json OSID";
        }

        return osID;
    }


    private static void checkInformation(Context context, OpenVCallback callback) {
        try {
            VolleySingleton volleySingleton = new VolleySingleton(context);

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("LanguageID", "");
                jsonBody.put("Query", "");
                jsonBody.put("OSID", String.valueOf(OSID));
                jsonBody.put("Operand", "");
                jsonBody.put("PageNo", "");
                jsonBody.put("RowPerPage", "");
                jsonBody.put("SortIndex", "");
            } catch (Exception e) {
                message = "[ERROR] A89P";
                callback.onOpenVResult(checkStatus, message);
                return;
            }

            JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, ApiData.ApiGetServersAddress, jsonBody,
                    response -> {
                        Log.d("RES os sr", response + "SAlAM");
                        checkStatus = checkAndSaveLayer(response);
                        callback.onOpenVResult(checkStatus, message);
                    },
                    error -> {
                        // Handle error
                        message = "[Error] from api";
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + ApiData.UserToken);
                    // Add other headers if needed
                    return headers;
                }
            };
            volleySingleton.addToRequestQueue(sr);

        } catch (Exception e) {
            message = "[Error] K85S";
        }
    }

    private static boolean checkAndSaveLayer(JSONObject response) {
        boolean isSave = false;
        Log.d("SEVE", "GET");
        try {
            // مقدار Status را دریافت می‌کنیم
            int status = response.getInt("Status");
            if (status == 0) {
                isSave = true;

                // آرایه‌ی Data را از JSON بدست می‌آوریم
                JSONArray dataArray = response.getJSONArray("Data");
                Log.d("SEVE RES", String.valueOf(dataArray));
                // برای هر آیتم در آرایه Data
                for (int i = 0; i < dataArray.length(); i++) {
                    // درآیتم فعلی
                    JSONObject item = dataArray.getJSONObject(i);

                    // مقادیر دیگر مورد نظر را استخراج می‌کنیم
                    String rasTitle = item.getString("RasTitle");

                    if (rasTitle.equals("Open")) {
                        String rasLocation = item.getString("RasLocation");
                        String rasImage = item.getString("RasImageUrl");
                        String serviceFile1 = item.getString("ServiceFile1");
                        if (!serviceFile1.isEmpty()) {
                            NetworkTask.executeTask(serviceFile1, new NetworkTask.NetworkTaskListener() {
                                @Override
                                public void onNetworkTaskCompleted(String result) {
                                    UserData.OpenVpnCount += 1;
                                    // پردازش نتیجه
                                    UserData.OpenVpnServerArray[UserData.OpenVpnCount][0] = String.valueOf(UserData.OpenVpnCount);
                                    UserData.OpenVpnServerArray[UserData.OpenVpnCount][1] = result;
                                    UserData.OpenVpnServerArray[UserData.OpenVpnCount][2] = rasLocation;
                                    UserData.OpenVpnServerArray[UserData.OpenVpnCount][3] = rasImage;

                                    OpenVpnServerList OpenVpnServerList = getOpenVpnServerList(UserData.OpenVpnCount);
                                    UserData.OpenVpnServerListItemList.add(OpenVpnServerList);
                                }

                                @Override
                                public void onNetworkTaskFailed(Exception e) {
                                    // پردازش خطا
                                    message = "مشکلی در پردازش فایل ها وجود دارد";
                                    Log.d("S", " 2مشکلی در پردازش فایل ها وجود دارد");
                                }
                            });
                        } else {
                            message = "مشکلی در پردازش فایل ها وجود دارد";
                            Log.d("S", "مشکلی در پردازش فایل ها وجود دارد");
                            isSave = false;
                            break;
                        }
                        Log.d("RASCONTENT", item.getString("ServiceFile1"));
                    } else if (rasTitle.equals("V2ray")) {
                        String rasExternalUser = item.getString("RasExternalUser");
                        UserData.V2rayServers = UserData.V2rayServers + rasExternalUser + "\n";
                    }

                }

            } else {
                message = "سرور ها به درستی دریافت نشدن";
            }

        } catch (JSONException e) {
            message = "[ERROR] K5P";
            Log.d("Ex from Servers", String.valueOf(e));
        } catch (Exception e) {
            message = "[ERROR] K6P";
            Log.d("Ex from Servers", String.valueOf(e));
        }

        return isSave;
    }

    @NonNull
    private static OpenVpnServerList getOpenVpnServerList(int x) {
        OpenVpnServerList OpenVpnServerList = new OpenVpnServerList();
        OpenVpnServerList.SetID(UserData.OpenVpnServerArray[x][0]);
        OpenVpnServerList.SetFileContent(UserData.OpenVpnServerArray[x][1]);
        OpenVpnServerList.SetCountry(UserData.OpenVpnServerArray[x][2]);
        OpenVpnServerList.SetImage(UserData.OpenVpnServerArray[x][3]);
        return OpenVpnServerList;
    }

}
