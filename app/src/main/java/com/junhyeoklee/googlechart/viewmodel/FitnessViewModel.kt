package com.junhyeoklee.googlechart.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atilmohamine.fitnesstracker.model.DailyFitnessModel
import com.atilmohamine.fitnesstracker.model.WeeklyFitnessModel
import com.atilmohamine.fitnesstracker.repository.FitnessRepository
import com.atilmohamine.fitnesstracker.repository.FitnessRepositoryImpl
import com.atilmohamine.fitnesstracker.repository.SharedPreferencesRepository
import com.atilmohamine.fitnesstracker.repository.SharedPreferencesRepositoryImpl
import com.junhyeoklee.googlechart.model.HourFitnessModel
import com.junhyeoklee.googlechart.model.MinuteFitnessModel
import com.junhyeoklee.googlechart.model.SecondFitnessModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FitnessViewModel @Inject constructor(private val fitnessRepository: FitnessRepository = FitnessRepositoryImpl(),private val sharedPreferencesRepo: SharedPreferencesRepository = SharedPreferencesRepositoryImpl()) : ViewModel() {

//    val sharedPreferencesRepo: SharedPreferencesRepository = SharedPreferencesRepositoryImpl()

    fun getSecondFitnessData(context:Context): LiveData<List<SecondFitnessModel>> {
        var secondFitnessLiveData = fitnessRepository.getSecondFitnessData(context)
        return secondFitnessLiveData
    }
    fun getMinuteFitnessData(context:Context): LiveData<List<MinuteFitnessModel>> {
        var minuteFitnessLiveData = fitnessRepository.getMinuteFitnessData(context)
        return minuteFitnessLiveData
    }

    fun getHourFitnessData(context:Context): LiveData<List<HourFitnessModel>> {
        var hourFitnessLiveData = fitnessRepository.getHourFitnessData(context)
        return hourFitnessLiveData
    }
     fun getDailyFitnessData(context: Context): LiveData<DailyFitnessModel> {
        var dailyFitnessLiveData = fitnessRepository.getDailyFitnessData(context)
        return dailyFitnessLiveData
    }

    fun getWeeklyFitnessData(context: Context): LiveData<WeeklyFitnessModel> {
        var weeklyFitnessLiveData = fitnessRepository.getWeeklyFitnessData(context)
        return weeklyFitnessLiveData
    }

    fun saveObjectiveSteps(context: Context, objectiveSteps: Int) {
        sharedPreferencesRepo.saveObjectiveSteps(context, objectiveSteps)
    }

    fun loadObjectiveSteps(context: Context): Int {
        return sharedPreferencesRepo.loadObjectiveSteps(context)
    }

}