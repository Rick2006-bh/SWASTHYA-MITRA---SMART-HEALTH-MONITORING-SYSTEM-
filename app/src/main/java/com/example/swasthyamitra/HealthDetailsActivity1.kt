package com.example.swasthyamitra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class HealthDetailsActivity1 : AppCompatActivity() {

    private lateinit var cgActivityLevel: ChipGroup
    private lateinit var cgExerciseHabit: ChipGroup
    private lateinit var cgDietary: ChipGroup
    private lateinit var cgWorkNature: ChipGroup
    private lateinit var cgBP: ChipGroup
    private lateinit var cgSugar: ChipGroup
    private lateinit var cgSurgery: ChipGroup
    private lateinit var cgStressLevel1: ChipGroup
    private lateinit var btnContinue1: Button
    private lateinit var tvGoToPage2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_health_details1)

        // Handle window insets for navigation bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        initViews()
        loadData()

        btnContinue1.setOnClickListener {
            if (validateFields()) {
                saveData()
                startActivity(Intent(this, HealthDetailsActivity2::class.java))
            } else {
                Toast.makeText(this, "Please select all options to continue", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToPage2.setOnClickListener {
            // Navigate to page 2 even without validation if user explicitly clicks the link
            startActivity(Intent(this, HealthDetailsActivity2::class.java))
        }
    }

    private fun initViews() {
        cgActivityLevel = findViewById(R.id.cgActivityLevel)
        cgExerciseHabit = findViewById(R.id.cgExerciseHabit)
        cgDietary = findViewById(R.id.cgDietary)
        cgWorkNature = findViewById(R.id.cgWorkNature)
        cgBP = findViewById(R.id.cgBP)
        cgSugar = findViewById(R.id.cgSugar)
        cgSurgery = findViewById(R.id.cgSurgery)
        cgStressLevel1 = findViewById(R.id.cgStressLevel1)
        btnContinue1 = findViewById(R.id.btnContinue1)
        tvGoToPage2 = findViewById(R.id.tvGoToPage2)
    }

    private fun validateFields(): Boolean {
        return cgActivityLevel.checkedChipId != -1 &&
                cgExerciseHabit.checkedChipId != -1 &&
                cgDietary.checkedChipId != -1 &&
                cgWorkNature.checkedChipId != -1 &&
                cgBP.checkedChipId != -1 &&
                cgSugar.checkedChipId != -1 &&
                cgSurgery.checkedChipId != -1 &&
                cgStressLevel1.checkedChipId != -1
    }

    private fun saveData() {
        val sharedPref = getSharedPreferences("HealthDetails", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("health_activity_level", findViewById<Chip>(cgActivityLevel.checkedChipId).text.toString())
            putString("health_exercise", findViewById<Chip>(cgExerciseHabit.checkedChipId).text.toString())
            putString("health_diet", findViewById<Chip>(cgDietary.checkedChipId).text.toString())
            putString("health_work", findViewById<Chip>(cgWorkNature.checkedChipId).text.toString())
            putString("health_bp", findViewById<Chip>(cgBP.checkedChipId).text.toString())
            putString("health_sugar", findViewById<Chip>(cgSugar.checkedChipId).text.toString())
            putString("health_surgery", findViewById<Chip>(cgSurgery.checkedChipId).text.toString())
            putString("health_stress", findViewById<Chip>(cgStressLevel1.checkedChipId).text.toString())
            apply()
        }
    }

    private fun loadData() {
        val sharedPref = getSharedPreferences("HealthDetails", Context.MODE_PRIVATE)
        setChipSelection(cgActivityLevel, sharedPref.getString("health_activity_level", ""))
        setChipSelection(cgExerciseHabit, sharedPref.getString("health_exercise", ""))
        setChipSelection(cgDietary, sharedPref.getString("health_diet", ""))
        setChipSelection(cgWorkNature, sharedPref.getString("health_work", ""))
        setChipSelection(cgBP, sharedPref.getString("health_bp", ""))
        setChipSelection(cgSugar, sharedPref.getString("health_sugar", ""))
        setChipSelection(cgSurgery, sharedPref.getString("health_surgery", ""))
        setChipSelection(cgStressLevel1, sharedPref.getString("health_stress", ""))
    }

    private fun setChipSelection(chipGroup: ChipGroup, value: String?) {
        if (value.isNullOrEmpty()) return
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.text.toString() == value) {
                chip.isChecked = true
                break
            }
        }
    }
}
