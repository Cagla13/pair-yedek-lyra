package com.example.lyraapp.data.membership

data class CheckoutCardDetails(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String,
)
