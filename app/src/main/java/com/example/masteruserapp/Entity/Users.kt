package com.example.masteruserapp.database.Entity

class Users(
    val userID: String,
    val username: String,
    val homeAddress : String,
    val doorMacaddress : String
) {
    constructor() : this("","", "","")
}