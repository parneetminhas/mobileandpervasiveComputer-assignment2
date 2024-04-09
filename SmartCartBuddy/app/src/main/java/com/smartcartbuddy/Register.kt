package com.smartcartbuddy

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Registering...")
    }

    fun signUp(view: View) {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        progressDialog.show()

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    // Sign up success
                    Toast.makeText(this@Register, "Sign up successful. Log in to proceed.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Register, LogIn::class.java))
                    finish()
                } else {
                    // Sign up failed
                    Toast.makeText(this@Register, "Sign up failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun logIn(view: View) {
        startActivity(Intent(this@Register, LogIn::class.java))
        finish()
    }
}

