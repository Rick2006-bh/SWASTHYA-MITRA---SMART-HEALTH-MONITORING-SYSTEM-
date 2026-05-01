package com.example.swasthyamitra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import java.io.IOException
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dbHelper: DatabaseHelper
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        dbHelper = DatabaseHelper(this)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val lastCheckupText = findViewById<TextView>(R.id.lastCheckupText)
        val startNewScanButton = findViewById<Button>(R.id.startNewScanButton)
        val profileButton = findViewById<Button>(R.id.profileButton)
        val connectionStatusButton = findViewById<Button>(R.id.connectionStatusButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val historyContainer = findViewById<LinearLayout>(R.id.historyContainer)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = currentUser.uid
        val userEmail = currentUser.email ?: "Unknown"

        // 🔥 Fetch User Name from Firestore first, fallback to SQLite
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: "User"
                    welcomeText.text = "Hello, $name!"
                } else {
                    val nameFromDb = getNameFromLocal(userEmail)
                    welcomeText.text = "Hello, $nameFromDb!"
                }
            }
            .addOnFailureListener {
                val nameFromDb = getNameFromLocal(userEmail)
                welcomeText.text = "Hello, $nameFromDb!"
            }

        // Load History from SQLite
        loadHistory(userEmail, historyContainer, lastCheckupText)

        // Check ESP32 Status
        checkEspStatus(connectionStatusButton)

        // Navigation to ProfileActivity for scanning (updated from DashboardActivity)
        startNewScanButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        connectionStatusButton.setOnClickListener {
            Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show()
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun getNameFromLocal(email: String): String {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "users",
            arrayOf("name"),
            "email = ?",
            arrayOf(email),
            null, null, null
        )
        var name = "User"
        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }
        cursor.close()
        return name
    }

    private fun loadHistory(email: String, container: LinearLayout, lastCheckupTv: TextView) {
        val history = dbHelper.getHealthHistory(email)
        container.removeAllViews()

        if (history.isNotEmpty()) {
            val last = history[0]
            lastCheckupTv.text = "Last: ${last["date"]} | HR: ${last["hr"]} | SpO2: ${last["spo2"]}% | Temp: ${last["temp"]}°C | BMI: ${last["bmi"]} | ECG: ${last["ecg"]} | Stress: ${last["stress"]} | Resp: ${last["respiratory"]} | H: ${last["height"]}cm"

            for (item in history) {
                val textView = TextView(this)
                val text = "${item["date"]} - HR: ${item["hr"]}, SpO2: ${item["spo2"]}%, Temp: ${item["temp"]}°C, BMI: ${item["bmi"]}, ECG: ${item["ecg"]}, Stress: ${item["stress"]}, Resp: ${item["respiratory"]}, H: ${item["height"]}cm"
                textView.text = text
                textView.setPadding(0, 8, 0, 8)
                textView.setTextColor(android.graphics.Color.BLACK)
                container.addView(textView)
                
                val view = android.view.View(this)
                view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                view.setBackgroundColor(android.graphics.Color.LTGRAY)
                container.addView(view)
            }
        } else {
            val textView = TextView(this)
            textView.text = "No records yet."
            container.addView(textView)
        }
    }

    private fun checkEspStatus(button: Button) {
        val request = Request.Builder().url("http://192.168.4.1/status").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    button.text = "Device Status: Disconnected"
                    button.setTextColor(android.graphics.Color.RED)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    button.text = "ESP Status: Connected"
                    button.setTextColor(android.graphics.Color.GREEN)
                }
            }
        })
    }
}
