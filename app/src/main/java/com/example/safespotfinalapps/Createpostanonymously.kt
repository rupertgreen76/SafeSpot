package com.example.safespotfinalapps

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreatePostAnonymously : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var switchAnonymous: SwitchMaterial
    private lateinit var btnLocate: Button
    private lateinit var etDescription: EditText
    private lateinit var btnChooseFile: Button
    private lateinit var btnPost: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCharCount: TextView
    private lateinit var spinnerDisasterType: Spinner

    private lateinit var locationResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectedAddress: String = ""
    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0

    private val disasterTypes = listOf(
        "🌊 Flood",
        "🔥 Fire",
        "🌪️ Typhoon",
        "🌋 Earthquake",
        "⛰️ Landslide",
        "🌧️ Heavy Rain",
        "💨 Strong Winds",
        "🚨 Emergency",
        "⚠️ Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_createpostanonymously)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        locationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedAddress = result.data?.getStringExtra("selected_address") ?: ""
                selectedLat = result.data?.getDoubleExtra("selected_lat", 0.0) ?: 0.0
                selectedLng = result.data?.getDoubleExtra("selected_lng", 0.0) ?: 0.0
                btnLocate.text = selectedAddress
            }
        }

        btnBack = findViewById(R.id.btnBack)
        switchAnonymous = findViewById(R.id.switchAnonymous)
        btnLocate = findViewById(R.id.btnLocate)
        etDescription = findViewById(R.id.etDescription)
        btnChooseFile = findViewById(R.id.btnChooseFile)
        btnPost = findViewById(R.id.btnPost)
        progressBar = findViewById(R.id.progressBar)
        tvCharCount = findViewById(R.id.tvCharCount)
        spinnerDisasterType = findViewById(R.id.spinnerDisasterType)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, disasterTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDisasterType.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnLocate.setOnClickListener {
            val intent = Intent(this, MyLocation::class.java)
            locationResultLauncher.launch(intent)
        }

        btnChooseFile.isEnabled = false
        btnChooseFile.text = "Photo upload unavailable"
        btnChooseFile.alpha = 0.5f

        etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCharCount.text = "$length/500"
                tvCharCount.setTextColor(
                    if (length >= 450)
                        android.graphics.Color.parseColor("#F44336")
                    else
                        android.graphics.Color.parseColor("#AAAAAA")
                )
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnPost.setOnClickListener {
            val description = etDescription.text.toString().trim()
            val isAnonymous = switchAnonymous.isChecked
            val disasterType = spinnerDisasterType.selectedItem.toString()

            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedAddress.isEmpty()) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "You must be logged in to post", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPost.isEnabled = false
            progressBar.visibility = View.VISIBLE

            savePostToFirestore(description, isAnonymous, disasterType, currentUser.uid, currentUser.email)
        }
    }

    private fun savePostToFirestore(
        description: String,
        isAnonymous: Boolean,
        disasterType: String,
        userId: String,
        userEmail: String?
    ) {
        val post = hashMapOf(
            "description" to description,
            "isAnonymous" to isAnonymous,
            "disasterType" to disasterType,
            "userId" to if (isAnonymous) "anonymous" else userId,
            "realUserId" to userId,
            "userEmail" to if (isAnonymous) "Anonymous" else userEmail,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "imageUrl" to "",
            "location" to selectedAddress,
            "latitude" to selectedLat,
            "longitude" to selectedLng
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Post shared successfully!", Toast.LENGTH_LONG).show()
                btnPost.isEnabled = true
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1500)
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnPost.isEnabled = true
                Toast.makeText(this, "Failed to post: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}