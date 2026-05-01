package com.example.swasthyamitra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swasthyamitra.databinding.ActivityHealthDetails2Binding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class HealthDetailsActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityHealthDetails2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthDetails2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load existing data if any
        loadHealthData()

        binding.btnContinueExisting.setOnClickListener {
            if (validateFields()) {
                saveHealthData()
                Toast.makeText(this, "Details Saved Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please select all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEditProfile.setOnClickListener {
            // Optional: logic to allow editing or just stay here
            Toast.makeText(this, "You can edit details now", Toast.LENGTH_SHORT).show()
        }

        binding.btnDeleteData.setOnClickListener {
            deleteHealthData()
        }
    }

    private fun validateFields(): Boolean {
        return binding.cgSleepQuality.checkedChipId != -1 &&
                binding.cgStressLevel2.checkedChipId != -1 &&
                binding.cgAllergies.checkedChipId != -1 &&
                binding.cgAlcohol.checkedChipId != -1 &&
                binding.cgChronic.checkedChipId != -1 &&
                binding.cgConcerns.checkedChipId != -1
    }

    private fun saveHealthData() {
        val sharedPrefs = getSharedPreferences("HealthDetails", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        fun getSelectedChipText(chipGroup: ChipGroup): String {
            val checkedId = chipGroup.checkedChipId
            return if (checkedId != -1) {
                val chip = chipGroup.findViewById<Chip>(checkedId)
                chip.text.toString()
            } else {
                "Not Specified"
            }
        }

        editor.putString("health_sleep", getSelectedChipText(binding.cgSleepQuality))
        editor.putString("health_stress2", getSelectedChipText(binding.cgStressLevel2))
        editor.putString("health_allergy", getSelectedChipText(binding.cgAllergies))
        editor.putString("health_alcohol", getSelectedChipText(binding.cgAlcohol))
        editor.putString("health_chronic", getSelectedChipText(binding.cgChronic))
        editor.putString("health_recent", getSelectedChipText(binding.cgConcerns))
        editor.putBoolean("health_smoking", binding.switchSmoking.isChecked)

        val description = binding.etHealthDescription.text.toString().trim()
        editor.putString("health_description", description)

        editor.apply()
    }

    private fun loadHealthData() {
        val sharedPrefs = getSharedPreferences("HealthDetails", Context.MODE_PRIVATE)
        
        setChipSelection(binding.cgSleepQuality, sharedPrefs.getString("health_sleep", ""))
        setChipSelection(binding.cgStressLevel2, sharedPrefs.getString("health_stress2", ""))
        setChipSelection(binding.cgAllergies, sharedPrefs.getString("health_allergy", ""))
        setChipSelection(binding.cgAlcohol, sharedPrefs.getString("health_alcohol", ""))
        setChipSelection(binding.cgChronic, sharedPrefs.getString("health_chronic", ""))
        setChipSelection(binding.cgConcerns, sharedPrefs.getString("health_recent", ""))
        
        binding.switchSmoking.isChecked = sharedPrefs.getBoolean("health_smoking", false)
        binding.etHealthDescription.setText(sharedPrefs.getString("health_description", ""))
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

    private fun deleteHealthData() {
        val sharedPrefs = getSharedPreferences("HealthDetails", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        Toast.makeText(this, "All health data cleared", Toast.LENGTH_SHORT).show()
        finish()
    }
}
