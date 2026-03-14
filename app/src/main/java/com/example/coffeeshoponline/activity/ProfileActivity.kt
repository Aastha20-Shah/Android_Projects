package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.coffeeshoponline.R
import com.example.coffeeshoponline.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Target the specific user folder in the database
        database = FirebaseDatabase
            .getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
            .reference
            .child("users")
            .child(currentUser.uid)

        loadUserData()

        // Match your Cart-style back button ID
        binding.backBtn.setOnClickListener { finish() }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Fetch keys saved in SignUpActivity
                    val name = snapshot.child("name").value?.toString() ?: "No Name"
                    val email = snapshot.child("email").value?.toString() ?: "No Email"
                    val phone = snapshot.child("phone").value?.toString() ?: "No Phone"

                    // Display in your Profile IDs
                    binding.profileName.text = name
                    binding.profileEmail.text = email
                    binding.profilePhone.text = phone
                } else {
                    binding.profileName.text = "Profile Not Found"
                    binding.profileEmail.text = auth.currentUser?.email
                    binding.profilePhone.text = "Data Missing"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}