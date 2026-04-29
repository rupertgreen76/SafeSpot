package com.example.safespotfinalapps

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class SearchBar : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var cardResult: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_searchbar)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Views
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        cardResult = findViewById(R.id.cardResult)

        // Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // Search Input Logic
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    Toast.makeText(this, "Searching for: $query", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        // Result Card Action
        cardResult.setOnClickListener {
            Toast.makeText(this, "Location selected from results", Toast.LENGTH_SHORT).show()
            // Here you could return this location to another activity or show it on a map
            finish()
        }
    }
}
