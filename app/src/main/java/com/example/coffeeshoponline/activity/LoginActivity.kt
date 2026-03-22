package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.coffeeshoponline.R
import com.example.coffeeshoponline.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupKeyboardAutoScroll()
        auth = FirebaseAuth.getInstance()
        binding.btnLogin.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {

                    if(it.isSuccessful){

                        Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()

                    }else{
                        Toast.makeText(this,"Login Failed",Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnLoginAsAdmin.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }

    }
    private fun setupKeyboardAutoScroll() {
        val scroll = binding.root as ScrollView // Assuming root is ScrollView

        // List all your EditTexts here
        val editTexts = listOf(binding.etEmail, binding.etPassword)

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
