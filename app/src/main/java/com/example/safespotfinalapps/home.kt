package com.example.safespotfinalapps

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Home : AppCompatActivity() {

    private lateinit var btnAddPost: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var tabHome: TextView
    private lateinit var tabProfile: TextView
    private lateinit var tabSafetyGuide: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Views
        btnAddPost = findViewById(R.id.btnAddPost)
        btnSearch = findViewById(R.id.btnSearch)
        tabHome = findViewById(R.id.tabHome)
        tabProfile = findViewById(R.id.tabProfile)
        tabSafetyGuide = findViewById(R.id.tabSafetyGuide)

        // Toolbar Actions
        btnAddPost.setOnClickListener {
            val intent = Intent(this, CreatePostAnonymously::class.java)
            startActivity(intent)
        }

        btnSearch.setOnClickListener {
            val intent = Intent(this, SearchBar::class.java)
            startActivity(intent)
        }

        // Navigation Tabs
        tabHome.setOnClickListener {
            // Already on Home
            Toast.makeText(this, "Home Feed Refreshed", Toast.LENGTH_SHORT).show()
        }

        tabProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        tabSafetyGuide.setOnClickListener {
            val intent = Intent(this, SafetyGuide::class.java)
            startActivity(intent)
        }
    }
}
