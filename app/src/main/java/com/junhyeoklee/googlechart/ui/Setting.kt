package com.junhyeoklee.googlechart.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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

    private fun showObjectiveSteps(context: Context) { // Objective Steps 값을 보여주는 함수로, FitnessViewModel에서 Objective Steps 값을 가져와서 TextView에 표시
        stepsTextView.text = fitnessViewModel.loadObjectiveSteps(context).toString()
    }

    private fun showObjectiveDialog(context: Context) { // Objective Steps 값을 설정하는 다이얼로그를 표시하는 함수
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_objective, null)
        val dialogBuilder = AlertDialog.Builder(context)// AlertDialog를 생성하는 빌더 객체를 초기화
            .setView(dialogView)
            .setTitle("Select Objective Steps")

        val objectiveSeekBar = dialogView.findViewById<NumberPicker>(R.id.stepsPicker)

        objectiveSeekBar.minValue = 8
        objectiveSeekBar.maxValue = 40
        objectiveSeekBar.value = fitnessViewModel.loadObjectiveSteps(context) / 1000 //  FitnessViewModel에서 Objective Steps 값을 가져와서 설정

        objectiveSeekBar.setFormatter( object : NumberPicker.Formatter { // Objective Steps 값을 1000 단위로 표시하기 위해 값을 포맷팅하는데 사용되는 Formatter를 설정
            override fun format(value: Int): String {
                return "${value * 1000}"
            }
        })

        //  "Save" 버튼을 클릭했을 때, 선택한 Objective Steps 값을 저장하고 UI를 업데이트
        dialogBuilder.setPositiveButton("Save") { _, _ ->
            val newObjectiveSteps = objectiveSeekBar.value * 1000
            fitnessViewModel.saveObjectiveSteps(context, newObjectiveSteps)
            showObjectiveSteps(context)
            Toast.makeText(context, "Objective steps saved", Toast.LENGTH_SHORT).show()
        }

        dialogBuilder.create().show()
    }

}