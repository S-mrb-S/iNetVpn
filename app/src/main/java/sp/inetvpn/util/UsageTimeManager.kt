//package sp.inetvpn.util
//
//import android.annotation.SuppressLint
//import sp.inetvpn.Data.GlobalData
//import java.time.LocalDate
//import java.time.LocalTime
//import java.time.format.DateTimeFormatter
//
//// by MRB
///*
//    Connection count
// */
//@SuppressLint("NewApi")
//class UsageTimeManager {
//
//    fun saveConnectionTime(timeString: String) {
//        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
//        val time = LocalTime.parse(timeString, formatter)
//        GlobalData.prefUsageStorage.putString("connection_${LocalDate.now()}", time.toString())
//    }
//
//    fun getConnectionTime(): LocalTime? {
//        val timeString =
//            GlobalData.prefUsageStorage.getString("connection_${LocalDate.now()}", null)
//        return if (timeString != null) LocalTime.parse(timeString) else null
//    }
//
//}
