package com.example.roundupapp.utils

import java.security.SecureRandom

/**
 * Converts minor units (pence) to GBP string format
 * Example: 12345 -> "123.45"
 */
fun Int?.toGbp(): String {
  this?.let {
    val pounds = this / 100
    val pence = this % 100
    return "£$pounds.${"%02d".format(pence)}"
  }
  return "£0.00"
}

/**
 * Generates a random hexadecimal string of specified length
 * Note: For production use UUID.randomUUID() instead
 */
fun randomHex(length: Int, rnd: SecureRandom = SecureRandom()): String {
  val hex = "0123456789abcdef"
  return buildString { repeat(length) { append(hex[rnd.nextInt(16)]) } }
}
