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

        database = FirebaseDatabase
            .getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
            .reference
            .child("users")
            .child(currentUser.uid)

        loadUserData()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.saveProfileBtn.setOnClickListener {
            saveProfileChanges()
        }

        binding.changePasswordBtn.setOnClickListener {
            changePassword()
        }
    }

    // ---------------- LOAD USER DATA ----------------

    private fun loadUserData() {

        database.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    val name = snapshot.child("name").value?.toString() ?: ""
                    val email = snapshot.child("email").value?.toString() ?: ""
                    val phone = snapshot.child("phone").value?.toString() ?: ""

                    binding.profileName.setText(name)
                    binding.profileEmail.setText(email)
                    binding.profilePhone.setText(phone)

                    // Load Address
                    val address = snapshot.child("address")

                    binding.profileHouse.setText(address.child("house").value?.toString() ?: "")
                    binding.profileArea.setText(address.child("area").value?.toString() ?: "")
                    binding.profileLandmark.setText(address.child("landmark").value?.toString() ?: "")
                    binding.profileCity.setText(address.child("city").value?.toString() ?: "")
                    binding.profileState.setText(address.child("state").value?.toString() ?: "")
                    binding.profilePincode.setText(address.child("pincode").value?.toString() ?: "")
                    binding.profileCountry.setText(address.child("country").value?.toString() ?: "")

                } else {

                    binding.profileName.setText("")
                    binding.profileEmail.setText(auth.currentUser?.email ?: "")
                    binding.profilePhone.setText("")
                }
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(
                    this@ProfileActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // ---------------- SAVE PROFILE ----------------

    private fun saveProfileChanges() {

        val name = binding.profileName.text.toString()
        val phone = binding.profilePhone.text.toString()

        val house = binding.profileHouse.text.toString()
        val area = binding.profileArea.text.toString()
        val landmark = binding.profileLandmark.text.toString()
        val city = binding.profileCity.text.toString()
        val state = binding.profileState.text.toString()
        val pincode = binding.profilePincode.text.toString()
        val country = binding.profileCountry.text.toString()

        val updates = HashMap<String, Any>()

        updates["name"] = name
        updates["phone"] = phone

        val address = HashMap<String, Any>()
        address["house"] = house
        address["area"] = area
        address["landmark"] = landmark
        address["city"] = city
        address["state"] = state
        address["pincode"] = pincode
        address["country"] = country

        updates["address"] = address

        database.updateChildren(updates)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Profile updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                binding.root.clearFocus()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Update failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // ---------------- CHANGE PASSWORD ----------------

    private fun changePassword() {

        val oldPassword = binding.oldPassword.text.toString()
        val newPassword = binding.newPassword.text.toString()

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Enter both passwords", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {

            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, oldPassword)

            user.reauthenticate(credential)
                .addOnSuccessListener {

                    user.updatePassword(newPassword)
                        .addOnSuccessListener {

                            Toast.makeText(
                                this,
                                "Password updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            binding.oldPassword.setText("")
                            binding.newPassword.setText("")
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Failed to update password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Current password is incorrect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}