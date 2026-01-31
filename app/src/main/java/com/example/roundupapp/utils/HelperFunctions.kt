package com.example.roundupapp.utils

fun Int?.toGbp(): String {
  this?.let {
    val pounds = this / 100
    val pence = this % 100
    return "£$pounds.${"%02d".format(pence)}"
  }
  return "£0.00"
}
