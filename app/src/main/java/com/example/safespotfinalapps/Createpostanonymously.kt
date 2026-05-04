package com.example.safespotfinalapps

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.pm.PackageManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class CreatePostAnonymously : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var switchAnonymous: SwitchMaterial
    private lateinit var btnLocate: Button
    private lateinit var etDescription: EditText
    private lateinit var btnChooseFile: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnPost: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCharCount: TextView
    private lateinit var spinnerDisasterType: Spinner
    private lateinit var ivPreview: ImageView
    private lateinit var tvImageStatus: TextView

    private lateinit var locationResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectedAddress: String = ""
    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0
    private var selectedImageBitmap: Bitmap? = null

    // ── Cloudinary config ──────────────────────────────────────────────────────

    private val CLOUDINARY_CLOUD_NAME = "dud3qnrw1"
    private val CLOUDINARY_UPLOAD_PRESET = "safespot_preset"
    // ──────────────────────────────────────────────────────────────────────────

    private val disasterTypes = listOf(
        "🌊 Flood",
        "🔥 Fire",
        "🌪️ Typhoon",
        "🌋 Earthquake",
        "⛰️ Landslide",
        "🌧️ Heavy Rain",
        "💨 Strong Winds",
        "🚨 Emergency",
        "⚠️ Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_createpostanonymously)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Location picker
        locationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedAddress = result.data?.getStringExtra("selected_address") ?: ""
                selectedLat = result.data?.getDoubleExtra("selected_lat", 0.0) ?: 0.0
                selectedLng = result.data?.getDoubleExtra("selected_lng", 0.0) ?: 0.0
                btnLocate.text = selectedAddress
            }
        }

        // Gallery picker
        galleryResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                    selectedImageBitmap = bitmap
                    ivPreview.setImageBitmap(bitmap)
                    ivPreview.visibility = View.VISIBLE
                    tvImageStatus.text = "✅ Image selected"
                    tvImageStatus.visibility = View.VISIBLE
                }
            }
        }

        // Camera
        cameraResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    selectedImageBitmap = it
                    ivPreview.setImageBitmap(it)
                    ivPreview.visibility = View.VISIBLE
                    tvImageStatus.text = "✅ Photo taken"
                    tvImageStatus.visibility = View.VISIBLE
                }
            }
        }

        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack = findViewById(R.id.btnBack)
        switchAnonymous = findViewById(R.id.switchAnonymous)
        btnLocate = findViewById(R.id.btnLocate)
        etDescription = findViewById(R.id.etDescription)
        btnChooseFile = findViewById(R.id.btnChooseFile)
        btnPost = findViewById(R.id.btnPost)
        progressBar = findViewById(R.id.progressBar)
        tvCharCount = findViewById(R.id.tvCharCount)
        spinnerDisasterType = findViewById(R.id.spinnerDisasterType)
        ivPreview = findViewById(R.id.ivPreview)
        tvImageStatus = findViewById(R.id.tvImageStatus)

        // Try to find camera button, may not exist in layout yet
        btnTakePhoto = try {
            findViewById(R.id.btnTakePhoto)
        } catch (e: Exception) {
            Button(this) // dummy button if not in layout
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, disasterTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDisasterType.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnLocate.setOnClickListener {
            val intent = Intent(this, MyLocation::class.java)
            locationResultLauncher.launch(intent)
        }

        // Gallery picker
        btnChooseFile.isEnabled = true
        btnChooseFile.text = "📷 Choose from Gallery"
        btnChooseFile.alpha = 1.0f
        btnChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryResultLauncher.launch(intent)
        }

        // Camera
        btnTakePhoto.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED -> {
                    launchCamera()
                }
                else -> {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }
        }

        etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCharCount.text = "$length/500"
                tvCharCount.setTextColor(
                    if (length >= 450)
                        android.graphics.Color.parseColor("#F44336")
                    else
                        android.graphics.Color.parseColor("#AAAAAA")
                )
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnPost.setOnClickListener {
            val description = etDescription.text.toString().trim()
            val isAnonymous = switchAnonymous.isChecked
            val disasterType = spinnerDisasterType.selectedItem.toString()

            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedAddress.isEmpty()) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "You must be logged in to post", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPost.isEnabled = false
            progressBar.visibility = View.VISIBLE

            // If image selected, upload to Cloudinary first
            if (selectedImageBitmap != null) {
                uploadImageToCloudinary(selectedImageBitmap!!) { imageUrl ->
                    savePostToFirestore(description, isAnonymous, disasterType, currentUser.uid, currentUser.email, imageUrl)
                }
            } else {
                savePostToFirestore(description, isAnonymous, disasterType, currentUser.uid, currentUser.email, "")
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            cameraResultLauncher.launch(intent)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Uploads [bitmap] to Cloudinary using an unsigned upload preset (multipart/form-data).
     * This avoids Base64 inflation and is the recommended approach for mobile clients.
     * Calls [onComplete] with the secure_url on success, or an empty string on failure.
     */
    private fun uploadImageToCloudinary(bitmap: Bitmap, onComplete: (String) -> Unit) {
        Thread {
            try {
                // 1. Compress bitmap to JPEG bytes
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val imageBytes = outputStream.toByteArray()

                // 2. Build multipart/form-data body manually
                val boundary = "SafeSpotBoundary${System.currentTimeMillis()}"
                val CRLF = "\r\n"
                val bodyStream = ByteArrayOutputStream()

                fun writeLine(s: String) = bodyStream.write((s + CRLF).toByteArray(Charsets.UTF_8))

                // -- upload_preset field
                writeLine("--$boundary")
                writeLine("Content-Disposition: form-data; name=\"upload_preset\"")
                writeLine("")
                writeLine(CLOUDINARY_UPLOAD_PRESET)

                // -- file field
                writeLine("--$boundary")
                writeLine("Content-Disposition: form-data; name=\"file\"; filename=\"post_image.jpg\"")
                writeLine("Content-Type: image/jpeg")
                writeLine("")
                bodyStream.write(imageBytes)
                writeLine("")

                // closing boundary
                writeLine("--$boundary--")

                val bodyBytes = bodyStream.toByteArray()

                // 3. Open connection to Cloudinary upload endpoint
                val uploadUrl = URL("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload")
                val connection = uploadUrl.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                connection.setRequestProperty("Content-Length", bodyBytes.size.toString())
                connection.connectTimeout = 20_000
                connection.readTimeout = 20_000

                connection.outputStream.use { it.write(bodyBytes) }

                // 4. Parse response
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)
                    // Use secure_url so images are always served over HTTPS
                    val imageUrl = json.getString("secure_url")
                    runOnUiThread { onComplete(imageUrl) }
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "No error body"
                    android.util.Log.e("Cloudinary", "Upload failed [$responseCode]: $errorBody")
                    runOnUiThread {
                        Toast.makeText(this, "Image upload failed, posting without image", Toast.LENGTH_SHORT).show()
                        onComplete("")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Cloudinary", "Upload exception", e)
                runOnUiThread {
                    Toast.makeText(this, "Image upload error: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete("")
                }
            }
        }.start()
    }

    private fun savePostToFirestore(
        description: String,
        isAnonymous: Boolean,
        disasterType: String,
        userId: String,
        userEmail: String?,
        imageUrl: String
    ) {
        val post = hashMapOf(
            "description" to description,
            "isAnonymous" to isAnonymous,
            "disasterType" to disasterType,
            "userId" to if (isAnonymous) "anonymous" else userId,
            "realUserId" to userId,
            "userEmail" to if (isAnonymous) "Anonymous" else userEmail,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "imageUrl" to imageUrl,
            "location" to selectedAddress,
            "latitude" to selectedLat,
            "longitude" to selectedLng
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Post shared successfully!", Toast.LENGTH_LONG).show()
                btnPost.isEnabled = true
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1500)
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnPost.isEnabled = true
                Toast.makeText(this, "Failed to post: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}