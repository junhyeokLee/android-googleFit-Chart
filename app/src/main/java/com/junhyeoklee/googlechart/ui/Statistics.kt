package com.junhyeoklee.googlechart.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.atilmohamine.fitnesstracker.model.WeeklyFitnessModel
import com.atilmohamine.fitnesstracker.utils.DayAxisValueFormatter
import com.atilmohamine.fitnesstracker.utils.HourAxisValueFormatter
import com.atilmohamine.fitnesstracker.utils.MinuteAxisValueFormatter
import com.atilmohamine.fitnesstracker.utils.SecondAxisValueFormatter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.junhyeoklee.googlechart.R
import com.junhyeoklee.googlechart.databinding.FragmentStatisticsBinding
import com.junhyeoklee.googlechart.model.DistanceFitnessModel
import com.junhyeoklee.googlechart.model.HourFitnessModel
import com.junhyeoklee.googlechart.model.MinuteFitnessModel
import com.junhyeoklee.googlechart.model.MinuteSpeedFitnessModel
import com.junhyeoklee.googlechart.model.SecondFitnessModel
import com.junhyeoklee.googlechart.viewmodel.FitnessViewModel


class Statistics : Fragment(R.layout.fragment_statistics) {

    private lateinit var weekChart: BarChart // 주단위 차트
    private lateinit var hourkChart: BarChart // 시간 단위 차트
    private lateinit var minuteChart: BarChart // 분단위 차트
    private lateinit var secondChart: BarChart // 초단위 차트
    private lateinit var speedChart: LineChart // 속도 라인차트
    private lateinit var averageSpeedChart: LineChart // 평균속도 라인차트
    private lateinit var average_speed: TextView // 평균 스피드
    private var isDragging = false
    private val fitnessViewModel: FitnessViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentStatisticsBinding.bind(view)

        binding.apply {
            this@Statistics.weekChart = this.weekChart
            this@Statistics.hourkChart = this.hourChart
            this@Statistics.minuteChart = this.minuteChart
            this@Statistics.secondChart = this.secondChart
            this@Statistics.speedChart = this.speedChart
            this@Statistics.averageSpeedChart = this.averageSpeedChart
            this@Statistics.average_speed = this.averageSpeed
        }

        fitnessViewModel.getWeeklyFitnessData(requireContext())
            .observe(viewLifecycleOwner, Observer { WeeklyFitness ->
                loadWeekChart(WeeklyFitness) // 주단위 차트를 로드 주 단위로 칼로리, 걸음 수, 거리에 대한 데이터를 바차트로 표시
            })
        fitnessViewModel.getHourFitnessData(requireContext())
            .observe(viewLifecycleOwner, Observer { HourFitness ->
                loadHourChart(HourFitness) // 시간 단위 차트를 로드 시간 단위로 걸음 수에 대한 데이터를 바차트로 표시
            })
        fitnessViewModel.getMinuteFitnessData(requireContext())
            .observe(viewLifecycleOwner, Observer { MinuteFitness ->
                loadMinuteChart(MinuteFitness) // 분단위 차트를 로드 분 단위로 걸음 수에 대한 데이터를 바차트로 표시

            })
        fitnessViewModel.getSecondFitnessData(requireContext()).observe(viewLifecycleOwner, Observer { SecondFitness->
            loadSecondChart(SecondFitness) // 단위 차트를 로드 초 단위로 걸음 수에 대한 데이터를 바차트로 표시
        })

        fitnessViewModel.getMinuteSpeedFitnessData(requireContext())
            .observe(viewLifecycleOwner, Observer { MinuteSpeedFitness ->
                loadMinuteSpeedChart(MinuteSpeedFitness) // 분단위 속도 라인차트를 로드 분 단위로 속도에 대한 데이터를 라인차트로 표시
            })

