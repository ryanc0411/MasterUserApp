package com.example.masteruserapp

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_main)

        sharedPreferences = this.getSharedPreferences("DoorMacAddress", Activity.MODE_PRIVATE)

        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        OneSignal.sendTag("User_ID","user2@gmail.com")

        FirebaseDatabase.getInstance()
            .getReference("door")
            .child(sharedPreferences.getString("ID","DoorLock MasterApp").toString())
            .child("loginStatus")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.getValue(Int::class.java)==1){
                            goToUserActivity()
                        }

                    }


                })

        button_login.setOnClickListener {
            FirebaseDatabase.getInstance()
                .getReference("door")
                .child(macaddress_editText_login.text.toString())
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                FirebaseDatabase.getInstance()
                                    .getReference("door")
                                    .child(macaddress_editText_login.text.toString())
                                    .child("registerStatus")
                                    .addListenerForSingleValueEvent(
                                        object : ValueEventListener {
                                            override fun onCancelled(error: DatabaseError) {
                                                TODO("Not yet implemented")
                                            }

                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if(snapshot.getValue(Int::class.java)==0)
                                                {
                                                    goToRegisterPinActivity()
                                                    val editor1: SharedPreferences.Editor = sharedPreferences.edit()
                                                    editor1.putString("ID", macaddress_editText_login.text.toString() )
                                                    editor1.commit()
                                                }
                                                else if (snapshot.getValue(Int::class.java)==1)
                                                {
                                                    goToUserActivity()
                                                    val editor1: SharedPreferences.Editor = sharedPreferences.edit()
                                                    editor1.putString("ID", macaddress_editText_login.text.toString() )
                                                    editor1.commit()
                                                }
                                            }
                                        })
                            } else {
                                showToast("Invalid Door Lock MAC ADDRESS")
                            }
                        }


                    })
        }
    }

    private fun showToast(message : String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }

    private fun goToUserActivity() {
        val intent = Intent(this, UserActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun goToRegisterPinActivity() {
    val intent = Intent(this, RegisterPinActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    }
}
