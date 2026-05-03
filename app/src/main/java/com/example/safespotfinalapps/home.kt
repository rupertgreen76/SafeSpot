package com.example.safespotfinalapps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class Home : AppCompatActivity() {

    private lateinit var btnAddPost: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var tabHome: TextView
    private lateinit var tabProfile: TextView
    private lateinit var tabSafetyGuide: TextView
    private lateinit var recyclerPosts: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private lateinit var db: FirebaseFirestore
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        btnAddPost = findViewById(R.id.btnAddPost)
        btnSearch = findViewById(R.id.btnSearch)
        tabHome = findViewById(R.id.tabHome)
        tabProfile = findViewById(R.id.tabProfile)
        tabSafetyGuide = findViewById(R.id.tabSafetyGuide)
        recyclerPosts = findViewById(R.id.recyclerPosts)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyState = findViewById(R.id.emptyState)

        postAdapter = PostAdapter(postList)
        recyclerPosts.layoutManager = LinearLayoutManager(this)
        recyclerPosts.adapter = postAdapter

        swipeRefresh.setColorSchemeColors(
            android.graphics.Color.parseColor("#1a73e8")
        )

        // FIX: Swipe-to-refresh now properly restarts the listener
        swipeRefresh.setOnRefreshListener {
            listenerRegistration?.remove()
            listenerRegistration = null
            startRealtimeListener()
        }

        btnAddPost.setOnClickListener {
            startActivity(Intent(this, CreatePostAnonymously::class.java))
        }

        btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchBar::class.java))
        }

        tabHome.setOnClickListener {
            recyclerPosts.scrollToPosition(0)
        }

        tabProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        tabSafetyGuide.setOnClickListener {
            val intent = Intent(this, SafetyGuide::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    // FIX: Restart listener when returning to this screen
    override fun onResume() {
        super.onResume()
        if (listenerRegistration == null) {
            startRealtimeListener()
        }
    }

    // FIX: Remove listener when leaving this screen to avoid duplicates
    override fun onPause() {
        super.onPause()
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    private fun updateEmptyState() {
        if (postList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            swipeRefresh.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            swipeRefresh.visibility = View.VISIBLE
        }
    }

    private fun parsePost(document: com.google.firebase.firestore.DocumentSnapshot): Post {
        return Post(
            id = document.id,
            description = document.getString("description") ?: "",
            isAnonymous = document.getBoolean("isAnonymous") ?: false,
            userEmail = document.getString("userEmail") ?: "Unknown",
            timestamp = document.getTimestamp("timestamp"),
            location = document.getString("location") ?: "",
            latitude = document.getDouble("latitude") ?: 0.0,
            longitude = document.getDouble("longitude") ?: 0.0,
            disasterType = document.getString("disasterType") ?: "",
            imageUrl = document.getString("imageUrl")
                ?: document.getString("imageUri")
                ?: ""
        )
    }

    private fun startRealtimeListener() {
        listenerRegistration = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    if (error.message?.contains("index") == true) {
                        // Fallback without ordering if index doesn't exist
                        db.collection("posts")
                            .get()
                            .addOnSuccessListener { documents ->
                                postList.clear()
                                for (document in documents) {
                                    postList.add(parsePost(document))
                                }
                                postAdapter.notifyDataSetChanged()
                                updateEmptyState()
                            }
                    } else {
                        Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show()
                    }
                    swipeRefresh.isRefreshing = false
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    postList.clear()
                    for (document in snapshots) {
                        postList.add(parsePost(document))
                    }
                    postAdapter.notifyDataSetChanged()
                    swipeRefresh.isRefreshing = false
                    updateEmptyState()
                }
            }
    }
}