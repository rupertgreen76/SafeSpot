package com.example.safespotfinalapps

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SearchBar : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var recyclerSearch: RecyclerView
    private lateinit var tvNoResults: TextView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val allPosts = mutableListOf<Post>()
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_searchbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        recyclerSearch = findViewById(R.id.recyclerSearch)
        tvNoResults = findViewById(R.id.tvNoResults)

        postAdapter = PostAdapter(postList)
        recyclerSearch.layoutManager = LinearLayoutManager(this)
        recyclerSearch.adapter = postAdapter

        loadAllPosts()

        btnBack.setOnClickListener { finish() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPosts(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE) {
                filterPosts(etSearch.text.toString().trim())
                true
            } else {
                false
            }
        }
    }

    private fun loadAllPosts() {
        db.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                allPosts.clear()
                for (document in documents) {
                    val post = Post(
                        id = document.id,
                        description = document.getString("description") ?: "",
                        isAnonymous = document.getBoolean("isAnonymous") ?: false,
                        userEmail = document.getString("userEmail") ?: "Unknown",
                        timestamp = document.getTimestamp("timestamp"),
                        location = document.getString("location") ?: "",
                        latitude = document.getDouble("latitude") ?: 0.0,
                        longitude = document.getDouble("longitude") ?: 0.0,
                        disasterType = document.getString("disasterType") ?: "",
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    allPosts.add(post)
                }
            }
    }

    private fun filterPosts(query: String) {
        postList.clear()
        if (query.isEmpty()) {
            recyclerSearch.visibility = View.GONE
            tvNoResults.visibility = View.GONE
            return
        }

        val filtered = allPosts.filter { post ->
            post.description.contains(query, ignoreCase = true) ||
                    post.userEmail.contains(query, ignoreCase = true)
        }

        postList.addAll(filtered)
        postAdapter.notifyDataSetChanged()

        if (filtered.isEmpty()) {
            recyclerSearch.visibility = View.GONE
            tvNoResults.visibility = View.VISIBLE
        } else {
            recyclerSearch.visibility = View.VISIBLE
            tvNoResults.visibility = View.GONE
        }
    }
}