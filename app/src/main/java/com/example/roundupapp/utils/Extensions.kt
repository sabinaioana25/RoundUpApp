package com.example.roundupapp.utils

import java.security.SecureRandom

/**
 * Converts minor units (pence) to GBP string format
 * Example: 12345 -> "123.45"
 */
fun Int?.toGbp(): String {
  val value = this ?: 0
  val pounds = value.div(100)
  val pence = value.rem(100)
  return "£$pounds.${"%02d".format(pence)}"
}

/**
 * Generates a random hexadecimal string of specified length
 * Note: For production use UUID.randomUUID() instead
 */
fun randomHex(length: Int, rnd: SecureRandom = SecureRandom()): String {
  val hex = "0123456789abcdef"
  return buildString { repeat(length) { append(hex[rnd.nextInt(16)]) } }
}

fun randomUuidV4(rnd: SecureRandom = SecureRandom()): String {
  val variantChars = "89ab"
  val variant = variantChars[rnd.nextInt(variantChars.length)]
  return buildString {
    append(randomHex(8, rnd)); append('-')
    append(randomHex(4, rnd)); append('-')
    append('4'); append(randomHex(3, rnd)); append('-')
    append(variant); append(randomHex(3, rnd)); append('-')
    append(randomHex(12, rnd))
  }
}
