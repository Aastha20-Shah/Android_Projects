package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.coffeeshoponline.databinding.ActivitySignUpBinding
import com.example.coffeeshoponline.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupKeyboardAutoScroll()
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        // Using your exact XML IDs
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, "Please accept the Terms & Policy", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegister.isEnabled = false // Prevent multiple clicks

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""

                    // 🔥 Pointing to your specific Realtime Database URL
                    val database = FirebaseDatabase
                        .getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
                        .reference

                    // Create the data map with the keys the Profile page expects
                    val userMap = HashMap<String, Any>()
                    userMap["name"] = name
                    userMap["email"] = email
                    userMap["phone"] = phone
                    userMap["firstOrder"] = true

                    // 🔥 CRITICAL: Write the data to "users" -> "UID"
                    database.child("users").child(uid).setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Database Write Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Auth Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    private fun setupKeyboardAutoScroll() {
        val scroll = binding.root as ScrollView // Assuming root is ScrollView

        // List all your EditTexts here
        val editTexts = listOf(binding.etName, binding.etPhone,binding.etEmail,binding.etPassword,binding.etConfirmPassword)

        for (editText in editTexts) {
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Delay slightly to let keyboard open, then scroll to the view
                    binding.root.postDelayed({
                        scroll.smoothScrollTo(0, editText.bottom)
                    }, 200)
                }
            }
        }
    }
}