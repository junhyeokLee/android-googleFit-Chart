package com.junhyeoklee.googlechart.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import com.junhyeoklee.googlechart.R
import com.junhyeoklee.googlechart.databinding.FragmentSettingBinding
import com.junhyeoklee.googlechart.viewmodel.FitnessViewModel

class Setting : Fragment(R.layout.fragment_setting) {

    private lateinit var stepsTextView: TextView
    private lateinit var changeObjButton: Button

    private val fitnessViewModel: FitnessViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingBinding.bind(view)

        binding.apply {
            stepsTextView = this.steps
            changeObjButton = this.changeObj
        }
        showObjectiveSteps(requireContext())
        changeObjButton.setOnClickListener{
            showObjectiveDialog(requireContext())
        }
    }

    private fun showObjectiveSteps(context: Context) {
        stepsTextView.text = fitnessViewModel.loadObjectiveSteps(context).toString()
    }

    private fun showObjectiveDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_objective, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Select Objective Steps")

        val objectiveSeekBar = dialogView.findViewById<NumberPicker>(R.id.stepsPicker)

        objectiveSeekBar.minValue = 8
        objectiveSeekBar.maxValue = 40
        objectiveSeekBar.value = fitnessViewModel.loadObjectiveSteps(context) / 1000

        // Set the formatter to display the values in increments of 1000
        objectiveSeekBar.setFormatter( object : NumberPicker.Formatter {
            override fun format(value: Int): String {
                return "${value * 1000}"
            }
        })

        // Save the new objective steps value when the "Save" button is clicked
        dialogBuilder.setPositiveButton("Save") { _, _ ->
            val newObjectiveSteps = objectiveSeekBar.value * 1000
            fitnessViewModel.saveObjectiveSteps(context, newObjectiveSteps)
            showObjectiveSteps(context)
            Toast.makeText(context, "Objective steps saved", Toast.LENGTH_SHORT).show()
        }

        // Show the dialog
        dialogBuilder.create().show()
    }

}