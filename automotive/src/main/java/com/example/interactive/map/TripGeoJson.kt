package com.example.interactive.map

import com.example.interactive.shared.vehicle.LatLng

/** 여행 경로(선)와 정차 지점(점)을 지도에 그릴 GeoJSON 글자로 만든다 */
object TripGeoJson {
    const val EMPTY = """{"type":"FeatureCollection","features":[]}"""

    fun line(points: List<LatLng>): String =
        if (points.size < 2) EMPTY
        else """{"type":"Feature","geometry":{"type":"LineString","coordinates":[${points.joinToString(",") { "[${it.longitude},${it.latitude}]" }}]}}"""

    fun points(points: List<LatLng>): String =
        if (points.isEmpty()) EMPTY
        else """{"type":"FeatureCollection","features":[${points.joinToString(",") {
            """{"type":"Feature","geometry":{"type":"Point","coordinates":[${it.longitude},${it.latitude}]}}"""
        }}]}"""
}
