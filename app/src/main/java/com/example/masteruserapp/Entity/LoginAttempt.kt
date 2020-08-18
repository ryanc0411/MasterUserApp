package com.example.masteruserapp.database.Entity


class LoginAttempt(
    var currentAttempt: Int
) {

    constructor() : this(0)

}