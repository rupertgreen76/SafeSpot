package com.example.safespotfinalapps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class CreatePostAnonymously : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var switchAnonymous: SwitchMaterial
    private lateinit var btnLocate: Button
    private lateinit var etDescription: EditText
    private lateinit var btnChooseFile: Button
    private lateinit var btnPost: Button

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_createpostanonymously)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Activity Result Launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize Views
        btnBack = findViewById(R.id.btnBack)
        switchAnonymous = findViewById(R.id.switchAnonymous)
        btnLocate = findViewById(R.id.btnLocate)
        etDescription = findViewById(R.id.etDescription)
        btnChooseFile = findViewById(R.id.btnChooseFile)
        btnPost = findViewById(R.id.btnPost)

        // Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // Locate Button
        btnLocate.setOnClickListener {
            val intent = Intent(this, MyLocation::class.java)
            startActivity(intent)
        }

        // Choose File Button
        btnChooseFile.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Post Button
        btnPost.setOnClickListener {
            val description = etDescription.text.toString().trim()
            val isAnonymous = switchAnonymous.isChecked

            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            } else {
                val postType = if (isAnonymous) "Anonymously" else "Publicly"
                Toast.makeText(this, "Posting $postType: $description", Toast.LENGTH_LONG).show()
                //  you would typically send data to your backend/Firebase
                finish()
            }
        }
    }
}
