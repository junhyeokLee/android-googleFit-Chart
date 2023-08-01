package com.junhyeoklee.googlechart.model

import com.google.gson.annotations.SerializedName

data class TakeOutStepModel(@SerializedName("Data Source") val dataSource: String,
                     @SerializedName("Data Points") val dataPoints: List<StepDataPoint>
                      )

data class StepDataPoint(
    @SerializedName("fitValue") val fitValue: List<StepFitValue>,
    @SerializedName("originDataSourceId") val originDataSourceId: String,
    @SerializedName("endTimeNanos") val endTimeNanos: Long,
    @SerializedName("dataTypeName") val dataTypeName: String,
    @SerializedName("startTimeNanos") val startTimeNanos: Long,
    @SerializedName("modifiedTimeMillis") val modifiedTimeMillis: Long,
    @SerializedName("rawTimestampNanos") val rawTimestampNanos: Long
)

data class StepFitValue(
    @SerializedName("value") val value: StepValue
)

data class StepValue(
    @SerializedName("intVal") val intVal: Int
)