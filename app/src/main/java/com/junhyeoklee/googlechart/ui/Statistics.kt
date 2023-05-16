package com.junhyeoklee.googlechart.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.atilmohamine.fitnesstracker.model.WeeklyFitnessModel
import com.atilmohamine.fitnesstracker.utils.DayAxisValueFormatter
import com.atilmohamine.fitnesstracker.utils.HourAxisValueFormatter
import com.atilmohamine.fitnesstracker.utils.MinuteAxisValueFormatter
import com.atilmohamine.fitnesstracker.utils.SecondAxisValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.junhyeoklee.googlechart.R
import com.junhyeoklee.googlechart.databinding.FragmentStatisticsBinding
import com.junhyeoklee.googlechart.model.HourFitnessModel
import com.junhyeoklee.googlechart.model.MinuteFitnessModel
import com.junhyeoklee.googlechart.model.SecondFitnessModel
import com.junhyeoklee.googlechart.viewmodel.FitnessViewModel


class Statistics : Fragment(R.layout.fragment_statistics) {

    private lateinit var weekChart: BarChart
    private lateinit var hourkChart: BarChart
    private lateinit var minuteChart: BarChart
    private lateinit var secondChart: BarChart

    private val fitnessViewModel: FitnessViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentStatisticsBinding.bind(view)

        binding.apply {
            this@Statistics.weekChart = this.weekChart
            this@Statistics.hourkChart = this.hourChart
            this@Statistics.minuteChart = this.minuteChart
            this@Statistics.secondChart = this.secondChart
        }

        fitnessViewModel.getWeeklyFitnessData(requireContext()).observe(viewLifecycleOwner, Observer { WeeklyFitness->
            loadWeekChart(WeeklyFitness)
        })
        fitnessViewModel.getHourFitnessData(requireContext()).observe(viewLifecycleOwner, Observer { HourFitness->
            loadHourChart(HourFitness)
        })
        fitnessViewModel.getMinuteFitnessData(requireContext()).observe(viewLifecycleOwner, Observer { MinuteFitness->
            loadMinuteChart(MinuteFitness)
        })
        fitnessViewModel.getSecondFitnessData(requireContext()).observe(viewLifecycleOwner, Observer { SecondFitness->
            loadSecondChart(SecondFitness)
        })

    }

    private fun loadWeekChart(WeeklyFitness: WeeklyFitnessModel) {
        weekChart.description.isEnabled = false
        weekChart.setTouchEnabled(false)
        weekChart.setDrawGridBackground(false)

        val caloriesDataSet = BarDataSet(mutableListOf(), "Calories")
        val stepsDataSet = BarDataSet(mutableListOf(), "Steps")
        stepsDataSet.color = ContextCompat.getColor(requireContext(), R.color.calories)
        stepsDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val distanceDataSet = LineDataSet(mutableListOf(), "Distance")

        WeeklyFitness.dailyFitnessList.forEachIndexed { index, fitnessData ->
            val caloriesEntry = BarEntry(index.toFloat(), fitnessData.caloriesBurned.toFloat())
            val stepsEntry = BarEntry(index.toFloat(), fitnessData.stepCount.toFloat())
            val distanceEntry = BarEntry(index.toFloat(), fitnessData.distance)

            caloriesDataSet.addEntry(caloriesEntry)
            stepsDataSet.addEntry(stepsEntry)
            distanceDataSet.addEntry(distanceEntry)
        }

        val data = BarData()
        data.addDataSet(caloriesDataSet)
        data.addDataSet(stepsDataSet)
//        data.addDataSet(distanceDataSet)

        weekChart.data = data

        // Set X-axis properties
        val xAxis = weekChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //xAxis.labelCount = 7
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        xAxis.valueFormatter = DayAxisValueFormatter(weekChart)

        // Set Y-axis properties
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

    private fun loadHourChart(HourFitness: List<HourFitnessModel>){
        hourkChart.description.isEnabled = false
        hourkChart.setNoDataText("데이터가 없습니다.")
        hourkChart.setTouchEnabled(false)
        hourkChart.setDrawGridBackground(false)

        val stepsDataSet = BarDataSet(mutableListOf(), "Steps")
        stepsDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)


        HourFitness.forEachIndexed { index, hourFitnessModel ->
            val stepsEntry = BarEntry(index.toFloat(), hourFitnessModel.stepCount.toFloat())
            stepsDataSet.addEntry(stepsEntry)

        }

        val data = BarData()
        data.addDataSet(stepsDataSet)
//        data.addDataSet(distanceDataSet)

        hourkChart.data = data

        // Set X-axis properties
        val xAxis = hourkChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //xAxis.labelCount = 7
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        xAxis.valueFormatter = HourAxisValueFormatter(hourkChart)

        // Set Y-axis properties
        val yAxisLeft = hourkChart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.isEnabled = false
        yAxisLeft.axisMinimum = 0f

        val yAxisRight = hourkChart.axisRight
        yAxisRight.isEnabled = false
        yAxisLeft.setDrawGridLines(false)

        val barData = hourkChart.barData
        barData.barWidth = 0.6f

        hourkChart.legend.isEnabled = false

        hourkChart.notifyDataSetChanged()
        hourkChart.invalidate()
    }


    private fun loadMinuteChart(MinuteFitness: List<MinuteFitnessModel>){
        minuteChart.description.isEnabled = false
        minuteChart.setNoDataText("데이터가 없습니다.")
        minuteChart.setTouchEnabled(false)
        minuteChart.setDrawGridBackground(false)

        val stepsDataSet = BarDataSet(mutableListOf(), "Steps")
        stepsDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)


        MinuteFitness.forEachIndexed { index, minuteFitnessModel ->
            val stepsEntry = BarEntry(index.toFloat(), minuteFitnessModel.stepCount.toFloat())
            stepsDataSet.addEntry(stepsEntry)

        }

        val data = BarData()
        data.addDataSet(stepsDataSet)
