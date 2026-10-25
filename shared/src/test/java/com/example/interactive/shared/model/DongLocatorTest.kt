package com.example.interactive.shared.model

import com.example.interactive.shared.vehicle.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DongLocatorTest {

    // 왼쪽 네모(A), 오른쪽 네모(B), 'ㄷ' 모양(C: 가운데가 움푹 파임)
    private fun square(lat: Double, lon: Double, size: Double = 1.0) = listOf(
        LatLng(lat, lon), LatLng(lat + size, lon), LatLng(lat + size, lon + size), LatLng(lat, lon + size), LatLng(lat, lon),
    )

    private val a = Dong("A", "가동", LatLng(0.5, 0.5), listOf(square(0.0, 0.0)))
    private val b = Dong("B", "나동", LatLng(0.5, 1.5), listOf(square(0.0, 1.0)))
    private val c = Dong(
        "C", "다동", LatLng(5.5, 0.5),
        listOf(
            listOf(
                LatLng(5.0, 0.0), LatLng(8.0, 0.0), LatLng(8.0, 3.0), LatLng(7.0, 3.0), LatLng(7.0, 1.0),
                LatLng(6.0, 1.0), LatLng(6.0, 3.0), LatLng(5.0, 3.0), LatLng(5.0, 0.0),
            )
        ),
    )
    private val islands = Dong("I", "섬동", LatLng(20.5, 20.5), listOf(square(20.0, 20.0), square(30.0, 30.0)))
    private val locator = DongLocator(listOf(a, b, c, islands))

    @Test
    fun findsTheDongContainingThePoint() {
        assertEquals("A", locator.codeAt(LatLng(0.5, 0.5)))
        assertEquals("B", locator.codeAt(LatLng(0.5, 1.5)))
    }

    @Test
    fun outsideEveryDongIsNull() {
        assertNull(locator.find(LatLng(-1.0, 0.5)))
        assertNull(locator.find(LatLng(0.5, 9.0)))
    }

    @Test
    fun concaveShapeExcludesTheNotch() {
        assertEquals("C", locator.codeAt(LatLng(5.5, 0.5))) // 'ㄷ'의 몸통
        assertNull(locator.find(LatLng(6.5, 2.0))) // 파인 부분은 밖
    }

    @Test
    fun dongWithSeveralPolygonsMatchesAnyOfThem() {
        assertEquals("I", locator.codeAt(LatLng(30.5, 30.5)))
        assertNull(locator.find(LatLng(25.0, 25.0)))
    }

    @Test
    fun byCodeFindsTheDong() {
        assertEquals("나동", locator.byCode("B")?.name)
        assertNull(locator.byCode("없음"))
    }
}
