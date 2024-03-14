package sp.inetvpn.util

import android.annotation.SuppressLint
import sp.inetvpn.data.GlobalData
import java.time.LocalDate

// by MRB
/*
    Connection count
 */
@SuppressLint("NewApi")
class UsageConnectionManager {

    private fun saveConnectionInfo(date: LocalDate, count: Int) {
        GlobalData.prefUsageStorage.putLong("connection_${date.toEpochDay()}", count.toLong())
    }

    private fun getConnectionCount(date: LocalDate): Int {
        return GlobalData.prefUsageStorage.getLong("connection_${date.toEpochDay()}", 0).toInt()
    }

    // save connection
    fun establishConnection() {
        val currentDate = LocalDate.now()

        val currentCount = getConnectionCount(currentDate)
        saveConnectionInfo(currentDate, currentCount + 1)
    }

    fun getConnectionCountForToday(): Int {
        val currentDate = LocalDate.now()
        return getConnectionCount(currentDate)
    }

    fun getConnectionCountForYesterday(): Int {
        val yesterday = LocalDate.now().minusDays(1) // یک روز قبل از امروز
        return getConnectionCount(yesterday)
    }

    // All connections
    fun getTotalConnections(): Int {
        var totalConnections = 0

        // بازیابی تمام کلیدهای زمانی
        val allKeys = GlobalData.prefUsageStorage.allKeys()
        for (key in allKeys!!) {
            // از کلیدهایی که با الگوی connection_ شروع می‌شوند استفاده می‌کنیم
            if (key.startsWith("connection_")) {
                // بازیابی تعداد اتصال مربوط به این کلید و افزودن به تعداد کل اتصال‌ها
                val count = GlobalData.prefUsageStorage.getInt(key, 0)
                totalConnections += count
            }
        }

        return totalConnections
    }

}