//        data.addDataSet(distanceDataSet)

        minuteChart.data = data

        // Set X-axis properties
        val xAxis = minuteChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //xAxis.labelCount = 7
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        xAxis.valueFormatter = MinuteAxisValueFormatter(minuteChart)

        // Set Y-axis properties
        val yAxisLeft = minuteChart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.isEnabled = false
        yAxisLeft.axisMinimum = 0f

        val yAxisRight = minuteChart.axisRight
        yAxisRight.isEnabled = false
        yAxisLeft.setDrawGridLines(false)

        val barData = minuteChart.barData
        barData.barWidth = 0.6f

        minuteChart.legend.isEnabled = false

        minuteChart.notifyDataSetChanged()
        minuteChart.invalidate()
    }

    private fun loadSecondChart(SecondFitness: List<SecondFitnessModel>){
        secondChart.description.isEnabled = false
        secondChart.setTouchEnabled(false)
        secondChart.setDrawGridBackground(false)

        val stepsDataSet = BarDataSet(mutableListOf(), "Steps")
        stepsDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)


        SecondFitness.forEachIndexed { index, secondFitnessModel ->
//            Log.e("초 데이터 = ","몇초 = "+secondFitnessModel.time.toString()+"   : 스텝 = "+secondFitnessModel.stepCount.toFloat())

            val stepsEntry = BarEntry(index.toFloat(), secondFitnessModel.stepCount.toFloat())
            stepsDataSet.addEntry(stepsEntry)

        }

        val data = BarData()
        data.addDataSet(stepsDataSet)
//        data.addDataSet(distanceDataSet)

        secondChart.data = data

        // Set X-axis properties
        val xAxis = secondChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //xAxis.labelCount = 7
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        xAxis.valueFormatter = SecondAxisValueFormatter(secondChart)

        // Set Y-axis properties
        val yAxisLeft = secondChart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.isEnabled = false
        yAxisLeft.axisMinimum = 0f

        val yAxisRight = secondChart.axisRight
        yAxisRight.isEnabled = false
        yAxisLeft.setDrawGridLines(false)

        val barData = secondChart.barData
        barData.barWidth = 0.6f

        secondChart.legend.isEnabled = false

        secondChart.notifyDataSetChanged()
        secondChart.invalidate()
    }

}