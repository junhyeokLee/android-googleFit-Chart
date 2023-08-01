package com.junhyeoklee.googlechart.model

import com.google.gson.annotations.SerializedName

data class TakeOutDistanceModel(@SerializedName("Data Source") val dataSource: String,
                     @SerializedName("Data Points") val dataPoints: List<DistanceDataPoint> )

data class DistanceDataPoint(
    @SerializedName("fitValue") val fitValue: List<DistanceFitValue>,
    @SerializedName("originDataSourceId") val originDataSourceId: String,
    @SerializedName("endTimeNanos") val endTimeNanos: Long,
    @SerializedName("dataTypeName") val dataTypeName: String,
    @SerializedName("startTimeNanos") val startTimeNanos: Long,
    @SerializedName("modifiedTimeMillis") val modifiedTimeMillis: Long,
    @SerializedName("rawTimestampNanos") val rawTimestampNanos: Long
)

data class DistanceFitValue(
    @SerializedName("value") val value: DistanceValue
)

data class DistanceValue(
    @SerializedName("fpVal") val fpVal: Float
)