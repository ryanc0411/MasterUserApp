@file:Suppress("DEPRECATION")

package com.example.masteruserapp.ui.login

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.masteruserapp.Common.Common
import com.example.masteruserapp.R
import com.example.masteruserapp.UserActivity
import com.example.masteruserapp.database.Entity.LoginAttempt
import com.example.masteruserapp.database.Entity.Users
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.greenrobot.eventbus.EventBus


class LoginFragment : Fragment() {

    lateinit var emailText: EditText
    lateinit var  passwordText: EditText
    lateinit var ref: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login, container, false)
        root.registerBtn.setOnClickListener{view : View ->
            view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)}




        // initiate the firebase database
        ref = FirebaseDatabase.getInstance().getReference("users")

        emailText = root.email_editText_login
        passwordText = root.password_editText_login

        root.button_login.setOnClickListener{
            val loading = ProgressDialog(context)
            loading.setMessage("Loading...")
            loading.show()

            login(loading)
        }

        root.forgetpass.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("\t\t\t\t\tForgot Password?? \n \t\t\t\t\tType your EMAIL here.\n")
            val view = layoutInflater.inflate(R.layout.fragment_forgetpassword,null)
            val username = view.findViewById<EditText>(R.id.emailofuser)
            builder.setView(view)
            builder.setPositiveButton("Reset", DialogInterface.OnClickListener { _, _ ->
                forgotPassword(username)
            })
            builder.setNegativeButton("close", DialogInterface.OnClickListener { _, _ ->  })
            builder.show()
        }



        return root
    }

    private  fun forgotPassword(username : EditText){

        if (username.text.toString().isEmpty()) {
            view?.let {
                Snackbar.make(it, "Please enter email!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            username.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()) {
            view?.let {
                Snackbar.make(it, "Please enter valid email!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            username.requestFocus()
            return
        }



        mAuth.sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    view?.let {
                        Snackbar.make(it, "RESET Email Sent!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    }
                }
                else {
                    view?.let {
                        Snackbar.make(it, "Email are not register yet!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    }
                }
            }.addOnFailureListener(OnFailureListener { e -> e.printStackTrace() })

    }

    private fun login(loading: ProgressDialog) {

        var isValid = true
        if (emailText.text.toString().trim().isEmpty()) {
            emailText.setError("Email is required")
            isValid = false
            loading.dismiss()
        }
        if (passwordText.text.toString().isEmpty()) {
            passwordText.setError("Password is required")
            isValid = false
            loading.dismiss()
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailText.text.toString()).matches()&&!emailText.text.toString().trim().isEmpty()) {
            view?.let {
                Snackbar.make(it, "Please enter valid email!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                isValid = false
                loading.dismiss()
            }
            emailText.requestFocus()
        }

        if (isValid) {
            mAuth.signInWithEmailAndPassword(
                emailText.text.toString(),
                passwordText.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    // get the created user
                    val user = mAuth.currentUser
                    // get the created user ID
                    val uid = user!!.uid

                    // check the login attempt limit
                    var registerEmail = emailText.text.toString()
                    var emailRoot = registerEmail.replace(".", " ")
                    ref.child("loginAttempt").child(emailRoot)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                val currentAttempt = p0.getValue(LoginAttempt::class.java)!!.currentAttempt
                                if (currentAttempt < 3) {
                                    // login attempt within valid range, proceed to login
                                    resetLoginAttempt()
                                    // determine the login user role
                                    gotoUserActivity(loading, uid)
                                } else {
                                    // login attempt exceeds 3
                                    // this account has been blocked
                                    loading.dismiss()
                                    val builder = AlertDialog.Builder(context)
                                    builder.setTitle("This email has been blocked!")
                                    builder.setMessage("Please contact to administrators 011-34567890!")
                                    builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                                        emailText.requestFocus()
                                    }
                                    builder.show()
                                }
                            }
                        })
                } else {
                    // check if email correct, but password incorrect
                    // then update the login attempt
                    checkEmailExistsOrNot()
                    loading.dismiss()
                    // pop up a error message
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Invalid email and password!")
                    builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                        emailText.requestFocus()
                    }
                    builder.show()
                }
            }
        }
    }

    private fun gotoUserActivity(loading: ProgressDialog, uid: String) {
        ref.child(uid).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                loading.dismiss()
                val urole = p0.getValue(Users::class.java)
                    // redirect to the customer page
                    val sharedPreferences:SharedPreferences = activity!!.getSharedPreferences("username1",Context.MODE_PRIVATE )
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("username",urole!!.username)
                    editor.commit()

                    Common.userName = urole.username
                    Common.homeAddress = urole.homeAddress
                    val intent = Intent(activity, UserActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    Toast.makeText(activity, "Login User Successful", Toast.LENGTH_SHORT).show()


            }
        })
    }

    private fun checkEmailExistsOrNot(){
        mAuth.fetchSignInMethodsForEmail(emailText.text.toString())
            .addOnCompleteListener(OnCompleteListener<SignInMethodQueryResult> { task ->
                if (task.result!!.signInMethods!!.size == 0) {
                    // email and password incorrect
                } else {
                    // email correct, but password incorrect
                    // update the login attempt
                    updateLoginAttempt()
                }
            }).addOnFailureListener(OnFailureListener { e -> e.printStackTrace() })
    }

    private fun resetLoginAttempt() {
        // reset the login attempt
        var registerEmail = emailText.text.toString()
        var emailRoot = registerEmail.replace(".", " ")
        ref.child("loginAttempt").child(emailRoot).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val updateAttempt = LoginAttempt(0)
                ref.child("loginAttempt").child(emailRoot).setValue(updateAttempt)
            }
        })
    }

    private fun updateLoginAttempt() {
        var registerEmail = emailText.text.toString()
        var emailRoot = registerEmail.replace(".", " ")
        ref.child("loginAttempt").child(emailRoot).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                var counter = p0.getValue(LoginAttempt::class.java)!!.currentAttempt
                counter++
                val updateAttempt = LoginAttempt(counter)
                    ref.child("loginAttempt").child(emailRoot).setValue(updateAttempt)

            }
        })
    }

}


