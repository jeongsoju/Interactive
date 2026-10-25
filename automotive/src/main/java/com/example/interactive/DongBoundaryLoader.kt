package com.example.interactive

import android.content.Context
import com.example.interactive.shared.model.Dong
import com.example.interactive.shared.vehicle.LatLng
import org.json.JSONArray
import org.json.JSONObject

/**
 * assets/dongs_gangneung.json을 읽는다 (docs/data-sources.md: 통계청 SGIS, vuski/admdongkor ver20261001, CC BY 4.0).
 * 좌표는 [경도, 위도] 순서로 저장돼 있다.
 */
object DongBoundaryLoader {

    fun load(context: Context): List<Dong> {
        val text = context.assets.open("dongs_gangneung.json").bufferedReader().use { it.readText() }
        val array = JSONArray(text)
        return List(array.length()) { i ->
            val d = array.getJSONObject(i)
            Dong(
                code = d.getString("code"),
                name = d.getString("name"),
                center = LatLng(d.getDouble("lat"), d.getDouble("lon")),
                polygons = d.getJSONArray("polygons").let { polys ->
                    List(polys.length()) { p -> ring(polys.getJSONArray(p)) }
                },
            )
        }
    }

    private fun ring(points: JSONArray): List<LatLng> =
        List(points.length()) { i -> points.getJSONArray(i).let { LatLng(it.getDouble(1), it.getDouble(0)) } }
}
