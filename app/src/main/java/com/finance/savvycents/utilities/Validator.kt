package com.finance.savvycents.utilities

sealed class Validator(val errorMsg: String? = null) {
    class Success() : Validator(null)
    class Error(errorMsg: String) : Validator(errorMsg)
}