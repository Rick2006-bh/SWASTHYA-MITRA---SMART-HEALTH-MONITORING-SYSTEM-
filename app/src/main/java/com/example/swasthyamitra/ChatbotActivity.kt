package com.example.swasthyamitra

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChatbotActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var typingIndicator: TextView
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<MessageModel>()
    private var isSending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_chatbot)

        val rootLayout = findViewById<View>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        // Initial AI message
        if (messages.isEmpty()) {
            addAIMessage("Hello! I am Swasthya AI, your health assistant. I've analyzed your profile. How can I help you today?")
        }

        sendButton.setOnClickListener {
            val question = messageEditText.text.toString().trim()
            if (question.isNotEmpty() && !isSending) {
                performChat(question)
            } else if (isSending) {
                Toast.makeText(this, "Please wait for the AI to respond...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        typingIndicator = findViewById(R.id.typingIndicator)

        adapter = ChatAdapter(messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = adapter
    }

    private fun performChat(userMessage: String) {
        isSending = true
        addUserMessage(userMessage)
        messageEditText.text.clear()
        
        updateUIForLoading(true)

        // 1. Collect Data with robust defaults
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest@swasthya.com"
        val healthPrefs = getSharedPreferences("HealthDetails", MODE_PRIVATE)
        val userSession = getSharedPreferences("UserSession", MODE_PRIVATE)

        // Collect health profile from SharedPreferences or Intent
        val userProfile = UserProfile(
            name = userSession.getString("userName", "User") ?: "User",
            gender = healthPrefs.getString("gender", "Not Specified") ?: "Not Specified",
            age = userSession.getString("userAge", "25")?.toIntOrNull() ?: 25,
            weight = intent.getStringExtra("weight")?.toIntOrNull() 
                     ?: healthPrefs.getString("health_weight", "70")?.toIntOrNull() ?: 70,
            height = intent.getStringExtra("height")?.toIntOrNull() 
                     ?: healthPrefs.getString("health_height", "170")?.toIntOrNull() ?: 170,
            bmi = intent.getStringExtra("bmi")?.toDoubleOrNull() ?: 24.2,
            dietType = healthPrefs.getString("health_diet", "Standard/Balanced") ?: "Standard/Balanced",
            activityLevel = healthPrefs.getString("health_activity_level", "Moderate") ?: "Moderate",
            sleepQuality = healthPrefs.getString("health_sleep", "Good") ?: "Good",
            bloodPressure = healthPrefs.getString("health_bp", "120/80") ?: "120/80",
            bloodSugar = healthPrefs.getString("health_sugar", "90 mg/dL") ?: "90 mg/dL",
            stressLevel = intent.getStringExtra("stress") ?: "Normal",
            allergies = healthPrefs.getString("health_allergy", "None") ?: "None",
            smoking = if (healthPrefs.getBoolean("health_smoking", false)) "Yes" else "No",
            alcohol = healthPrefs.getString("health_alcohol", "No") ?: "No",
            chronicConditions = healthPrefs.getString("health_chronic", "None") ?: "None",
            recentHealthConcerns = healthPrefs.getString("health_recent", "None") ?: "None"
        )

        val request = ChatRequest(
            email = userEmail,
            message = userMessage,
            heartRate = intent.getStringExtra("heartRate")?.toIntOrNull() 
                        ?: healthPrefs.getString("last_heart_rate", "72")?.toIntOrNull() ?: 72,
            temperature = intent.getStringExtra("temp")?.toDoubleOrNull() ?: 36.6,
            ecg = intent.getStringExtra("ecg") ?: "Normal",
            userProfile = userProfile
        )

        // 2. API Call
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.sendMessage(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("SW_CHAT_LOG", "RESPONSE_SUCCESS: $body")

                    val aiReply = body?.reply?.trim()
                    
                    // Improved Response Handling
                    if (aiReply.isNullOrBlank() || aiReply.length < 10) {
                        addAIMessage("I processed your request, but I need more specific health details (like weight, height, or symptoms) to give a better recommendation. Could you provide those?")
                    } else {
                        addAIMessage(aiReply)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SW_CHAT_LOG", "RESPONSE_ERROR ($response.code()): $errorBody")
                    
                    // Handle Render cold start or server errors
                    if (response.code() == 503 || response.code() == 504 || response.code() == 502) {
                        addAIMessage("My server is currently waking up (Render cold start). This usually takes 30-60 seconds. Please try sending your message again.")
                    } else {
                        addAIMessage("I'm having some trouble connecting to my database. Please check your internet and try again.")
                    }
                }
            } catch (e: Exception) {
                Log.e("SW_CHAT_LOG", "CONNECTION_FAILURE: ${e.message}")
                addAIMessage("Connection timed out. The server might be offline or your internet is unstable. Please try again later.")
            } finally {
                updateUIForLoading(false)
            }
        }
    }

    private fun updateUIForLoading(isLoading: Boolean) {
        isSending = isLoading
        sendButton.isEnabled = !isLoading
        sendButton.alpha = if (isLoading) 0.5f else 1.0f
        typingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        messageEditText.isEnabled = !isLoading
    }

    private fun addUserMessage(text: String) {
        messages.add(MessageModel(text, "user"))
        adapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)
    }

    private fun addAIMessage(text: String) {
        messages.add(MessageModel(text, "ai"))
        adapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.smoothScrollToPosition(messages.size - 1)
    }
}
