package com.kiranawala.utils.extensions

fun Double.toIndianCurrency(): String {
    return "₹%.2f".format(this)
}

fun Double.roundToTwoDecimals(): Double {
    return kotlin.math.round(this * 100) / 100
}