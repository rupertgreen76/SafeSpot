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

class SafetyGuide : AppCompatActivity() {

    private lateinit var btnMenu: ImageView
    private lateinit var btnAddPost: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var tabHome: TextView
    private lateinit var tabProfile: TextView
    private lateinit var tabSafetyGuide: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_safetyguide)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Views
        btnMenu = findViewById(R.id.btnMenu)
        btnAddPost = findViewById(R.id.btnAddPost)
        btnSearch = findViewById(R.id.btnSearch)
        tabHome = findViewById(R.id.tabHome)
        tabProfile = findViewById(R.id.tabProfile)
        tabSafetyGuide = findViewById(R.id.tabSafetyGuide)

        // Toolbar Actions
        btnMenu.setOnClickListener {
            val intent = Intent(this, Logout::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        btnAddPost.setOnClickListener {
            val intent = Intent(this, CreatePostAnonymously::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        btnSearch.setOnClickListener {
            val intent = Intent(this, SearchBar::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        // Navigation Tabs
        tabHome.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
        }

        tabProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
        }

        tabSafetyGuide.setOnClickListener {
            // Already on Safety Guide
            Toast.makeText(this, "Safety Guide Refreshed", Toast.LENGTH_SHORT).show()
        }
    }
}
