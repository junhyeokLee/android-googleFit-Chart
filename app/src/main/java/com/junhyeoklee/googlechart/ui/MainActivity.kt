package com.junhyeoklee.googlechart.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.junhyeoklee.googlechart.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1 // Google Fit API 권한 요청에 사용되는 코드 상수
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q // 현재 기기의 Android 버전이 Android Q(10) 이상인지 여부를 확인하기 위한 변수
    private lateinit var bottomNavigationView: BottomNavigationView

    // Google Fit API에서 가져올 데이터 유형에 대한 접근 권한 설정을 담은 FitnessOptions 객체.
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 권한이 승인되어 있는지 확인하고, 승인되어 있다면 Google Fit에 로그인하고 그렇지 않다면 권한 요청을 수행
        checkPermissionsAndRun(GOOGLE_FIT_PERMISSIONS_REQUEST_CODE)
    }

    private fun loadNav_controller() { // BottomNavigationView와 NavController를 초기화하고, 현재 선택된 목적지로 이동하는 함수
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        val navController = findNavController(R.id.nav_controller)

        val currentDestinationId = navController.currentDestination?.id

        if(currentDestinationId != null) {
            navController.popBackStack(currentDestinationId,false)
            navController.navigate(currentDestinationId)
        }
        bottomNavigationView.setupWithNavController(navController)
    }


    private fun checkPermissionsAndRun(fitActionRequestCode: Int) {
        if (permissionApproved()) { //  위치 권한이 승인되었는지 여부를 반환하는 함수
            fitSignIn() // Google 계정에 대한 Google Fit API 권한이 있는지 확인하고, 없다면 권한 요청을 진행하는 함수
        } else {
            // 런타임에 위치 권한을 요청하는 함수로, 권한 요청 이유를 설명하는 스낵바를 표시하고 권한을 요청
            requestRuntimePermissions(fitActionRequestCode)
        }
    }

    private fun requestPermissions() { //  Google Fit API에 대한 권한을 요청하는 함수
        GoogleSignIn.requestPermissions(
            this, // your activity
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            getGoogleAccount(),
            fitnessOptions)
    }

    private fun fitSignIn() {
        if (!GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)) {
            requestPermissions()
        } else {
            loadNav_controller()
        }
    }

    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

    private fun permissionApproved(): Boolean {
        val approved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            true
        }
        return approved
    }

    // 런타임(실행 시간)에 위치 권한을 요청하는 함수
    private fun requestRuntimePermissions(requestCode: Int) {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)

        requestCode.let {
            if (shouldProvideRationale) {
                Log.i(ContentValues.TAG, "Displaying permission rationale to provide additional context.")
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Permission Denied",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Settings") {
                        // Request permission
                        ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            requestCode)
                    }
                    .show()
            } else { // 권한을 직접 요청
                Log.i(ContentValues.TAG, "Requesting permission")
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode)
            }
        }
        checkPermissionsAndRun(requestCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // 권한 요청 결과를 처리하는 함수로, 권한이 승인된 경우 앱을 로드하는 함수를 호출하고, 승인되지 않은 경우 권한을 다시 요청
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> loadNav_controller()
                else -> {
                    // Result wasn't from Google Fit
                }
            }
            else -> {
                // 권한이 없을때 재요청
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show()
                requestPermissions()
            }
        }
    }



}
