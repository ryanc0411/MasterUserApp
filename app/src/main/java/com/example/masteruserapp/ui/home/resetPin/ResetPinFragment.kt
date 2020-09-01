package com.example.masteruserapp.ui.resetPin

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.masteruserapp.R
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_resetpin.*
import kotlinx.android.synthetic.main.fragment_resetpin.passwordEditText
import kotlinx.android.synthetic.main.fragment_resetpin.retypePasswordEditText
import kotlinx.android.synthetic.main.fragment_resetpin.view.*

class ResetPinFragment : Fragment() {

    lateinit var oldpassword: EditText
    lateinit var  passwordText: EditText
    lateinit var retypeText: EditText
    lateinit var ref: DatabaseReference


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val root = inflater.inflate(R.layout.fragment_resetpin, container, false)

        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("DoorMacAddress", Context.MODE_PRIVATE )


        // initiate the firebase database
        ref = FirebaseDatabase.getInstance().getReference("door").child(sharedPreferences.getString("ID","DoorLock MasterApp").toString())

        // get those edit text and save into a variable
        oldpassword = root.oldpasswordText
        passwordText = root.passwordEditText
        retypeText = root.retypePasswordEditText



        root.checkBtn.setOnClickListener {
           ref.child("pinNumber").addListenerForSingleValueEvent(object : ValueEventListener{
               override fun onCancelled(error: DatabaseError) {
                   TODO("Not yet implemented")
               }

               override fun onDataChange(snapshot: DataSnapshot) {
                  if(snapshot.getValue(String::class.java)!!.equals(oldpassword.text.toString())){
                      oldpassword.visibility = View.GONE
                      passwordText.visibility = View.VISIBLE
                      retypeText.visibility = View.VISIBLE
                      checkBtn.visibility = View.GONE
                      resetBtn.visibility = View.VISIBLE
                  }
                   else{
                      Toast.makeText(activity,"Wrong Pin Number!",Toast.LENGTH_SHORT).show()
                  }
               }

           })
        }

        root.resetBtn.setOnClickListener {
            var isValid = checkUserInput()

            // once all the input fields are valid, then go into this if statement
            if (isValid) {
                var macaddress = sharedPreferences.getString("ID","DoorLock MasterApp")

                FirebaseDatabase.getInstance()
                    .getReference("door")
                    .child(macaddress.toString())
                    .child("pinNumber")
                    .setValue(passwordEditText.text.toString())

                val builder = AlertDialog.Builder(context)
                builder.setTitle("Reset Pin Successful!")
                builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                    view?.findNavController()?.navigate(R.id.nav_home)
                }
                builder.show()

            }
        }


            return root
    }

    private fun checkUserInput(): Boolean {
        var isValid = true

        if (passwordEditText.text.toString().isEmpty()) {
            passwordEditText.setError("New 6 Pin is required")
            isValid = false
        }
        if (retypePasswordEditText.text.toString().isEmpty()) {
            retypePasswordEditText.setError("Retype 6 pin is required")
            isValid = false
        }
        // ensure the password and retype password are same
        if (!passwordEditText.text.toString().equals(
                retypePasswordEditText.text.toString(), false) && isValid) {
            Toast.makeText(activity, "Pin do not match", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }



}



