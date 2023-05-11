package com.atilmohamine.fitnesstracker.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.atilmohamine.fitnesstracker.model.DailyFitnessModel
import com.atilmohamine.fitnesstracker.model.WeeklyFitnessModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.junhyeoklee.googlechart.model.HourFitnessModel
import com.junhyeoklee.googlechart.model.MinuteFitnessModel
import com.junhyeoklee.googlechart.model.SecondFitnessModel
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit


class FitnessRepositoryImpl(): FitnessRepository {
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    override fun getSecondFitnessData(context: Context): MutableLiveData<List<SecondFitnessModel>> {
        val secondFitnessLiveData= MutableLiveData<List<SecondFitnessModel>>()

        val endTime = System.currentTimeMillis()
        val startTime = getStartOfToday()


        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.MINUTES)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { response ->
                // 데이터를 가져온 후에 초 단위로 변환하여 사용
                val secondFitnessList = mutableListOf<SecondFitnessModel>()
                response.buckets.forEach { bucket ->

                    val startMinute = TimeUnit.MILLISECONDS.toMinutes(bucket.getStartTime(TimeUnit.MILLISECONDS))
                    val endMinute = TimeUnit.MILLISECONDS.toMinutes(bucket.getEndTime(TimeUnit.MILLISECONDS))

                    for (minute in startMinute until endMinute) {
                        bucket.dataSets.forEach { dataSet ->
                            dataSet.dataPoints.forEach { dataPoint ->
                                val stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt()

                                val startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                                val endTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS)
                                val durationInMillis = endTime - startTime

                                val numSeconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis)
                                val stepCountPerSecond = stepCount / numSeconds

                                for (secondOffset in 0 until numSeconds) {
                                    val second = TimeUnit.MINUTES.toSeconds(minute) + secondOffset
                                    val secondFitness = SecondFitnessModel(second, stepCountPerSecond.toInt())
                                    secondFitnessList.add(secondFitness)
                                }
                            }
                        }
                    }
                }

