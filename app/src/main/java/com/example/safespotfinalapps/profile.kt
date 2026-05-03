package com.example.safespotfinalapps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {

    private lateinit var btnMenu: ImageView
    private lateinit var btnAddPostToolbar: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var tabHome: TextView
    private lateinit var tabProfile: TextView
    private lateinit var tabSafetyGuide: TextView
    private lateinit var btnAddPostMain: Button
    private lateinit var tvGreeting: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPostCount: TextView
    private lateinit var recyclerMyPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnMenu = findViewById(R.id.btnMenu)
        btnAddPostToolbar = findViewById(R.id.btnAddPostToolbar)
        btnSearch = findViewById(R.id.btnSearch)
        tabHome = findViewById(R.id.tabHome)
        tabProfile = findViewById(R.id.tabProfile)
        tabSafetyGuide = findViewById(R.id.tabSafetyGuide)
        btnAddPostMain = findViewById(R.id.btnAddPost)
        tvGreeting = findViewById(R.id.tvGreeting)
        tvEmail = findViewById(R.id.tvEmail)
        tvPostCount = findViewById(R.id.tvPostCount)
        recyclerMyPosts = findViewById(R.id.recyclerMyPosts)

        postAdapter = PostAdapter(
            posts = postList,
            showDeleteButton = true,
            onDeleteClick = { post -> deletePost(post) }
        )
        recyclerMyPosts.layoutManager = LinearLayoutManager(this)
        recyclerMyPosts.adapter = postAdapter

        loadUserInfo()
        loadMyPosts()

        btnMenu.setOnClickListener {
            val intent = Intent(this, Logout::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        btnAddPostToolbar.setOnClickListener { navigateToAddPost() }

        btnSearch.setOnClickListener {
            val intent = Intent(this, SearchBar::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        btnAddPostMain.setOnClickListener { navigateToAddPost() }

        tabHome.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        tabProfile.setOnClickListener {
            loadMyPosts()
            Toast.makeText(this, "Profile Refreshed", Toast.LENGTH_SHORT).show()
        }

        tabSafetyGuide.setOnClickListener {
            val intent = Intent(this, SafetyGuide::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    private fun loadUserInfo() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fullName = document.getString("fullName") ?: "User"
                    tvGreeting.text = "Hello, $fullName!"
                } else {
                    tvGreeting.text = "Hello!"
                }
                tvEmail.text = currentUser.email
            }
            .addOnFailureListener {
                tvGreeting.text = "Hello!"
                tvEmail.text = currentUser.email
            }
    }

    private fun loadMyPosts() {
        val currentUser = auth.currentUser ?: return

        // Load all posts where realUserId matches — covers both anonymous and public
        db.collection("posts")
            .whereEqualTo("realUserId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    postList.add(parsePost(document))
                }
                postList.sortByDescending { it.timestamp?.toDate() }
                tvPostCount.text = "${postList.size} post${if (postList.size != 1) "s" else ""}"
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Fallback to old method if realUserId field doesn't exist yet
                db.collection("posts")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        postList.clear()
                        for (document in documents) {
                            postList.add(parsePost(document))
                        }
                        postList.sortByDescending { it.timestamp?.toDate() }
                        tvPostCount.text = "${postList.size} post${if (postList.size != 1) "s" else ""}"
                        postAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to load your posts", Toast.LENGTH_SHORT).show()
                    }
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

    private fun deletePost(post: Post) {
        AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("posts").document(post.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Post deleted!", Toast.LENGTH_SHORT).show()
                        loadMyPosts()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
        loadMyPosts()
    }

    private fun navigateToAddPost() {
        val intent = Intent(this, CreatePostAnonymously::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }
}