        fitnessViewModel.getDistanceFitnessData(requireContext())
            .observe(viewLifecycleOwner, Observer { DistanceFitness ->
                loadAverageSpeedChart(DistanceFitness) // 평균 스피드차트를 로드
            })

    }

    private fun loadWeekChart(WeeklyFitness: WeeklyFitnessModel) {
        weekChart.description.isEnabled = false
        weekChart.setTouchEnabled(false)
        weekChart.setDrawGridBackground(false)

        val caloriesDataSet = BarDataSet(mutableListOf(), "Calories") // 칼로리에 대한 데이터셋 생성
        val stepsDataSet = BarDataSet(mutableListOf(), "Steps") // 걸음 수에 대한 데이터셋 생성
        stepsDataSet.color = ContextCompat.getColor(requireContext(), R.color.calories) // 걸음 수 데이터셋의 색상 설정
        stepsDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white) // 걸음 수 데이터 값의 텍스트 색상 설정
        val distanceDataSet = LineDataSet(mutableListOf(), "Distance") // 거리에 대한 데이터셋 생성 (선 그래프)

        WeeklyFitness.dailyFitnessList.forEachIndexed { index, fitnessData ->
            val caloriesEntry = BarEntry(index.toFloat(), fitnessData.caloriesBurned.toFloat()) // 칼로리 엔트리 생성
            val stepsEntry = BarEntry(index.toFloat(), fitnessData.stepCount.toFloat()) // 걸음 수 엔트리 생성
            val distanceEntry = BarEntry(index.toFloat(), fitnessData.distance) // 거리 엔트리 생성

            caloriesDataSet.addEntry(caloriesEntry) // 칼로리 데이터셋에 엔트리 추가
            stepsDataSet.addEntry(stepsEntry) // 걸음 수 데이터셋에 엔트리 추가
            distanceDataSet.addEntry(distanceEntry) // 거리 데이터셋에 엔트리 추가
        }

        val data = BarData()
        data.addDataSet(caloriesDataSet)
        data.addDataSet(stepsDataSet)
        // data.addDataSet(distanceDataSet)

        weekChart.data = data

        val xAxis = weekChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        xAxis.valueFormatter = DayAxisValueFormatter(weekChart) // X축 값 포맷터로 DayAxisValueFormatter를 사용하여 일자 형식으로 표시

        val yAxisLeft = weekChart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.isEnabled = false
        yAxisLeft.axisMinimum = 0f

        val yAxisRight = weekChart.axisRight
        yAxisRight.isEnabled = false
        yAxisLeft.setDrawGridLines(false)

        val barData = weekChart.barData
        barData.barWidth = 0.6f

        weekChart.legend.isEnabled = false

        weekChart.notifyDataSetChanged()
        weekChart.invalidate()
    }


    // 시간별 피트니스 데이터를 로드하고 그에 따라 시간별 차트를 초기화 및 업데이트하는 함수
    private fun loadHourChart(HourFitness: List<HourFitnessModel>) {
        hourkChart.description.isEnabled = false
        hourkChart.setNoDataText("No Data") // 데이터가 없을 경우 표시할 텍스트 설정
        hourkChart.setTouchEnabled(false)
        hourkChart.setDrawGridBackground(false)

        val stepsDataSet = BarDataSet(mutableListOf(), "Steps") // "Steps"를 표시하는 막대 그래프 데이터셋
        stepsDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white) // 텍스트 색상 설정

        HourFitness.forEachIndexed { index, hourFitnessModel ->
            val stepsEntry = BarEntry(index.toFloat(), hourFitnessModel.stepCount.toFloat()) // 막대 그래프에 추가할 데이터 엔트리 생성
            stepsDataSet.addEntry(stepsEntry) // 데이터셋에 데이터 엔트리 추가
        }

        val data = BarData()
        data.addDataSet(stepsDataSet) // 데이터셋을 데이터에 추가
        hourkChart.data = data

        val xAxis = hourkChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM // X축 위치 설정
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white) // X축 텍스트 색상 설정
        xAxis.valueFormatter = HourAxisValueFormatter(hourkChart) // X축 값 포맷터 설정

        val yAxisLeft = hourkChart.axisLeft
        yAxisLeft.setDrawGridLines(false) // Y축 그리드 라인 그리지 않음
        yAxisLeft.isEnabled = false
        yAxisLeft.axisMinimum = 0f // Y축 최소값 설정

        val yAxisRight = hourkChart.axisRight
        yAxisRight.isEnabled = false
        yAxisLeft.setDrawGridLines(false)

        val barData = hourkChart.barData
        barData.barWidth = 0.6f // 막대 그래프의 너비 설정

        hourkChart.legend.isEnabled = false

        hourkChart.notifyDataSetChanged()
        hourkChart.invalidate()
    }



    private fun loadMinuteChart(MinuteFitness: List<MinuteFitnessModel>) {
        minuteChart.description.isEnabled = false
        minuteChart.setNoDataText("No Data.") // 데이터가 없을 때 표시할 텍스트 설정
        minuteChart.setTouchEnabled(false)
        minuteChart.setDrawGridBackground(false)

        val stepsDataSet = BarDataSet(mutableListOf(), "Steps") // "Steps"를 표시하는 막대 그래프 데이터셋
        stepsDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white) // 텍스트 색상 설정

        MinuteFitness.forEachIndexed { index, minuteFitnessModel ->
            val stepsEntry = BarEntry(index.toFloat(), minuteFitnessModel.stepCount.toFloat()) // 막대 그래프에 추가할 데이터 엔트리 생성
            stepsDataSet.addEntry(stepsEntry) // 데이터셋에 데이터 엔트리 추가
        }

        val data = BarData()
        data.addDataSet(stepsDataSet) // 데이터셋을 데이터에 추가
        minuteChart.data = data

        val xAxis = minuteChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM // X축 위치 설정
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white) // X축 텍스트 색상 설정
        xAxis.valueFormatter = MinuteAxisValueFormatter(minuteChart) // X축 값 포맷터 설정

        val yAxisLeft = minuteChart.axisLeft
        yAxisLeft.setDrawGridLines(false) // Y축 그리드 라인 그리지 않음
        yAxisLeft.isEnabled = false
        yAxisLeft.axisMinimum = 0f // Y축 최소값 설정

        val yAxisRight = minuteChart.axisRight
        yAxisRight.isEnabled = false
        yAxisLeft.setDrawGridLines(false)

        val barData = minuteChart.barData
        barData.barWidth = 0.6f // 막대 그래프의 너비 설정

        minuteChart.legend.isEnabled = false

        minuteChart.notifyDataSetChanged()
        minuteChart.invalidate()
    }


    private fun loadSecondChart(SecondFitness: List<SecondFitnessModel>) {
        // 차트 기본 설정
        secondChart.description.isEnabled = false
        secondChart.setTouchEnabled(false)
        secondChart.setDrawGridBackground(false)

        val stepsDataSet = BarDataSet(mutableListOf(), "Steps") // "Steps"를 표시하는 막대 그래프 데이터셋
        stepsDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white) // 텍스트 색상 설정

        SecondFitness.forEachIndexed { index, secondFitnessModel ->
            val stepsEntry = BarEntry(index.toFloat(), secondFitnessModel.stepCount.toFloat()) // 막대 그래프에 추가할 데이터 엔트리 생성
            stepsDataSet.addEntry(stepsEntry) // 데이터셋에 데이터 엔트리 추가
        }

        val data = BarData()
        data.addDataSet(stepsDataSet) // 데이터셋을 데이터에 추가
        secondChart.data = data

        // X축 설정
        val xAxis = secondChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM // X축 위치 설정
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white) // X축 텍스트 색상 설정
        xAxis.valueFormatter = SecondAxisValueFormatter(secondChart) // X축 값 포맷터 설정

        // Y축 설정
        val yAxisLeft = secondChart.axisLeft
        yAxisLeft.setDrawGridLines(false) // Y축 그리드 라인 그리지 않음
        yAxisLeft.isEnabled = false
        yAxisLeft.axisMinimum = 0f // Y축 최소값 설정

        val yAxisRight = secondChart.axisRight
        yAxisRight.isEnabled = false
        yAxisLeft.setDrawGridLines(false)

        val barData = secondChart.barData
        barData.barWidth = 0.6f // 막대 그래프의 너비 설정

        secondChart.legend.isEnabled = false

        secondChart.notifyDataSetChanged()
        secondChart.invalidate()
    }


    private fun loadMinuteSpeedChart(MinuteSpeedFitness: List<MinuteSpeedFitnessModel>) {
        // 차트 기본 설정
        speedChart.description.isEnabled = false
        speedChart.setNoDataText("No data.")
        speedChart.setDrawGridBackground(false)
        speedChart.setTouchEnabled(true)

        val speedDataSet = LineDataSet(mutableListOf(), "Speed") // "Speed"를 표시하는 선 그래프 데이터셋
        speedDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white) // 텍스트 색상 설정

        MinuteSpeedFitness.forEachIndexed { index, minuteSpeedFitnessModel ->
            val speed = minuteSpeedFitnessModel.speed.toFloat()
            val formattedSpeed = "${speed.toInt()}:${String.format("%02d", ((speed - speed.toInt()) * 60).toInt())}"
            val speedEntry = Entry(index.toFloat(), speed.toFloat()) // 선 그래프에 추가할 데이터 엔트리 생성
            speedEntry.data = formattedSpeed // 데이터 엔트리에 속성 추가
            speedDataSet.addEntry(speedEntry) // 데이터셋에 데이터 엔트리 추가
        }

        val data = LineData()
        data.addDataSet(speedDataSet) // 데이터셋을 데이터에 추가
        speedDataSet.color = Color.WHITE // 선 색상 설정
        speedDataSet.lineWidth = 2f // 선 굵기 설정
        speedDataSet.setCircleColor(Color.rgb(255, 255, 255)) // 원 색상 설정
        speedDataSet.circleRadius = 4f // 원 반지름 설정
        speedDataSet.setDrawCircleHole(false) // 원 내부 구멍 그리지 않음
        speedDataSet.valueTextSize = 10f // 텍스트 크기 설정
        speedDataSet.valueTextColor = Color.WHITE // 텍스트 색상 설정
        speedDataSet.setDrawValues(true) // 값 텍스트 표시 설정
        speedDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 선 그래프 모드 설정
        // speedDataSet.setDrawCircles(false) // 원 그리기 설정하지 않음
        speedDataSet.setDrawValues(false) // 값 텍스트 그리기 설정하지 않음

        // 그라데이션 배경 설정
        val fillDrawable = object : Drawable() {
            override fun draw(canvas: Canvas) {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val shader = LinearGradient(
                    0f, 0f, 0f, bounds.height().toFloat(),
                    Color.rgb(63, 81, 181), Color.TRANSPARENT, Shader.TileMode.CLAMP
                )
                paint.shader = shader
                canvas.drawRect(bounds, paint)
            }

            override fun setAlpha(alpha: Int) {}

            override fun setColorFilter(colorFilter: ColorFilter?) {}

            override fun getOpacity(): Int {
                return PixelFormat.UNKNOWN
            }
        }
        speedDataSet.setDrawFilled(true) // 그래프 내부를 채우는 설정
        speedDataSet.fillDrawable = fillDrawable // 그라데이션 배경을 그래프에 설정

        // X축 설정
        val xAxis = speedChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM // X축 위치 설정
        xAxis.textColor = Color.WHITE // X축 텍스트 색상 설정
        xAxis.setDrawGridLines(false) // X축 그리드 라인 그리지 않음

        // Y축 설정
        val yAxisLeft = speedChart.axisLeft
        yAxisLeft.textColor = Color.WHITE // Y축 텍스트 색상 설정
        yAxisLeft.setDrawGridLines(true) // Y축 그리드 라인 그리기 설정
        yAxisLeft.axisMinimum = 0f // Y축 최소값 설정

        // Y축 값 포맷터 설정
        val yAxisFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                // 여기에서 소수점 값을 원하는 형식으로 변환하여 반환합니다.
                Log.d("소수점 데이터인가?", "" + value)
                return String.format("%.0f", value)
            }
        }
        yAxisLeft.valueFormatter = yAxisFormatter

        val yAxisRight = speedChart.axisRight
        yAxisRight.isEnabled = false

        // Chart 설정
        speedChart.description.isEnabled = false
        speedChart.legend.isEnabled = false
        speedChart.animateY(1000, Easing.Linear)

        speedChart.setOnChartGestureListener(object : OnChartGestureListener {
            override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
                if (lastPerformedGesture == ChartTouchListener.ChartGesture.DRAG) {
                    isDragging = true
                    // showDataOnScreen()
                }
            }

            override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
                if (lastPerformedGesture == ChartTouchListener.ChartGesture.DRAG) {
                    isDragging = false
                    // hideDataOnScreen()
                }
            }

            override fun onChartLongPressed(me: MotionEvent?) {}
            override fun onChartDoubleTapped(me: MotionEvent?) {}
            override fun onChartSingleTapped(me: MotionEvent?) {}

            override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}

            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
        })

        // 데이터 추가 및 그리기
        val lineData = LineData(speedDataSet)
        speedChart.data = lineData
        speedChart.invalidate()
    }


    private fun loadAverageSpeedChart(DistanceFitness: List<DistanceFitnessModel>) {
        averageSpeedChart.description.isEnabled = false
        averageSpeedChart.setNoDataText("데이터가 없습니다.")
        averageSpeedChart.setTouchEnabled(false)
        averageSpeedChart.setDrawGridBackground(false)

        val averageSpeedDataSet = LineDataSet(mutableListOf(), "Speed")
        averageSpeedDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)

        DistanceFitness.forEachIndexed { index, distanceFitnessModel ->
            val distanceEntry = Entry(index.toFloat(), distanceFitnessModel.distance.toFloat())
            averageSpeedDataSet.addEntry(distanceEntry)
        }

        val data = LineData()
        data.addDataSet(averageSpeedDataSet)

        averageSpeedDataSet.color = Color.WHITE
        averageSpeedDataSet.lineWidth = 2f
        averageSpeedDataSet.setCircleColor(Color.rgb(255, 255, 255))
        averageSpeedDataSet.circleRadius = 4f
        averageSpeedDataSet.setDrawCircleHole(false)
        averageSpeedDataSet.valueTextSize = 10f
        averageSpeedDataSet.valueTextColor = Color.WHITE
        averageSpeedDataSet.setDrawValues(true)
        averageSpeedDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        // 그라데이션 배경 설정
        val fillDrawable = object : Drawable() {
            override fun draw(canvas: Canvas) {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val shader = LinearGradient(
                    0f, 0f, 0f, bounds.height().toFloat(),
                    Color.rgb(63, 81, 181), Color.TRANSPARENT, Shader.TileMode.CLAMP
                )
                paint.shader = shader
                canvas.drawRect(bounds, paint)
            }

            override fun setAlpha(alpha: Int) {}

            override fun setColorFilter(colorFilter: ColorFilter?) {}

            override fun getOpacity(): Int {
                return PixelFormat.UNKNOWN
            }
        }
        averageSpeedDataSet.setDrawFilled(true)
        averageSpeedDataSet.fillDrawable = fillDrawable
        // X축 설정
        val xAxis = averageSpeedChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false)


        // Y축 설정
        val yAxisLeft = averageSpeedChart.axisLeft
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.axisMinimum = 0f

        val yAxisRight = averageSpeedChart.axisRight
        yAxisRight.isEnabled = false

        // Chart 설정
        averageSpeedChart.description.isEnabled = false
        averageSpeedChart.legend.isEnabled = false
        averageSpeedChart.setTouchEnabled(false)
        averageSpeedChart.animateY(1000, Easing.Linear)

        // 데이터 추가 및 그리기
        val lineData = LineData(averageSpeedDataSet)
        averageSpeedChart.data = lineData
        averageSpeedChart.invalidate()
    }

}