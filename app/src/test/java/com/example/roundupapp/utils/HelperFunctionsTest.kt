package com.example.roundupapp.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.SecureRandom

class HelperFunctionsTest {

    @Test
    fun `toGbp converts minor units to formatted string`() {
        assertEquals("£1.00", 100.toGbp())
        assertEquals("£0.05", 5.toGbp())
        assertEquals("£0.00", 0.toGbp())
        assertEquals("£10.50", 1050.toGbp())
    }

    @Test
    fun `toGbp handles null`() {
        val nullInt: Int? = null
        assertEquals("£0.00", nullInt.toGbp())
    }

    @Test
    fun `randomHex generates string of correct length`() {
        assertEquals(10, randomHex(10).length)
        assertEquals(0, randomHex(0).length)
    }
}
