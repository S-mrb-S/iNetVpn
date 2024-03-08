package sp.inetvpn.ui.split

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import sp.inetvpn.model.SplitList
import sp.inetvpn.util.manageDisableList
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future

class SaveData {

    fun loadData(packageManager: PackageManager) {
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val appList = packageManager.queryIntentActivities(mainIntent, 0)
            val executor = Executors.newFixedThreadPool(appList.size)
            val futures: MutableList<Future<SplitList>> = ArrayList()
            for (info in appList) {
                futures.add(executor.submit<SplitList> {
                    val packageName = info.activityInfo.packageName
                    var appName = ""
                    val iconList = info.activityInfo.loadIcon(packageManager)
                    try {
                        val appInfo = packageManager.getApplicationInfo(packageName, 0)
                        appName = packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    }
                    val splitList = SplitList()
                    splitList.appName = appName
                    splitList.splitIconList = iconList
                    Log.d("AP", "SSS")
                    splitList.isSelected = !manageDisableList.isSavePackage(packageName)
                    splitList.packageName = packageName
                    splitList
                })
            }
            executor.shutdown()
            for (future in futures) {
                try {
                    SplitActivity.splitLists.add(future.get())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            }
        } catch (ignored: Exception) {
        }
    }

}