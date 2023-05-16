package com.junhyeoklee.googlechart.model

import com.google.gson.annotations.SerializedName

data class StepModel(@SerializedName("Data Source") val dataSource: String,
                     @SerializedName("Data Points") val dataPoints: List<DataPoint>
                      )

data class DataPoint(
    @SerializedName("fitValue") val fitValue: List<FitValue>,
    @SerializedName("originDataSourceId") val originDataSourceId: String,
    @SerializedName("endTimeNanos") val endTimeNanos: Long,
    @SerializedName("dataTypeName") val dataTypeName: String,
    @SerializedName("startTimeNanos") val startTimeNanos: Long,
    @SerializedName("modifiedTimeMillis") val modifiedTimeMillis: Long,
    @SerializedName("rawTimestampNanos") val rawTimestampNanos: Long
)

data class FitValue(
    @SerializedName("value") val value: Value
)

data class Value(
    @SerializedName("intVal") val intVal: Int
)