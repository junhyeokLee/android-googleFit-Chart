package com.atilmohamine.fitnesstracker.utils

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class MinuteAxisValueFormatter(private val chart: BarChart) : ValueFormatter() {

//    val locale = Locale("ko", "KR")
//    val dateFormat = SimpleDateFormat(pattern, locale)

    private val dateFormat = SimpleDateFormat("mm", Locale.KOREA)

    override fun getFormattedValue(value: Float): String {
        val minuteAgo = chart.data.dataSets[0].entryCount - value.toInt() - 1
        return when (minuteAgo) {
            0 -> "현재"
            else -> {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MINUTE, -minuteAgo)
                dateFormat.format(calendar.time)
            }
        }
    }

}