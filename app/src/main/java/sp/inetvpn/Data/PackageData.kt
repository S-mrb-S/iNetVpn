package sp.inetvpn.Data

import android.content.Intent
import android.content.pm.PackageManager
import sp.inetvpn.model.SplitList
import sp.inetvpn.util.ManageDisableList
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future

class PackageData {
    private val splitLists: MutableList<SplitList> = ArrayList()

    fun loadData(packageManager: PackageManager): MutableList<SplitList> {
        try {
            if (splitLists.isEmpty()) {
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

                        splitList.isSelected = !ManageDisableList.isSavePackage(packageName)
                        splitList.packageName = packageName
                        splitList
                    })
                }
                executor.shutdown()
                for (future in futures) {
                    try {
                        splitLists.add(future.get())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                    }
                }
            }

        } catch (ignored: Exception) {
        }

        return splitLists
    }

}