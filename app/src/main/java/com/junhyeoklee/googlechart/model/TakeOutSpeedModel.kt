package com.junhyeoklee.googlechart.model

import com.google.gson.annotations.SerializedName

data class TakeOutSpeedModel(@SerializedName("Data Source") val dataSource: String,
                     @SerializedName("Data Points") val dataPoints: List<SpeedDataPoint> )

data class SpeedDataPoint(
    @SerializedName("fitValue") val fitValue: List<SpeedFitValue>,
    @SerializedName("originDataSourceId") val originDataSourceId: String,
    @SerializedName("endTimeNanos") val endTimeNanos: Long,
    @SerializedName("dataTypeName") val dataTypeName: String,
    @SerializedName("startTimeNanos") val startTimeNanos: Long,
    @SerializedName("modifiedTimeMillis") val modifiedTimeMillis: Long,
    @SerializedName("rawTimestampNanos") val rawTimestampNanos: Long
)

data class SpeedFitValue(
    @SerializedName("value") val value: SpeedValue
)

data class SpeedValue(
    @SerializedName("fpVal") val fpVal: Float
)