package com.example.roundupapp.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.security.SecureRandom

fun Int?.toGbp(): String {
  this?.let {
    val pounds = this / 100
    val pence = this % 100
    return "£$pounds.${"%02d".format(pence)}"
  }
  return "£0.00"
}

fun Int.toDecimal(
  scale: Int = 2,
  rounding: RoundingMode = RoundingMode.CEILING
): BigDecimal {
  return BigDecimal(this)
    .divide(BigDecimal(100))
    .setScale(scale, rounding)
}

fun randomHex(length: Int, rnd: SecureRandom = SecureRandom()): String {
  val hex = "0123456789abcdef"
  return buildString { repeat(length) { append(hex[rnd.nextInt(16)]) } }
}
