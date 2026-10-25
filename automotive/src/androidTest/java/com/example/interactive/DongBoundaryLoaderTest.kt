package com.example.interactive

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.interactive.shared.model.DongLocator
import com.example.interactive.shared.vehicle.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** assets/dongs_gangneung.json(실제 행정동 경계)을 읽어 위치→동 변환을 확인한다. 기기에서 돌린다 */
@RunWith(AndroidJUnit4::class)
class DongBoundaryLoaderTest {

    private val dongs = DongBoundaryLoader.load(InstrumentationRegistry.getInstrumentation().targetContext)
    private val locator = DongLocator(dongs)

    @Test
    fun readsGangneungsDongs() {
        assertEquals(21, dongs.size)
        assertEquals(dongs.size, dongs.map { it.code }.toSet().size)
    }

    @Test
    fun everyCenterIsInsideItsOwnDong() {
        dongs.forEach { assertEquals(it.name, it.code, locator.codeAt(it.center)) }
    }

    @Test
    fun knownPlacesMapToTheirDong() {
        assertEquals("경포동", locator.find(LatLng(37.7955, 128.8962))?.name) // 경포대 부근
        assertEquals("주문진읍", locator.find(LatLng(37.8914, 128.8225))?.name) // 주문진항 부근
        assertNotNull(locator.find(LatLng(37.7556, 128.8961))) // 강릉역 부근
    }

    @Test
    fun outsideGangneungIsNull() {
        assertNull(locator.find(LatLng(37.5665, 126.9780))) // 서울 시청
        assertNull(locator.find(LatLng(37.4563, 129.1656))) // 삼척
    }
}
