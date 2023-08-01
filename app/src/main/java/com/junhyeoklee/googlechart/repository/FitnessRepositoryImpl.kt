package com.atilmohamine.fitnesstracker.repository

import android.content.Context
import android.util.Log
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
import com.google.gson.Gson
import com.junhyeoklee.googlechart.model.DistanceFitnessModel
import com.junhyeoklee.googlechart.model.HourFitnessModel
import com.junhyeoklee.googlechart.model.MinuteFitnessModel
import com.junhyeoklee.googlechart.model.MinuteSpeedFitnessModel
import com.junhyeoklee.googlechart.model.SecondFitnessModel
import com.junhyeoklee.googlechart.model.TakeOutDistanceModel
import com.junhyeoklee.googlechart.model.TakeOutSpeedModel
import com.junhyeoklee.googlechart.model.TakeOutStepModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

    private val kstZoneId = ZoneId.of("Asia/Seoul")

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
            .aggregate(DataType.TYPE_MOVE_MINUTES, DataType.AGGREGATE_MOVE_MINUTES)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { data ->

                val buckets = data.buckets
                val bucket = if (buckets.isNotEmpty()) buckets[0] else null
                var stepCount = 0
                var calories = 0
                var distance = 0.0f
                var active = 0.0f

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
                            DataType.TYPE_MOVE_MINUTES -> {
                                active = dataPoint.getValue(Field.FIELD_DURATION).asInt().toFloat()
                            }
                        }
                    }
                }
                val dailyFitness = DailyFitnessModel(stepCount, calories, distance,active)
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
                    var active = 0.0f
                    var speed = 0.0f
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
                                DataType.TYPE_MOVE_MINUTES -> {
                                    active = dataPoint.getValue(Field.FIELD_DURATION).asInt().toFloat()
                                }
                                DataType.TYPE_SPEED ->{
                                    speed = dataPoint.getValue(Field.FIELD_SPEED).asFloat()
                                }
                            }
                        }
                    }

                    val dailyFitness = DailyFitnessModel(stepCount, calories, distance,active)
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

    override fun getMinuteSpeedFitnessData(context: Context): MutableLiveData<List<MinuteSpeedFitnessModel>> {
        val minuteSpeedFitnessLiveData = MutableLiveData<List<MinuteSpeedFitnessModel>>()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1) // 어제의 날짜로 설정
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 26)
        val startTime = calendar.timeInMillis

        calendar.set(Calendar.MINUTE, 38)
        val endTime = calendar.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
            .bucketByTime(1, TimeUnit.SECONDS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { data ->
                if (data.status.isSuccess) {
                    val minuteSpeedFitnessList = mutableListOf<MinuteSpeedFitnessModel>()
                    val buckets = data.buckets
                    for (bucket in buckets) {
                        for (dataSet in bucket.dataSets) {
                            for (dataPoint in dataSet.dataPoints) {
                                val speed = dataPoint.getValue(Field.FIELD_AVERAGE).asFloat()
                                val pointStartTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                                val pointEndTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS)
                                val timeInSeconds = (pointEndTime - pointStartTime) / 1000 // 초로 변환
                                val timeInMinutes = timeInSeconds / 60 // 분으로 변환
                                val speedPerMinute = speed * 60 // 분당 평균 속도 (미터/분)
                                val distanceInKm = speedPerMinute * timeInSeconds / 1000 // 이동 거리 (km)
                                val kilometers = (speed / 1000) * 60 // (키로미터/분)
                                val minute_of_kilometer = 1 / kilometers // 1키로미터당 걸리는 분
                                val second_minute_of_kilometer = (minute_of_kilometer - minute_of_kilometer.toInt()) * 60 // 초 단위로 변환된 분의 소수 부분을 분 단위로 변환
                                val seconds = Math.round(second_minute_of_kilometer).toInt() // 초 데이터를 반올림하여 정수로 변환

//                                val finalValue = minute_of_kilometer.toInt()""+":"+seconds

//                                val minutes = timeInSeconds / 60 // 분 단위
//                                val seconds = timeInSeconds % 60 // 초 단위


                                val startTimeFormatted = SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(pointStartTime))
                                val endTimeFormatted = SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(pointEndTime))
                                Log.d("DataPoint", "시작 시간: $startTimeFormatted, 종료 시간: $endTimeFormatted")

                                val TimeFormatted = SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(timeInSeconds))
                                Log.d("DataPoint2", "시간: $startTimeFormatted")

                                Log.d("TimePerOneKm", "미터/초: $speed")
                                Log.d("TimePerOneKm2", "미터/분): $speedPerMinute")
                                Log.d("TimePerOneKm3", "키로미터/분: $kilometers")
                                Log.d("스타트타임 ",""+pointStartTime)
                                Log.d("엔드타임 ",""+pointEndTime)


                                Log.e("KilometersPerMinute", "분당 평균 킬로미터:"+minute_of_kilometer.toInt()+":"+seconds)
                                Log.e("KilometersPerMinute", "분/km:"+minute_of_kilometer)

                                val minuteSpeedFitness = MinuteSpeedFitnessModel(pointStartTime, minute_of_kilometer)
                                minuteSpeedFitnessList.add(minuteSpeedFitness)
                            }
                        }
                    }
                    minuteSpeedFitnessLiveData.postValue(minuteSpeedFitnessList)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
            }

        return minuteSpeedFitnessLiveData
    }



    override fun getDistanceFitnessData(context: Context): MutableLiveData<List<DistanceFitnessModel>> {
        val distanceFitnessLiveData = MutableLiveData<List<DistanceFitnessModel>>()

        val endTime = System.currentTimeMillis()
        val startTime = getStartOfToday()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .bucketByTime(1, TimeUnit.MINUTES)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { data ->
                if (data.status.isSuccess) {
                    val distanceFitnessList = mutableListOf<DistanceFitnessModel>()
                    val buckets = data.buckets
                    for (bucket in buckets) {
                        for (dataSet in bucket.dataSets) {
                            for (dataPoint in dataSet.dataPoints) {
                                val distance = dataPoint.getValue(Field.FIELD_DISTANCE).asFloat()
                                val time = dataPoint.getEndTime(TimeUnit.MILLISECONDS) - dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                                val timeInMinutes = time / (1000f * 60) // 시간을 분 단위로 변환
                                val averageSpeed2 = distance / timeInMinutes // 분당 평균속력 계산
                                val averageSpeed = distance / time // 분당 평균속력 계산
                                val timePerKilometer = 1 / (averageSpeed2 / distance) // 1km당 걸리는 시간 (분/km)
                                val minutes = timePerKilometer.toInt() // 분
                                val seconds = ((timePerKilometer - minutes) * 60).toInt() // 초
                                val formattedTime = String.format("%d:%02d", minutes, seconds) // 분:초 형식으로 변환된 시간
                                Log.e("평균속도 = ",""+averageSpeed2)
                                Log.e("평균속도2 = ",""+formattedTime)

                                distanceFitnessList.add(DistanceFitnessModel(dataPoint.getStartTime(TimeUnit.MINUTES), averageSpeed))
                            }
                        }
                    }
                    distanceFitnessLiveData.postValue(distanceFitnessList)
                }
            }
            .addOnFailureListener { exception ->
                // 오류 처리
            }

//        val endTime = System.currentTimeMillis()
//        val startTime = getStartOfToday()

//        val readRequest = DataReadRequest.Builder()
//            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
//            .bucketByTime(1, TimeUnit.MINUTES)
//            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
//            .build()
//
//        Fitness.getHistoryClient(context, getGoogleAccount(context))
//            .readData(readRequest)
//            .addOnSuccessListener { data ->
//                if (data.status.isSuccess) {
//                    val distanceFitnessList = mutableListOf<DistanceFitnessModel>()
//                    val buckets = data.buckets
//                    for (bucket in buckets) {
//                        for (dataSet in bucket.dataSets) {
//                            for (dataPoint in dataSet.dataPoints) {
//                                val distance = dataPoint.getValue(Field.FIELD_DISTANCE).asFloat()
//                                val time = dataPoint.getEndTime(TimeUnit.MILLISECONDS) - dataPoint.getStartTime(TimeUnit.MILLISECONDS)
//                                val timeInMinutes = time / (1000f * 60) // 시간을 분 단위로 변환
//                                val speedPerKilometer = timeInMinutes / (distance / 1000) // km당 걸리는 시간 계산 (분 단위)
//                                distanceFitnessList.add(DistanceFitnessModel(dataPoint.getStartTime(TimeUnit.MINUTES), speedPerKilometer))
////                                Log.e("Time = ",""+time)
//                                Log.e("평균속력 = ",""+(timeInMinutes / distance) * 1000)
//                                Log.e("distance = ",""+distance.toString())
////                                Log.e("speedPerKilometer = ",""+ speedPerKilometer.toString())
////                                Log.e("distanceList = ",""+distanceFitnessList.toString())
//                            }
//                        }
//                    }
//                    distanceFitnessLiveData.postValue(distanceFitnessList)
//                }
//            }
//            .addOnFailureListener { exception ->
//                // Handle error
//            }

        return distanceFitnessLiveData
    }

    override fun getTakeOutStepData(context: Context): MutableLiveData<Int> {
        val targetStartYear = 2023
        val targetStartMonth = 5
        val targetStartDay = 18
        val targetStartHour = 16
        val targetStartMinute = 11

        val targetEndYear = 2023
        val targetEndMonth = 5
        val targetEndDay = 18
        val targetEndHour = 16
        val targetEndMinute = 26

        val targetStartAllTime = "$targetStartYear${targetStartMonth.toString().padStart(2, '0')}${targetStartDay.toString().padStart(2, '0')}${targetStartHour.toString().padStart(2, '0')}${targetStartMinute.toString().padStart(2, '0')}".toLong()
        val targetEndAllTime = "$targetEndYear${targetEndMonth.toString().padStart(2, '0')}${targetEndDay.toString().padStart(2, '0')}${targetEndHour.toString().padStart(2, '0')}${targetEndMinute.toString().padStart(2, '0')}".toLong()


        val takeOutStepLiveData = MutableLiveData<TakeOutStepModel>()
        val takeOutStepList = mutableListOf<TakeOutStepModel>()
        val totalStepCount = MutableLiveData<Int>()
        var totalSteps = 0
        // Json 파일의 데이터 분단위 출력

        val assetManager = context.assets
        val jsonInputStream = assetManager.open("step3.json")
        val json = jsonInputStream.bufferedReader().use { it.readText() }

        val gson = Gson()
        val stepCountData = gson.fromJson(json, TakeOutStepModel::class.java)


        for (step in stepCountData.dataPoints) {
            val startTime = step.startTimeNanos
            val endTime = step.endTimeNanos

//            val startTimeMinutes = TimeUnit.NANOSECONDS.toMinutes(startTime)
//            val endTimeMinutes = TimeUnit.NANOSECONDS.toMinutes(endTime)
//            val durationMinutes = endTimeMinutes - startTimeMinutes
//            val stepsPerMinute = if (durationMinutes != 0L) {
//                stepCount.toDouble() / durationMinutes
//            } else {
//                0.0
//            }
            val startDateTimeKST = Instant.ofEpochMilli(startTime / 1000000).atZone(kstZoneId)
            val endDateTimeKST = Instant.ofEpochMilli(endTime / 1000000).atZone(kstZoneId)

            val startYear = startDateTimeKST.year
            val startMonth = startDateTimeKST.monthValue
            val startDay = startDateTimeKST.dayOfMonth
            val startHour = startDateTimeKST.hour
            val startMinute = startDateTimeKST.minute

            val endYear = endDateTimeKST.year
            val endMonth = endDateTimeKST.monthValue
            val endDay = endDateTimeKST.dayOfMonth
            val endHour = endDateTimeKST.hour
            val endMinute = endDateTimeKST.minute

            val startAllTime = "$startYear${startMonth.toString().padStart(2, '0')}${startDay.toString().padStart(2, '0')}${startHour.toString().padStart(2, '0')}${startMinute.toString().padStart(2, '0')}".toLong()
            val endAllTime = "$endYear${endMonth.toString().padStart(2, '0')}${endDay.toString().padStart(2, '0')}${endHour.toString().padStart(2, '0')}${endMinute.toString().padStart(2, '0')}".toLong()

            // 특정시간 범위에 해당하는 스텝 수 추출
            if (startAllTime >= targetStartAllTime && endAllTime <= targetEndAllTime) {
                val stepCount = step.fitValue[0].value.intVal
                totalSteps += stepCount
//                Log.e("StepCountData", "Step Count: $stepCount")

//                Log.e("StepCountData", "Data Name: $dataName")
//                Log.e("StepCountData", "Step Count: $stepCount")
//                Log.e("StepCountData", "startTimeNanos : $startYear-$startMonth-$startDay $startHour:$startMinute")
//                Log.e("StepCountData", "endTimeNanos : $endYear-$endMonth-$endDay $endHour:$endMinute")
//                Log.e("startTargetTime","targetStartTime : $targetStartTime")
//                Log.e("startEndTime","targetEndTime : $targetEndTime")
//                Log.e("StepCountData", "분 차이: $durationMinutes")
//                Log.e("StepCountData", "1분으로 나눈값 : $stepsPerMinute")
            }

        }

        totalStepCount.value = totalSteps
        Log.e("총합 스텝 ","총 스텝: $totalSteps")

        return totalStepCount
    }

    override fun getTakeOutSpeedData(context: Context): MutableLiveData<Double> {
        val takeOutSpeedLiveData = MutableLiveData<TakeOutSpeedModel>()
        val targetStartYear = 2023
        val targetStartMonth = 5
        val targetStartDay = 18
        val targetStartHour = 16
        val targetStartMinute = 11

        val targetEndYear = 2023
        val targetEndMonth = 5
        val targetEndDay = 18
        val targetEndHour = 16
        val targetEndMinute = 26
        val targetStartAllTime = "$targetStartYear${targetStartMonth.toString().padStart(2, '0')}${targetStartDay.toString().padStart(2, '0')}${targetStartHour.toString().padStart(2, '0')}${targetStartMinute.toString().padStart(2, '0')}".toLong()
        val targetEndAllTime = "$targetEndYear${targetEndMonth.toString().padStart(2, '0')}${targetEndDay.toString().padStart(2, '0')}${targetEndHour.toString().padStart(2, '0')}${targetEndMinute.toString().padStart(2, '0')}".toLong()


//        val takeOutSpeedList = mutableListOf<TakeOutSpeedModel>()
        val totalSpeedCount = MutableLiveData<Double>()
        var totalSpeed = 0.0
        var count = 0
        // Json 파일의 데이터 분단위 출력

        val assetManager = context.assets
        val jsonInputStream = assetManager.open("speed2.json")
        val json = jsonInputStream.bufferedReader().use { it.readText() }

        val gson = Gson()
        val speedCountData = gson.fromJson(json, TakeOutSpeedModel::class.java)

        for (speed in speedCountData.dataPoints) {


            val startTime = speed.startTimeNanos
            val endTime = speed.endTimeNanos
            val dataName = speed.dataTypeName

            val startTimeMinutes = TimeUnit.NANOSECONDS.toMinutes(startTime)
            val endTimeMinutes = TimeUnit.NANOSECONDS.toMinutes(endTime)
            val durationMinutes = endTimeMinutes - startTimeMinutes

            val startDateTimeKST = Instant.ofEpochMilli(startTime / 1000000).atZone(kstZoneId)
            val endDateTimeKST = Instant.ofEpochMilli(endTime / 1000000).atZone(kstZoneId)

            val startYear = startDateTimeKST.year
            val startMonth = startDateTimeKST.monthValue
            val startDay = startDateTimeKST.dayOfMonth
            val startHour = startDateTimeKST.hour
            val startMinute = startDateTimeKST.minute

            val endYear = endDateTimeKST.year
            val endMonth = endDateTimeKST.monthValue
            val endDay = endDateTimeKST.dayOfMonth
            val endHour = endDateTimeKST.hour
            val endMinute = endDateTimeKST.minute

//            val speedPerMinute = if (durationMinutes != 0L) {
//                speedCount.toDouble() / durationMinutes
//            } else {
//                0.0
//            }

            val startAllTime = "$startYear${startMonth.toString().padStart(2, '0')}${startDay.toString().padStart(2, '0')}${startHour.toString().padStart(2, '0')}${startMinute.toString().padStart(2, '0')}".toLong()
            val endAllTime = "$endYear${endMonth.toString().padStart(2, '0')}${endDay.toString().padStart(2, '0')}${endHour.toString().padStart(2, '0')}${endMinute.toString().padStart(2, '0')}".toLong()

            // 특정시간 범위에 해당하는 스텝 수 추출
            if (startAllTime >= targetStartAllTime && endAllTime <= targetEndAllTime) {
                val speedCount = speed.fitValue[0].value.fpVal
                totalSpeed += speedCount
                count++


//                Log.e("SpeedCountData", "Data Name: $dataName")
                Log.e("SpeedCountData", "Seed Count: $speedCount")
                Log.e("SpeedCountData", "startTimeNanos : $startYear-$startMonth-$startDay $startHour:$startMinute")
                Log.e("SpeedCountData", "endTimeNanos : $endYear-$endMonth-$endDay $endHour:$endMinute")
//                Log.e("SpeedCountData", "분 차이: $durationMinutes")
//                Log.e("SpeedCountData", "1분으로 나눈값 : $speedPerMinute")
            }

            val takeOutSpeed = TakeOutSpeedModel(speedCountData.dataSource,speedCountData.dataPoints)
            takeOutSpeedLiveData.postValue(takeOutSpeed)

        }
//        val averageSpeedInMps = if (count > 0) totalSpeed / count else 0.0
//        val averageSpeedInKmph = averageSpeedInMps * 3.6
//        val averageSpeedPerKm = averageSpeedInKmph / totalDistanceInKm
//        var totalAverageSpeed = (totalSpeed / count)* 3.6
//
//        var totalAverageSpeed = (totalSpeed / count)* 3.6
        Log.e("총합 속도 ","총 속도: $totalSpeed")

        totalSpeedCount.value = totalSpeed

        return totalSpeedCount
    }

    override fun getTakeOutDistanceData(context: Context): MutableLiveData<Double> {
        val takeOutDistanceLiveData = MutableLiveData<TakeOutDistanceModel>()
        val targetStartYear = 2023
        val targetStartMonth = 5
        val targetStartDay = 18
        val targetStartHour = 16
        val targetStartMinute = 11

        val targetEndYear = 2023
        val targetEndMonth = 5
        val targetEndDay = 18
        val targetEndHour = 16
        val targetEndMinute = 26

        val targetStartAllTime = "$targetStartYear${targetStartMonth.toString().padStart(2, '0')}${targetStartDay.toString().padStart(2, '0')}${targetStartHour.toString().padStart(2, '0')}${targetStartMinute.toString().padStart(2, '0')}".toLong()
        val targetEndAllTime = "$targetEndYear${targetEndMonth.toString().padStart(2, '0')}${targetEndDay.toString().padStart(2, '0')}${targetEndHour.toString().padStart(2, '0')}${targetEndMinute.toString().padStart(2, '0')}".toLong()

        val totalDistanceCount = MutableLiveData<Double>()
        var totalDistance = 0.0
        var count = 0
        // Json 파일의 데이터 분단위 출력

        val assetManager = context.assets
        val jsonInputStream = assetManager.open("distance.json")
        val json = jsonInputStream.bufferedReader().use { it.readText() }

        val gson = Gson()
        val distanceCountData = gson.fromJson(json, TakeOutDistanceModel::class.java)

        for (distance in distanceCountData.dataPoints) {

            val startTime = distance.startTimeNanos
            val endTime = distance.endTimeNanos
            val dataName = distance.dataTypeName

            val startTimeMinutes = TimeUnit.NANOSECONDS.toMinutes(startTime)
            val endTimeMinutes = TimeUnit.NANOSECONDS.toMinutes(endTime)
            val durationMinutes = endTimeMinutes - startTimeMinutes

            val startDateTimeKST = Instant.ofEpochMilli(startTime / 1000000).atZone(kstZoneId)
            val endDateTimeKST = Instant.ofEpochMilli(endTime / 1000000).atZone(kstZoneId)

            val startYear = startDateTimeKST.year
            val startMonth = startDateTimeKST.monthValue
            val startDay = startDateTimeKST.dayOfMonth
            val startHour = startDateTimeKST.hour
            val startMinute = startDateTimeKST.minute

            val endYear = endDateTimeKST.year
            val endMonth = endDateTimeKST.monthValue
            val endDay = endDateTimeKST.dayOfMonth
            val endHour = endDateTimeKST.hour
            val endMinute = endDateTimeKST.minute

            val startAllTime = "$startYear${startMonth.toString().padStart(2, '0')}${startDay.toString().padStart(2, '0')}${startHour.toString().padStart(2, '0')}${startMinute.toString().padStart(2, '0')}".toLong()
            val endAllTime = "$endYear${endMonth.toString().padStart(2, '0')}${endDay.toString().padStart(2, '0')}${endHour.toString().padStart(2, '0')}${endMinute.toString().padStart(2, '0')}".toLong()

            // 특정시간 범위에 해당하는 스텝 수 추출
            if (startAllTime >= targetStartAllTime && endAllTime <= targetEndAllTime) {
                val distanceCount = distance.fitValue[0].value.fpVal
                totalDistance += distanceCount
                count++


//                Log.e("SpeedCountData", "Data Name: $dataName")
//                Log.e("DistanceCountData", "Distance Count: $distanceCount")
//                Log.e("DistanceCountData", "startTimeNanos : $startYear-$startMonth-$startDay $startHour:$startMinute")
//                Log.e("DistanceCountData", "endTimeNanos : $endYear-$endMonth-$endDay $endHour:$endMinute")
//                Log.e("SpeedCountData", "분 차이: $durationMinutes")
//                Log.e("SpeedCountData", "1분으로 나눈값 : $speedPerMinute")
            }

            val takeOutDistance = TakeOutDistanceModel(distanceCountData.dataSource,distanceCountData.dataPoints)
            takeOutDistanceLiveData.postValue(takeOutDistance)

        }
//        val averageSpeedInMps = if (count > 0) totalSpeed / count else 0.0
//        val averageSpeedInKmph = averageSpeedInMps * 3.6
//        val averageSpeedPerKm = averageSpeedInKmph / totalDistanceInKm
//        var totalAverageSpeed = (totalSpeed / count)* 3.6
//        var totalAverageSpeed = (totalSpeed / count)* 3.6
        totalDistanceCount.value = totalDistance
//        Log.e("총합 거리 ","총 거리: $totalDistance")

        return totalDistanceCount
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