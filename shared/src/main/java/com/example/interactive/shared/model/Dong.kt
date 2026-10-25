package com.example.interactive.shared.model

import com.example.interactive.shared.vehicle.LatLng

/** 행정동 하나. code는 행정안전부 행정기관코드(10자리), polygons는 경계의 바깥 고리들(섬·떨어진 땅이 있으면 여러 개) */
data class Dong(
    val code: String,
    val name: String,
    val center: LatLng,
    val polygons: List<List<LatLng>>,
)

/** 위치가 어느 행정동 안인지 찾는다. 경계 밖(강릉이 아닌 곳 등)이면 null */
class DongLocator(private val dongs: List<Dong>) {

    private class Box(val dong: Dong, val minLat: Double, val maxLat: Double, val minLon: Double, val maxLon: Double)

    // 느린 계산(점이 폴리곤 안인지)을 하기 전에 네모 범위로 먼저 거른다
    private val boxes = dongs.map { dong ->
        val points = dong.polygons.flatten()
        Box(
            dong,
            points.minOf { it.latitude }, points.maxOf { it.latitude },
            points.minOf { it.longitude }, points.maxOf { it.longitude },
        )
    }

    fun find(location: LatLng): Dong? = boxes.firstOrNull { box ->
        location.latitude in box.minLat..box.maxLat && location.longitude in box.minLon..box.maxLon &&
            box.dong.polygons.any { contains(it, location) }
    }?.dong

    fun codeAt(location: LatLng): String? = find(location)?.code

    fun byCode(code: String): Dong? = dongs.firstOrNull { it.code == code }

    // 반직선 교차법: 점에서 오른쪽으로 쏜 선이 경계를 홀수 번 지나면 안쪽
    private fun contains(ring: List<LatLng>, p: LatLng): Boolean {
        var inside = false
        var j = ring.size - 1
        for (i in ring.indices) {
            val a = ring[i]
            val b = ring[j]
            if ((a.latitude > p.latitude) != (b.latitude > p.latitude) &&
                p.longitude < (b.longitude - a.longitude) * (p.latitude - a.latitude) / (b.latitude - a.latitude) + a.longitude
            ) inside = !inside
            j = i
        }
        return inside
    }
}
