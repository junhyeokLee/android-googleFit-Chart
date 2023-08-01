package com.junhyeoklee.googlechart.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.junhyeoklee.googlechart.R
import com.junhyeoklee.googlechart.databinding.FragmentTakeoutBinding
import com.junhyeoklee.googlechart.viewmodel.FitnessViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TakeOut : Fragment(R.layout.fragment_takeout) {

    private val fitnessViewModel: FitnessViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTakeoutBinding.bind(view)
        requireContext()

        // TakeOutStep 데이터를 가져와서 관찰하고, UI에 반영
        fitnessViewModel.getTakeOutStepData(requireContext()).observe(viewLifecycleOwner, Observer { TakeOutStep ->
            binding.steps.text = TakeOutStep.toString()
            binding.stepsBig.text = TakeOutStep.toString()
            binding.stepsProgressBar.progress = TakeOutStep
        })

        /* TakeOutSpeed 데이터를 가져와서 관찰하고, UI에 반영
        fitnessViewModel.getTakeOutSpeedData(requireContext()).observe(viewLifecycleOwner, Observer { TakeOutSpeed ->
            binding.speed.text = String.format("%.2f", TakeOutSpeed)
        }) */

        // TakeOutDistance 데이터를 가져와서 관찰하고, UI에 반영
        fitnessViewModel.getTakeOutDistanceData(requireContext()).observe(viewLifecycleOwner, Observer { TakeOutDistance ->
            binding.distance.text = String.format("%.2f", TakeOutDistance / 1000) +" km"

            val speed =  15.4 / (TakeOutDistance / 1000)
            binding.speed.text = (String.format("%.2f", speed) + "/km").replace(".",":")
        })
    }
}
