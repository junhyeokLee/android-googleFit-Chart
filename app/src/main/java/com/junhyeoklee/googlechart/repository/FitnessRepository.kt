package com.atilmohamine.fitnesstracker.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.atilmohamine.fitnesstracker.model.DailyFitnessModel
import com.atilmohamine.fitnesstracker.model.WeeklyFitnessModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.junhyeoklee.googlechart.model.HourFitnessModel
import com.junhyeoklee.googlechart.model.MinuteFitnessModel
import com.junhyeoklee.googlechart.model.SecondFitnessModel

interface FitnessRepository {
    fun getSecondFitnessData(context: Context): MutableLiveData<List<SecondFitnessModel>>
    fun getMinuteFitnessData(context: Context): MutableLiveData<List<MinuteFitnessModel>>
    fun getHourFitnessData(context: Context): MutableLiveData<List<HourFitnessModel>>
    fun getDailyFitnessData(context: Context): MutableLiveData<DailyFitnessModel>
    fun getWeeklyFitnessData(context: Context): MutableLiveData<WeeklyFitnessModel>
    fun getGoogleAccount(context: Context): GoogleSignInAccount

}