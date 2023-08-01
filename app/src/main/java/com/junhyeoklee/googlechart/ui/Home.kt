package com.junhyeoklee.googlechart.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.junhyeoklee.googlechart.R
import com.junhyeoklee.googlechart.databinding.FragmentHomeBinding
import com.junhyeoklee.googlechart.viewmodel.FitnessViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Home : Fragment(R.layout.fragment_home) {

    private val fitnessViewModel: FitnessViewModel by viewModels() // viewModels() 함수를 통해 FitnessViewModel 객체를 초기화

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view) // ViewBinding을 사용하여 view를 바인딩
        requireContext()

        // FitnessViewModel에서 dailyFitness 데이터를 가져와서 옵저버를 등록, 데이터가 업데이트될 때마다 UI를 업데이트
        fitnessViewModel.getDailyFitnessData(requireContext()).observe(viewLifecycleOwner, Observer { DailyFitness->
            binding.steps.text = DailyFitness.stepCount.toString()
            binding.stepsBig.text = DailyFitness.stepCount.toString()
            binding.burnedCalories.text = DailyFitness.caloriesBurned.toString()
            binding.distance.text = String.format("%.2f", DailyFitness.distance)
            binding.stepsProgressBar.progress = DailyFitness.stepCount
            binding.active.text = DailyFitness.active.toString() + " 분"

            val speed =  DailyFitness.active / DailyFitness.distance
            binding.speed.text = (String.format("%.2f", speed) + "/km").replace(".",":")

        })
    }

}