package com.example.masteruserapp

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterPinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        signupBtn.setOnClickListener {
            var isValid = checkUserInput()

            // once all the input fields are valid, then go into this if statement
            if (isValid) {
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("DoorMacAddress", Context.MODE_PRIVATE )
                var macaddress = sharedPreferences.getString("ID","DoorLock MasterApp")
                FirebaseDatabase.getInstance()
                    .getReference("door")
                    .child(macaddress.toString())
                    .child("registerStatus")
                    .setValue(1)

                FirebaseDatabase.getInstance()
                    .getReference("door")
                    .child(macaddress.toString())
                    .child("pinNumber")
                    .setValue(passwordEditText.toString())

                goToUserActivity()

            }
        }

    }

    private fun checkUserInput(): Boolean {
        var isValid = true

        if (passwordEditText.text.toString().isEmpty()) {
            passwordEditText.setError("Password is required")
            isValid = false
        }
        if (retypePasswordEditText.text.toString().isEmpty()) {
            retypePasswordEditText.setError("Retype password is required")
            isValid = false
        }
        // ensure the password and retype password are same
        if (!passwordEditText.text.toString().equals(
                retypePasswordEditText.text.toString(), false) && isValid) {
            Toast.makeText(this, "Password do not match", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }

    private fun goToUserActivity() {
        val intent = Intent(this, UserActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}