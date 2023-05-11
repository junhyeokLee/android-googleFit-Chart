package com.atilmohamine.fitnesstracker.utils

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class HourAxisValueFormatter(private val chart: BarChart) : ValueFormatter() {

//    val locale = Locale("ko", "KR")
//    val dateFormat = SimpleDateFormat(pattern, locale)

    private val dateFormat = SimpleDateFormat("HH", Locale.KOREA)

    override fun getFormattedValue(value: Float): String {
        val HourAgo = chart.data.dataSets[0].entryCount - value.toInt() - 1
        return when (HourAgo) {
            0 -> "현재"
            else -> {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR_OF_DAY, -HourAgo)
                dateFormat.format(calendar.time)
            }
        }
    }

}