package com.example.swasthyamitra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var heartRateText: TextView
    private lateinit var spo2Text: TextView
    private lateinit var tempText: TextView
    private lateinit var bmiText: TextView
    private lateinit var bmiStatusText: TextView
    private lateinit var ecgStatusText: TextView
    private lateinit var stressLevelText: TextView
    private lateinit var respiratoryRateText: TextView
    private lateinit var heightText: TextView
    private lateinit var viewReportButton: Button
    private lateinit var backButton: Button
    private lateinit var dbHelper: DatabaseHelper
    
    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private val esp32Url = "http://192.168.4.1/data"

    private var currentHeartRate = "75"
    private var currentSpo2 = "98"
    private var currentTemp = "36.5"
    private var currentBmi = "22.5"
    private var currentHeight = "175"
    private var currentEcg = "Normal"
    private var currentStress = "Low"
    private var currentRespiratory = "16"

    private val fetchDataRunnable = object : Runnable {
        override fun run() {
            fetchDataFromESP32()
            handler.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardRoot)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        heartRateText = findViewById(R.id.heartRateText)
        spo2Text = findViewById(R.id.spo2Text)
        tempText = findViewById(R.id.tempText)
        bmiText = findViewById(R.id.bmiText)
        bmiStatusText = findViewById(R.id.bmiStatusText)
        ecgStatusText = findViewById(R.id.ecgStatusText)
        stressLevelText = findViewById(R.id.stressLevelText)
        respiratoryRateText = findViewById(R.id.respiratoryRateText)
        heightText = findViewById(R.id.heightText)
        viewReportButton = findViewById(R.id.viewReportButton)
        backButton = findViewById(R.id.backButton)

        loadUserData()

        viewReportButton.setOnClickListener {
            viewReportButton.isEnabled = false // Prevent double-click hang
            saveDataAndOpenReport()
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        handler.post(fetchDataRunnable)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(fetchDataRunnable)
    }

    private fun loadUserData() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val weightStr = sharedPref.getString("user_weight", "0")
        val heightStr = sharedPref.getString("user_height", "175")
        
        currentHeight = heightStr ?: "175"
        heightText.text = currentHeight

        val weight = weightStr?.toDoubleOrNull() ?: 0.0
        val heightCm = currentHeight.toDoubleOrNull() ?: 175.0
        val heightM = heightCm / 100.0
        
        if (weight > 0 && heightM > 0) {
            val bmi = weight / (heightM * heightM)
            currentBmi = String.format("%.1f", bmi)
            bmiText.text = currentBmi
            
            bmiStatusText.text = when {
                bmi < 18.5 -> "Underweight"
                bmi < 25 -> "Normal"
                bmi < 30 -> "Overweight"
                else -> "Obese"
            }
        }
    }

    private fun fetchDataFromESP32() {
        val request = Request.Builder().url(esp32Url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string()
                if (jsonData != null) {
                    try {
                        val jsonObject = JSONObject(jsonData)
                        runOnUiThread {
                            currentHeartRate = jsonObject.optString("heartRate", currentHeartRate)
                            currentSpo2 = jsonObject.optString("spo2", currentSpo2)
                            currentTemp = jsonObject.optString("temp", currentTemp)
                            currentEcg = jsonObject.optString("ecg", "Normal")
                            currentStress = jsonObject.optString("stress", "Low")
                            currentRespiratory = jsonObject.optString("respiratory", "16")
                            
                            heartRateText.text = currentHeartRate
                            spo2Text.text = currentSpo2
                            tempText.text = currentTemp
                            ecgStatusText.text = currentEcg
                            stressLevelText.text = currentStress
                            respiratoryRateText.text = currentRespiratory

                            updateColors()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun updateColors() {
        val hr = currentHeartRate.toIntOrNull() ?: 0
        heartRateText.setTextColor(if (hr > 100 || (hr < 60 && hr > 0)) android.graphics.Color.RED else android.graphics.Color.parseColor("#D32F2F"))
        
        val s2 = currentSpo2.toIntOrNull() ?: 100
        spo2Text.setTextColor(if (s2 < 95) android.graphics.Color.RED else android.graphics.Color.parseColor("#1976D2"))
    }

    private fun saveDataAndOpenReport() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val email = sharedPref.getString("userEmail", "Unknown") ?: "Unknown"
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // Use a background thread for DB operations to avoid UI hang
        Thread {
            dbHelper.addHealthRecord(
                email, date, currentHeartRate, currentSpo2, currentTemp, 
                currentBmi, currentEcg, currentStress, currentRespiratory, currentHeight
            )
            
            runOnUiThread {
                val intent = Intent(this, AIReportActivity::class.java)
                intent.putExtra("heartRate", currentHeartRate)
                intent.putExtra("spo2", currentSpo2)
                intent.putExtra("temp", currentTemp)
                intent.putExtra("bmi", currentBmi)
                intent.putExtra("ecg", currentEcg)
                intent.putExtra("stress", currentStress)
                intent.putExtra("respiratory", currentRespiratory)
                intent.putExtra("height", currentHeight)
                startActivity(intent)
            }
        }.start()
    }
}
