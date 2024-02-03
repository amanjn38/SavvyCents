package com.finance.savvycents.models


data class User(
    val isLoggedIn: Boolean = false,
    val userId: String,
    val email: String?,
    val name: String,
    val phone: String?
){
    constructor() : this(false, "", null, "", null)
}