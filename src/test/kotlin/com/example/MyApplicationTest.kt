package com.example

import kotlin.test.Test
import kotlin.test.assertEquals

class MyApplicationTest {
    @Test
    fun testGetGreeting() {
        assertEquals("Hello from Kotlin!", getGreeting())
    }
}
