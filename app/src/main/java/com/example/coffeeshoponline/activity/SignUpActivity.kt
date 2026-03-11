package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
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
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show()
            return
        }

        if(password != confirmPassword){
            Toast.makeText(this,"Passwords do not match",Toast.LENGTH_SHORT).show()
            return
        }

        if(!binding.cbTerms.isChecked){
            Toast.makeText(this,"Accept Terms & Privacy Policy",Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->

                if(task.isSuccessful){

                    val uid = auth.currentUser!!.uid

                    val userMap = HashMap<String,String>()
                    userMap["name"] = name
                    userMap["email"] = email

                    FirebaseDatabase.getInstance()
                        .reference
                        .child("users")
                        .child(uid)
                        .setValue(userMap)

                    Toast.makeText(this,"Account Created",Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()

                }else{
                    Toast.makeText(this,task.exception!!.message,Toast.LENGTH_LONG).show()
                }

            }
    }
}