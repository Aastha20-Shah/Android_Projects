package com.example.coffeeshoponline.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshoponline.databinding.ViewholderAdminUserBinding
import com.example.coffeeshoponline.model.UserModel
import com.google.firebase.database.FirebaseDatabase

class AdminUserAdapter(private val users: List<UserModel>) :
    RecyclerView.Adapter<AdminUserAdapter.Viewholder>() {

    class Viewholder(val binding: ViewholderAdminUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderAdminUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val user = users[position]

        holder.binding.tvAdminUserName.text = user.name
        holder.binding.tvAdminUserEmail.text = user.email
        holder.binding.tvAdminUserPhone.text = user.phone

        val context = holder.itemView.context

        holder.binding.btnDeleteUser.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete ${user.name}?\nThis cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    FirebaseDatabase.getInstance().getReference("users").child(user.id).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        holder.binding.btnEditUser.setOnClickListener {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val inputName = EditText(context).apply {
                hint = "Name"
                setText(user.name)
            }
            val inputPhone = EditText(context).apply {
                hint = "Phone Number"
                setText(user.phone)
            }

            layout.addView(inputName)
            layout.addView(inputPhone)

            AlertDialog.Builder(context)
                .setTitle("Edit User Details")
                .setView(layout)
                .setPositiveButton("Save") { _, _ ->
                    val newName = inputName.text.toString().trim()
                    val newPhone = inputPhone.text.toString().trim()

                    if (newName.isNotEmpty() && newPhone.isNotEmpty()) {
                        val updates = mapOf(
                            "name" to newName,
                            "phone" to newPhone
                        )
                        FirebaseDatabase.getInstance().getReference("users")
                            .child(user.id).updateChildren(updates)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Details updated!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount(): Int = users.size
}
