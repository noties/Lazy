package ru.noties.lazy

import org.junit.Test
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestsHolder {

    @Test
    fun `null provider throws NPE`() {
        assertFailsWith<NullPointerException> {
            Lazy<Any>(null)
        }
    }

    @Test
    fun `returns the same value`() {
        val value = Any()
        assertEquals(value, value)
        val lazy = Lazy<Any>({ value })
        assertEquals(value, lazy.get())
        assertEquals(value, lazy.get())
    }

    @Test
    fun `provider is called once`() {
        var count = 0
        val lazy = Lazy({
            count += 1
            Any()
        })
        assertEquals(0, count)
        lazy.get()
        assertEquals(1, count)
        lazy.get()
        assertEquals(1, count)
    }

    @Test
    fun `isProviderCalled`() {
        val lazy = Lazy({ Any() })
        assertFalse(lazy.isProviderCalled)
        lazy.get()
        assertTrue(lazy.isProviderCalled)
    }

    @Test
    fun `depends on other lazy`() {
        val lazy = Lazy({ Any() })
        val lazy2 = Lazy({ lazy.get() })
        assertEquals(lazy.get(), lazy2.get())
    }

    @Test
    fun `naive threading`() {
        var count = 0
        val lazy = Lazy({ count += 1 })
        val executor = Executors.newFixedThreadPool(20)
        for (i in 0..1000) {
            executor.submit { lazy.get() }
        }
        assertEquals(1, count)
    }
}

