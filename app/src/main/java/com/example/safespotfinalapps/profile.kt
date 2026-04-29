package com.example.safespotfinalapps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : AppCompatActivity() {

    private lateinit var btnMenu: ImageView
    private lateinit var btnAddPostToolbar: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var tabHome: TextView
    private lateinit var tabProfile: TextView
    private lateinit var tabSafetyGuide: TextView
    private lateinit var btnAddPostMain: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Views
        btnMenu = findViewById(R.id.btnMenu)
        btnAddPostToolbar = findViewById(R.id.btnAddPostToolbar)
        btnSearch = findViewById(R.id.btnSearch)
        tabHome = findViewById(R.id.tabHome)
        tabProfile = findViewById(R.id.tabProfile)
        tabSafetyGuide = findViewById(R.id.tabSafetyGuide)
        btnAddPostMain = findViewById(R.id.btnAddPost)

        // Toolbar Actions
        btnMenu.setOnClickListener {
            val intent = Intent(this, Logout::class.java)
            startActivity(intent)
        }

        btnAddPostToolbar.setOnClickListener {
            navigateToAddPost()
        }

        btnSearch.setOnClickListener {
            val intent = Intent(this, SearchBar::class.java)
            startActivity(intent)
        }

        // Main Actions
        btnAddPostMain.setOnClickListener {
            navigateToAddPost()
        }

        // Navigation Tabs
        tabHome.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        tabProfile.setOnClickListener {
            // Already on Profile
            Toast.makeText(this, "Profile Refreshed", Toast.LENGTH_SHORT).show()
        }

        tabSafetyGuide.setOnClickListener {
            val intent = Intent(this, SafetyGuide::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToAddPost() {
        val intent = Intent(this, CreatePostAnonymously::class.java)
        startActivity(intent)
    }
}
