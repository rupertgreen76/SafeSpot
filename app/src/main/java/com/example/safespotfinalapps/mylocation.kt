package com.example.safespotfinalapps

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MyLocation : AppCompatActivity() {

    private lateinit var ivBack: ImageButton
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button
    private lateinit var tvAddress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mylocation)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Views
        ivBack = findViewById(R.id.ivBack)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnCancel = findViewById(R.id.btnCancel)
        tvAddress = findViewById(R.id.tv_address)

        // Back button to return to the previous screen
        ivBack.setOnClickListener {
            finish()
        }

        // Confirm button logic
        btnConfirm.setOnClickListener {
            val selectedAddress = tvAddress.text.toString()
            Toast.makeText(this, "Location Confirmed: $selectedAddress", Toast.LENGTH_SHORT).show()
            
            // Here you can pass the address back to CreatePost or use it in the app
            // For now, we'll just finish the activity
            finish()
        }

        // Cancel button logic
        btnCancel.setOnClickListener {
            Toast.makeText(this, "Location selection cancelled", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
