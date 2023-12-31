package com.finance.savvycents.models

data class Transaction(
    val type: String,
    val location: String,
    val dateTime: String,
    val amount: Double,
    val category: String,
    val subCategory: String
){
    constructor() : this("testing","testing","testing",10.1,"testing","testing")
}
