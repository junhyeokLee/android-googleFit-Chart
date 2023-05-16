package com.junhyeoklee.googlechart.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.junhyeoklee.googlechart.R
import com.junhyeoklee.googlechart.databinding.FragmentHomeBinding
import com.junhyeoklee.googlechart.viewmodel.FitnessViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Home : Fragment(R.layout.fragment_home) {

    private val fitnessViewModel: FitnessViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)
        requireContext()

        fitnessViewModel.getDailyFitnessData(requireContext()).observe(viewLifecycleOwner, Observer { DailyFitness->
            binding.steps.text = DailyFitness.stepCount.toString()
            binding.stepsBig.text = DailyFitness.stepCount.toString()
            binding.burnedCalories.text = DailyFitness.caloriesBurned.toString()
            binding.distance.text = String.format("%.2f", DailyFitness.distance)
            binding.stepsProgressBar.progress = DailyFitness.stepCount
            binding.active.text = DailyFitness.active.toString()

        })
    }

}