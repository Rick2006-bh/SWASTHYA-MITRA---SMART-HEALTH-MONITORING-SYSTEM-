package com.example.swasthyamitra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AIReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_report)

        val reportTable = findViewById<TableLayout>(R.id.reportTable)
        val aiExplanationText = findViewById<TextView>(R.id.aiExplanationText)
        val viewRecommendationsButton = findViewById<Button>(R.id.viewRecommendationsButton)
        val askAiButton = findViewById<Button>(R.id.askAiButton)

        // Retrieve all 8 parameters
        val heartRate = intent.getStringExtra("heartRate") ?: "N/A"
        val spo2 = intent.getStringExtra("spo2") ?: "N/A"
        val temp = intent.getStringExtra("temp") ?: "N/A"
        val bmi = intent.getStringExtra("bmi") ?: "N/A"
        val ecg = intent.getStringExtra("ecg") ?: "N/A"
        val stress = intent.getStringExtra("stress") ?: "N/A"
        val respiratory = intent.getStringExtra("respiratory") ?: "N/A"
        val height = intent.getStringExtra("height") ?: "N/A"

        // Add all rows to the table
        addTableRow(reportTable, "Heart Rate", "$heartRate BPM")
        addTableRow(reportTable, "SpO2", "$spo2 %")
        addTableRow(reportTable, "Temperature", "$temp °C")
        addTableRow(reportTable, "BMI", bmi)
        addTableRow(reportTable, "ECG Rate", ecg)
        addTableRow(reportTable, "Stress Level", stress)
        addTableRow(reportTable, "Respiratory Rate", "$respiratory BPM")
        addTableRow(reportTable, "Height", "$height cm")

        val explanation = generateAIExplanation(heartRate, spo2, temp, bmi, ecg, stress, respiratory)
        aiExplanationText.text = explanation

        viewRecommendationsButton.setOnClickListener {
            val intent = Intent(this, RecommendationsActivity::class.java)
            intent.putExtra("heartRate", heartRate)
            intent.putExtra("spo2", spo2)
            intent.putExtra("temp", temp)
            intent.putExtra("bmi", bmi)
            intent.putExtra("ecg", ecg)
            intent.putExtra("stress", stress)
            intent.putExtra("respiratory", respiratory)
            intent.putExtra("height", height)
            startActivity(intent)
        }

        askAiButton.setOnClickListener {
            Toast.makeText(this, "This feature is coming soon.", Toast.LENGTH_LONG).show()
        }
    }

    private fun addTableRow(table: TableLayout, label: String, value: String) {
        val row = TableRow(this)
        row.setPadding(8, 8, 8, 8)

        val labelTv = TextView(this)
        labelTv.text = label
        labelTv.setTextColor(android.graphics.Color.BLACK)
        labelTv.textSize = 16f

        val valueTv = TextView(this)
        valueTv.text = value
        valueTv.gravity = android.view.Gravity.END
        valueTv.setTextColor(android.graphics.Color.BLACK)
        valueTv.textSize = 16f
        valueTv.setPadding(20, 0, 0, 0)

        row.addView(labelTv)
        row.addView(valueTv)
        table.addView(row)
    }

    private fun generateAIExplanation(hr: String, spo2: String, temp: String, bmi: String, ecg: String, stress: String, resp: String): String {
        val hrVal = hr.toIntOrNull() ?: 75
        val spo2Val = spo2.toIntOrNull() ?: 98
        val bmiVal = bmi.toDoubleOrNull() ?: 22.0
        val respVal = resp.toIntOrNull() ?: 16

        var report = "Based on your vitals, "

        // SpO2
        report += if (spo2Val < 95) "your oxygen level (SpO2) is slightly low. " else "your oxygen level is normal. "

        // Heart Rate
        if (hrVal > 100) report += "Your heart rate is high (Tachycardia). "
        else if (hrVal < 60) report += "Your heart rate is lower than average. "
        else report += "Your heart rate is within the healthy range. "

        // BMI
        report += when {
            bmiVal < 18.5 -> "Your BMI indicates you are underweight. "
            bmiVal < 25 -> "Your BMI is in the healthy range. "
            bmiVal < 30 -> "Your BMI indicates you are overweight. "
            else -> "Your BMI indicates obesity. "
        }

        // ECG & Stress
        if (ecg != "Normal") report += "ECG shows some variations. "
        if (stress == "High") report += "Your stress levels are currently high. "

        // Respiratory
        if (respVal > 20) report += "Your respiratory rate is higher than normal. "

        return report + "\n\nDisclaimer: This is an automated summary. Please consult a professional for medical diagnosis."
    }
}