//                val startMinute = TimeUnit.MILLISECONDS.toMinutes(bucket.getStartTime(TimeUnit.MILLISECONDS))
//                    val endMinute = TimeUnit.MILLISECONDS.toMinutes(bucket.getEndTime(TimeUnit.MILLISECONDS))
//
//                    for (minute in startMinute until endMinute) {
//                        bucket.dataSets.forEach { dataSet ->
//                            dataSet.dataPoints.forEach { dataPoint ->
//                                val stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt()
//                                val startSecond = TimeUnit.MINUTES.toSeconds(minute)
//                                val endSecond = TimeUnit.MINUTES.toSeconds(minute + 1)
//
//                                for (second in startSecond until endSecond) {
//                                    val secondFitness = SecondFitnessModel(second, stepCount)
//                                    secondFitnessList.add(secondFitness)
//                                }
//                            }
//                        }
//                    }
//                }

                secondFitnessLiveData.postValue(secondFitnessList)
            }
            .addOnFailureListener { exception ->
                // 실패 처리
                Log.e("초단위 실패", exception.toString())
            }
        return secondFitnessLiveData
    }

    override fun getMinuteFitnessData(context: Context): MutableLiveData<List<MinuteFitnessModel>> {
        val minuteFitnessLiveData= MutableLiveData<List<MinuteFitnessModel>>()

        val endTime = System.currentTimeMillis()
        val startTime = getStartOfToday()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.MINUTES)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { data ->

                if (data.status.isSuccess) {
                    val minuteFitnessList = mutableListOf<MinuteFitnessModel>()
                    val buckets = data.buckets

                    for (bucket in buckets) {
                        for (dataSet in bucket.dataSets) {
                            for (dataPoint in dataSet.dataPoints) {
                                val stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                                val startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                                val minuteFitness = MinuteFitnessModel(startTime, stepCount)
                                minuteFitnessList.add(minuteFitness)
                            }
                        }
                    }

                    minuteFitnessLiveData.postValue(minuteFitnessList)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
            }


        return minuteFitnessLiveData
    }

    override fun getHourFitnessData(context: Context): MutableLiveData<List<HourFitnessModel>> {
        val hourFitnessLiveData= MutableLiveData<List<HourFitnessModel>>()

        val endTime = System.currentTimeMillis()
        val startTime = getStartOfToday()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.HOURS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { data ->
                if (data.status.isSuccess) {
                    val hourFitnessList = mutableListOf<HourFitnessModel>()
                    val buckets = data.buckets

                    for (bucket in buckets) {
                        for (dataSet in bucket.dataSets) {
                            for (dataPoint in dataSet.dataPoints) {
                                val stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                                val startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                                val hourFitness = HourFitnessModel(startTime, stepCount)
                                hourFitnessList.add(hourFitness)
                            }
                        }
                    }

                    hourFitnessLiveData.postValue(hourFitnessList)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
            }

        return hourFitnessLiveData
    }

    override fun getDailyFitnessData(context: Context): MutableLiveData<DailyFitnessModel> {
        val dailyFitnessLiveData = MutableLiveData<DailyFitnessModel>()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { data ->
                Log.e("ㅇㅇ", "성공 subscribed!")

                val buckets = data.buckets
                val bucket = if (buckets.isNotEmpty()) buckets[0] else null
                var stepCount = 0
                var calories = 0
                var distance = 0.0f

                bucket?.dataSets?.forEach { dataSet ->
                    dataSet.dataPoints.forEach { dataPoint ->
                        when (dataPoint.dataType) {
                            DataType.TYPE_STEP_COUNT_DELTA -> {
                                stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                            }
                            DataType.TYPE_CALORIES_EXPENDED -> {
                                calories = dataPoint.getValue(Field.FIELD_CALORIES).asFloat().toInt() / 1000
                            }
                            DataType.TYPE_DISTANCE_DELTA -> {
                                distance = dataPoint.getValue(Field.FIELD_DISTANCE).asFloat() / 1000
                            }
                        }
                    }
                }
                val dailyFitness = DailyFitnessModel(stepCount, calories, distance)
                dailyFitnessLiveData.postValue(dailyFitness)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }



        return dailyFitnessLiveData
    }

    override fun getWeeklyFitnessData(context: Context): MutableLiveData<WeeklyFitnessModel> {
        val weeklyFitnessLiveData = MutableLiveData<WeeklyFitnessModel>()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis

        // Build data read request for the last 7 days
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { data ->
                val buckets = data.buckets
                val dailyFitnessList = mutableListOf<DailyFitnessModel>()

                // Process each bucket of fitness data
                buckets.forEach { bucket ->
                    var stepCount = 0
                    var calories = 0
                    var distance = 0.0f

                    bucket.dataSets.forEach { dataSet ->
                        dataSet.dataPoints.forEach { dataPoint ->
                            when (dataPoint.dataType) {
                                DataType.TYPE_STEP_COUNT_DELTA -> {
                                    stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                                }
                                DataType.TYPE_CALORIES_EXPENDED -> {
                                    calories = dataPoint.getValue(Field.FIELD_CALORIES).asFloat().toInt() / 1000
                                }
                                DataType.TYPE_DISTANCE_DELTA -> {
                                    distance = dataPoint.getValue(Field.FIELD_DISTANCE).asFloat() / 1000
                                }
                            }
                        }
                    }

                    val dailyFitness = DailyFitnessModel(stepCount, calories, distance)
                    dailyFitnessList.add(dailyFitness)
                }

                val weeklyFitness = WeeklyFitnessModel(dailyFitnessList)
                weeklyFitnessLiveData.postValue(weeklyFitness)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }

        return weeklyFitnessLiveData
    }

    override fun getGoogleAccount(context: Context): GoogleSignInAccount = GoogleSignIn.getAccountForExtension(context, fitnessOptions)


    private fun readStepCountData(context:Context) {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, -24) // Read step count data for the past 24 hours
        val startTime = calendar.timeInMillis


        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByTime(10, TimeUnit.MINUTES) // Group data into 10-minute buckets
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { response ->
                // Process the step count data
                processStepCountData(response)
            }
            .addOnFailureListener { exception ->
                // Handle the failure to read step count data
            }

        val stepCountList = MutableList(24 * 60) { 0 } // 24시간 * 60분 = 1440개의 분 단위 리스트 생성

        for (minuteOfDay in 0 until 24 * 60) {
                    val stepCount = stepCountList[minuteOfDay]
                    val hour = minuteOfDay / 60
                    val minute = minuteOfDay % 60
                    println("Minute of day: $minuteOfDay, Step count: $stepCount")
                    println("%02d:%02d, 걸음 count: %d".format(hour, minute, stepCount))
                }
    }
    private fun processStepCountData(dataReadResponse: DataReadResponse) {
        for (bucket in dataReadResponse.getBuckets()) {
            val dataSets = bucket.getDataSets()

            for (dataSet in dataSets) {
                for (dataPoint in dataSet.getDataPoints()) {
                    for (field in dataPoint.getDataType().getFields()) {
                        if (field.getName().equals(Field.FIELD_STEPS.getName())) {
                            val stepCount = dataPoint.getValue(field).asInt()
                            val startTime = dataPoint.getStartTime(TimeUnit.MINUTES)
                            val endTime = dataPoint.getEndTime(TimeUnit.MINUTES)

                            // Display the step count data in 10-minute increments
                            println("Step count from $startTime to $endTime: $stepCount")
                        }
                    }
                }
            }
        }
    }
    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

}