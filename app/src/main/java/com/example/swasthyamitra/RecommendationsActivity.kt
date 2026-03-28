package com.example.swasthyamitra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecommendationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendations)

        val doctorStatusText = findViewById<TextView>(R.id.doctorStatusText)
        val adviceText = findViewById<TextView>(R.id.adviceText)
        val goHomeButton = findViewById<Button>(R.id.goHomeButton)
        val newCheckupButton = findViewById<Button>(R.id.newCheckupButton)

        val heartRate = intent.getStringExtra("heartRate")?.toIntOrNull() ?: 75
        val spo2 = intent.getStringExtra("spo2")?.toIntOrNull() ?: 98

        if (spo2 < 94 || heartRate > 120 || heartRate < 50) {
            doctorStatusText.text = "Doctor Status: Visit Doctor"
            doctorStatusText.setTextColor(android.graphics.Color.RED)
            adviceText.text = "Your vitals are outside the normal range. Please seek medical advice immediately."
        } else if (spo2 < 96 || heartRate > 100) {
            doctorStatusText.text = "Doctor Status: Monitor"
            doctorStatusText.setTextColor(android.graphics.Color.parseColor("#FFA500"))
            adviceText.text = "Your vitals are slightly elevated. Rest for 15 minutes and recheck."
        } else {
            doctorStatusText.text = "Doctor Status: Normal"
            doctorStatusText.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            adviceText.text = "Everything looks good! Keep maintaining a healthy lifestyle."
        }

        goHomeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        newCheckupButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}
