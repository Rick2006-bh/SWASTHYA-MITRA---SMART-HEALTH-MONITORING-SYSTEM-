package com.example.swasthyamitra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameText = findViewById<TextView>(R.id.nameText)
        val genderText = findViewById<TextView>(R.id.genderText)
        val ageText = findViewById<TextView>(R.id.ageText)
        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        val heightEditText = findViewById<EditText>(R.id.heightEditText)
        val startButton = findViewById<Button>(R.id.startButton)
        val btnHealthDetails = findViewById<Button>(R.id.btnHealthDetails)

        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = currentUser.uid

        // 🔥 Fetch user data from Firestore
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "N/A"
                    val gender = document.getString("gender") ?: "N/A"
                    val dob = document.getString("dob") ?: ""

                    nameText.text = "Name: $name"
                    genderText.text = "Gender: $gender"

                    // Store for Chatbot
                    val sessionPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    sessionPref.edit().putString("userName", name).apply()

                    val healthPref = getSharedPreferences("HealthDetails", Context.MODE_PRIVATE)
                    healthPref.edit().putString("gender", gender).apply()

                    if (dob.isNotEmpty()) {
                        try {
                            val parts = dob.split("/")
                            val day = parts[0].toInt()
                            val month = parts[1].toInt() - 1
                            val year = parts[2].toInt()

                            val dobCal = Calendar.getInstance()
                            dobCal.set(year, month, day)
                            val today = Calendar.getInstance()
                            var age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
                            if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                                age--
                            }
                            ageText.text = "Age: $age"
                            sessionPref.edit().putString("userAge", age.toString()).apply()
                        } catch (e: Exception) {
                            ageText.text = "Age: Error"
                        }
                    }
                }
            }

        startButton.setOnClickListener {
            val weight = weightEditText.text.toString().trim()
            val height = heightEditText.text.toString().trim()
            
            if (weight.isEmpty() || height.isEmpty()) {
                Toast.makeText(this, "Please enter both weight and height", Toast.LENGTH_SHORT).show()
            } else {
                // Save to SharedPreferences for BMI calculation in Dashboard
                val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("user_weight", weight)
                    putString("user_height", height)
                    apply()
                }
                
                startActivity(Intent(this, DashboardActivity::class.java))
            }
        }

        btnHealthDetails.setOnClickListener {
            val intent = Intent(this, HealthDetailsActivity1::class.java)
            startActivity(intent)
        }
    }
}
