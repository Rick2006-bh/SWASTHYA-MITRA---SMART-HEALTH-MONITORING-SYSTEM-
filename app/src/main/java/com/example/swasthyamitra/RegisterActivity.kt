package com.example.swasthyamitra

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        dbHelper = DatabaseHelper(this)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val dobEditText = findViewById<EditText>(R.id.dobEditText)
        val genderRadioGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginText = findViewById<TextView>(R.id.loginText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        dobEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                dobEditText.setText(date)
            }, year, month, day)
            datePickerDialog.show()
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim().lowercase()
            val password = passwordEditText.text.toString().trim()
            val dob = dobEditText.text.toString().trim()
            val selectedGenderId = genderRadioGroup.checkedRadioButtonId

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || dob.isEmpty() || selectedGenderId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            registerButton.isEnabled = false

            val genderRadioButton = findViewById<RadioButton>(selectedGenderId)
            val gender = genderRadioButton.text.toString()

            // 1. Create user in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // 2. Save additional details to Firestore (Cloud)
                            val user = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "dob" to dob,
                                "gender" to gender
                            )
                            db.collection("users").document(userId)
                                .set(user, SetOptions.merge())
                                .addOnSuccessListener {
                                    // 3. Save to local SQLite as backup
                                    dbHelper.addUser(name, email, password, gender, dob)
                                    
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    progressBar.visibility = View.GONE
                                    registerButton.isEnabled = true
                                    Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        registerButton.isEnabled = true
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
