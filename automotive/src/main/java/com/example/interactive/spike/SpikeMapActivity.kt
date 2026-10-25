package com.example.interactive.spike

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

// MapLibre 지도 표시 실험 (spike 브랜치 전용). 키 없이 쓸 수 있는 OpenFreeMap 스타일을 띄운다.
class SpikeMapActivity : Activity() {

    private lateinit var mapView: MapView
    private val startedAt = SystemClock.elapsedRealtime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        mapView = MapView(this)
        setContentView(mapView)
        mapView.onCreate(savedInstanceState)

        val style = intent.getStringExtra(EXTRA_STYLE) ?: DEFAULT_STYLE
        mapView.getMapAsync { map ->
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(37.7519, 128.8961)) // 강릉
                .zoom(13.0)
                .build()
            map.setStyle(style) {
                Log.i(SpikeActivity.TAG, "MAP_STYLE_LOADED $style ${SystemClock.elapsedRealtime() - startedAt}ms")
            }
        }
        mapView.addOnDidFinishRenderingMapListener { fully ->
            if (fully) Log.i(SpikeActivity.TAG, "MAP_RENDERED ${SystemClock.elapsedRealtime() - startedAt}ms")
        }
        mapView.addOnDidFailLoadingMapListener { Log.i(SpikeActivity.TAG, "MAP_FAILED $it") }
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState) }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }

    companion object {
        const val EXTRA_STYLE = "style"
        const val DEFAULT_STYLE = "https://tiles.openfreemap.org/styles/liberty"
    }
}
