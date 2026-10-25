package com.example.interactive.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.interactive.RecordingEngine
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

private const val DONG_SOURCE = "dongs"
private const val CAR_SOURCE = "car"
private const val EMPTY = """{"type":"FeatureCollection","features":[]}"""

// docs/design/README.md 색 표(라이트): 내 땅 파랑, 친구 땅 주황, 주인 없음 회색 점선
private fun byKind(mine: Expression, friend: Expression, none: Expression): Expression =
    Expression.match(Expression.get("kind"), none, Expression.stop("mine", mine), Expression.stop("friend", friend))

/** 지도 탭: 강릉 지도 위에 동마다 주인 색을 칠하고, 차 위치를 점으로 보여 준다 */
@Composable
fun MapScreen(vm: RecordingEngine) {
    val context = LocalContext.current
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleReady by remember { mutableStateOf(false) }
    val mapView = remember { MapView(context) }

    // MapView는 화면의 생명 주기(켜짐·꺼짐)를 직접 알려 줘야 한다
    val lifecycle = (context as ComponentActivity).lifecycle
    DisposableEffect(lifecycle) {
        mapView.onCreate(Bundle())
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        // 이미 켜진 상태에서 만들어졌다면 따라잡는다
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.also { view ->
                    view.getMapAsync { m ->
                        map = m
                        m.cameraPosition = CameraPosition.Builder().target(LatLng(37.77, 128.90)).zoom(11.0).build()
                        m.setStyle(Style.Builder().fromUri(OfflineMap.STYLE_URL)) { style ->
                            addTerritoryLayers(style)
                            styleReady = true
                        }
                    }
                }
            },
        )
        DownloadCard(Modifier.align(Alignment.TopStart).padding(24.dp))
    }

    // 주인이 바뀌면 색을 다시 칠하고, 차가 움직이면 점을 옮긴다
    LaunchedEffect(styleReady, vm.territories) {
        if (!styleReady) return@LaunchedEffect
        map?.getStyle { it.getSourceAs<GeoJsonSource>(DONG_SOURCE)?.setGeoJson(TerritoryGeoJson.build(vm.dongs, vm.territories)) }
    }
    // P 카드의 "영토 보기": 그 동으로 지도를 옮긴다
    LaunchedEffect(styleReady, vm.focusDong) {
        val dong = vm.focusDong ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(dong.center.latitude, dong.center.longitude), 13.0))
        vm.clearFocus()
    }
    LaunchedEffect(styleReady, vm.state?.location) {
        val location = vm.state?.location
        if (!styleReady || location == null) return@LaunchedEffect
        map?.getStyle {
            it.getSourceAs<GeoJsonSource>(CAR_SOURCE)?.setGeoJson(
                """{"type":"Feature","geometry":{"type":"Point","coordinates":[${location.longitude},${location.latitude}]}}"""
            )
        }
    }
}

private fun addTerritoryLayers(style: Style) {
    style.addSource(GeoJsonSource(DONG_SOURCE, EMPTY))
    style.addLayer(
        FillLayer("dong-fill", DONG_SOURCE).withProperties(
            PropertyFactory.fillColor(
                byKind(
                    mine = Expression.rgba(47, 107, 255, 0.22f),
                    friend = Expression.rgba(240, 138, 36, 0.22f),
                    none = Expression.rgba(0, 0, 0, 0f),
                )
            )
        )
    )
    // 선: 내 땅·친구 땅은 실선, 주인 없음은 점선 (점선은 값마다 바꿀 수 없어 층을 나눈다)
    style.addLayer(
        LineLayer("dong-line", DONG_SOURCE).withFilter(Expression.neq(Expression.get("kind"), "none")).withProperties(
            PropertyFactory.lineColor(byKind(Expression.rgb(47, 107, 255), Expression.rgb(217, 112, 14), Expression.rgb(154, 165, 177))),
            PropertyFactory.lineWidth(3f),
        )
    )
    style.addLayer(
        LineLayer("dong-line-none", DONG_SOURCE).withFilter(Expression.eq(Expression.get("kind"), "none")).withProperties(
            PropertyFactory.lineColor(Expression.rgb(154, 165, 177)),
            PropertyFactory.lineWidth(2f),
            PropertyFactory.lineDasharray(arrayOf(5f, 4f)),
        )
    )
    style.addSource(GeoJsonSource(CAR_SOURCE, EMPTY))
    style.addLayer(
        CircleLayer("car-dot", CAR_SOURCE).withProperties(
            PropertyFactory.circleRadius(9f),
            PropertyFactory.circleColor(Expression.rgb(47, 107, 255)),
            PropertyFactory.circleStrokeColor(Expression.rgb(255, 255, 255)),
            PropertyFactory.circleStrokeWidth(3f),
        )
    )
}

// 개발용: 강릉 지도를 아직 안 받았을 때만 보이는 카드. 받은 뒤에는 사라진다
@Composable
private fun DownloadCard(modifier: Modifier) {
    val context = LocalContext.current
    var downloaded by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { OfflineMap.hasRegion(context) { downloaded = it } }
    if (downloaded) return
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("강릉 지도를 미리 받아 둘까요?", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { OfflineMap.download(context, context.resources.displayMetrics.density) { status = it } },
                modifier = Modifier.heightIn(min = 64.dp),
            ) { Text("강릉 지도 미리 받기") }
            if (status.isNotEmpty()) Text(status, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
