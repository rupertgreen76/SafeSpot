package com.example.safespotfinalapps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import java.util.Date

data class Post(
    val id: String = "",
    val description: String = "",
    val isAnonymous: Boolean = false,
    val userEmail: String = "",
    val timestamp: Timestamp? = null,
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val disasterType: String = ""
)

class PostAdapter(
    private val posts: MutableList<Post>,
    private val showDeleteButton: Boolean = false,
    private val onDeleteClick: ((Post) -> Unit)? = null
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReporterName: TextView = itemView.findViewById(R.id.tvReporterName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvDisasterType: TextView = itemView.findViewById(R.id.tvDisasterType)
        val btnDelete: TextView = itemView.findViewById(R.id.btnDelete)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.tvReporterName.text = if (post.isAnonymous) "Anonymous" else post.userEmail
        holder.tvDescription.text = post.description

        post.timestamp?.let {
            holder.tvTimestamp.text = getTimeAgo(it.toDate())
        }

        if (post.disasterType.isNotEmpty()) {
            holder.tvDisasterType.visibility = View.VISIBLE
            holder.tvDisasterType.text = post.disasterType
            holder.tvDisasterType.setBackgroundResource(getDisasterColor(post.disasterType))
        } else {
            holder.tvDisasterType.visibility = View.GONE
        }

        if (post.location.isNotEmpty()) {
            holder.tvLocation.visibility = View.VISIBLE
            holder.tvLocation.text = "📍 ${post.location}"
        } else {
            holder.tvLocation.visibility = View.GONE
        }

        holder.tvDescription.text = post.description

        // FIX: trim the URL and load with improved Glide settings
        val imageUrl = post.imageUrl.trim()
        if (imageUrl.isNotEmpty()) {
            holder.ivPostImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .timeout(15000)
                )
                .into(holder.ivPostImage)
        } else {
            holder.ivPostImage.visibility = View.GONE
            Glide.with(holder.itemView.context).clear(holder.ivPostImage)
        }

        if (showDeleteButton) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDeleteClick?.invoke(post)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }

    override fun getItemCount() = posts.size

    private fun getDisasterColor(disasterType: String): Int {
        return when {
            disasterType.contains("Flood") -> R.drawable.tag_blue
            disasterType.contains("Fire") -> R.drawable.tag_red
            disasterType.contains("Typhoon") -> R.drawable.tag_purple
            disasterType.contains("Earthquake") -> R.drawable.tag_brown
            disasterType.contains("Landslide") -> R.drawable.tag_brown
            disasterType.contains("Rain") -> R.drawable.tag_blue
            disasterType.contains("Wind") -> R.drawable.tag_purple
            else -> R.drawable.disaster_tag_background
        }
    }

    private fun getTimeAgo(date: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            weeks < 4 -> "${weeks}w ago"
            months < 12 -> "${months}mo ago"
            else -> "${years}y ago"
        }
    }
}