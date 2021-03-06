package test.utils

import kotlin.*
import kotlin.test.*
import org.junit.Test as test

class LazyTest {

    @test fun initializationCalledOnce() {
        var callCount = 0
        val lazyInt = lazy { ++callCount }

        assertEquals(0, callCount)
        assertFalse(lazyInt.isInitialized())
        assertEquals(1, lazyInt.value)
        assertEquals(1, callCount)
        assertTrue(lazyInt.isInitialized())

        lazyInt.value
        assertEquals(1, callCount)
    }

    @test fun alreadyInitialized() {
        val lazyInt = lazyOf(1)

        assertTrue(lazyInt.isInitialized())
        assertEquals(1, lazyInt.value)
    }


    @test fun lazyToString() {
        var callCount = 0
        val lazyInt = lazy { ++callCount }

        assertNotEquals("1", lazyInt.toString())
        assertEquals(0, callCount)

        assertEquals(1, lazyInt.value)
        assertEquals("1", lazyInt.toString())
        assertEquals(1, callCount)
    }
}