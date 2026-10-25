package com.example.interactive.map

import com.example.interactive.shared.model.Dong
import com.example.interactive.shared.model.Territory
import com.example.interactive.shared.record.TerritoryScorer
import org.json.JSONArray
import org.json.JSONObject

/** 지도에 칠할 동 경계를 GeoJSON 글자로 만든다. kind: mine(내 땅) / friend(친구 땅) / none(주인 없음) */
object TerritoryGeoJson {

    fun build(dongs: List<Dong>, territories: List<Territory>): String {
        val ownerByDong = territories.associate { it.dongCode to it.ownerId }
        val features = JSONArray()
        dongs.forEach { dong ->
            val owner = ownerByDong[dong.code]
            val kind = when (owner) {
                null -> "none"
                TerritoryScorer.MY_ID -> "mine"
                else -> "friend"
            }
            val polygons = JSONArray()
            dong.polygons.forEach { ring ->
                val points = JSONArray()
                ring.forEach { points.put(JSONArray().put(it.longitude).put(it.latitude)) }
                polygons.put(JSONArray().put(points)) // 폴리곤 하나 = 바깥 고리 하나
            }
            features.put(
                JSONObject()
                    .put("type", "Feature")
                    .put("properties", JSONObject().put("code", dong.code).put("name", dong.name).put("kind", kind))
                    .put("geometry", JSONObject().put("type", "MultiPolygon").put("coordinates", polygons))
            )
        }
        return JSONObject().put("type", "FeatureCollection").put("features", features).toString()
    }
}
