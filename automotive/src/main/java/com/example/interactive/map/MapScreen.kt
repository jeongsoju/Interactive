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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.interactive.RecordingEngine
import com.example.interactive.shared.record.TripEntry
import com.example.interactive.ui.theme.AppColors
import com.example.interactive.ui.theme.LocalAppColors
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

private const val DONG_SOURCE = "dongs"
private const val CAR_SOURCE = "car"
private const val ROUTE_SOURCE = "route"
private const val STOPS_SOURCE = "stops"
private const val EMPTY = """{"type":"FeatureCollection","features":[]}"""

private fun Color.expr(): Expression = Expression.color(toArgb())

private fun byKind(mine: Expression, friend: Expression, none: Expression): Expression =
    Expression.match(Expression.get("kind"), none, Expression.stop("mine", mine), Expression.stop("friend", friend))

/** 지도 탭: 강릉 지도 위에 동마다 주인 색을 칠하고, 차 위치를 점으로 보여 준다. 라이트·다크는 시스템을 따른다 */
@Composable
fun MapScreen(vm: RecordingEngine, highlight: TripEntry? = null, wide: Boolean = true) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    // 스타일이 (다시) 읽힐 때마다 올라간다. 값을 다시 밀어 넣는 효과가 이 번호를 보고 돈다
    var styleVersion by remember { mutableIntStateOf(0) }
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
                        m.cameraPosition = CameraPosition.Builder().target(LatLng(37.77, 128.90)).zoom(11.0).build()
                        map = m
                    }
                }
            },
        )
        DownloadCard(Modifier.align(Alignment.Center).padding(24.dp))
    }

    // 낮·밤이 바뀌면 스타일을 새로 읽고 영토 층을 다시 얹는다
    LaunchedEffect(map, colors.dark) {
        val m = map ?: return@LaunchedEffect
        m.setStyle(Style.Builder().fromUri(OfflineMap.styleUrl(colors.dark))) { style ->
            applyDesign(style, colors)
            addTerritoryLayers(style, colors)
            styleVersion++
        }
    }

    // 주인이 바뀌면 색을 다시 칠하고, 차가 움직이면 점을 옮긴다
    LaunchedEffect(styleVersion, vm.territories) {
        if (styleVersion == 0) return@LaunchedEffect
        map?.getStyle { it.getSourceAs<GeoJsonSource>(DONG_SOURCE)?.setGeoJson(TerritoryGeoJson.build(vm.dongs, vm.territories)) }
    }
    // P 카드의 "영토 보기": 그 동으로 지도를 옮긴다
    LaunchedEffect(styleVersion, vm.focusDong) {
        val dong = vm.focusDong ?: return@LaunchedEffect
        if (styleVersion == 0) return@LaunchedEffect
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(dong.center.latitude, dong.center.longitude), 13.0))
        vm.clearFocus()
    }
    // 기록 탭에서 고른 여행의 경로와 정차 지점 (진행 중인 여행이면 점이 늘 때마다 다시 그린다)
    LaunchedEffect(styleVersion, highlight) {
        if (styleVersion == 0) return@LaunchedEffect
        map?.getStyle {
            it.getSourceAs<GeoJsonSource>(ROUTE_SOURCE)?.setGeoJson(TripGeoJson.line(highlight?.path().orEmpty()))
            it.getSourceAs<GeoJsonSource>(STOPS_SOURCE)?.setGeoJson(TripGeoJson.points(highlight?.stopPoints().orEmpty()))
        }
    }
    // 여행을 고르면 그 경로가 패널에 가리지 않게 보이도록 카메라를 맞춘다 (고를 때 한 번만)
    LaunchedEffect(styleVersion, highlight?.key) {
        val points = highlight?.path().orEmpty()
        if (styleVersion == 0 || points.isEmpty()) return@LaunchedEffect
        val density = context.resources.displayMetrics.density
        val latLngs = points.map { LatLng(it.latitude, it.longitude) }
        val update = if (latLngs.size == 1) {
            CameraUpdateFactory.newLatLngZoom(latLngs.first(), 13.0)
        } else {
            val bounds = LatLngBounds.fromLatLngs(latLngs)
            val left = (if (wide) 700 else 40) * density
            val bottom = (if (wide) 160 else 560) * density
            CameraUpdateFactory.newLatLngBounds(bounds, left.toInt(), (120 * density).toInt(), (80 * density).toInt(), bottom.toInt())
        }
        map?.animateCamera(update)
    }
    LaunchedEffect(styleVersion, vm.state?.location) {
        val location = vm.state?.location
        if (styleVersion == 0 || location == null) return@LaunchedEffect
        map?.getStyle {
            it.getSourceAs<GeoJsonSource>(CAR_SOURCE)?.setGeoJson(
                """{"type":"Feature","geometry":{"type":"Point","coordinates":[${location.longitude},${location.latitude}]}}"""
            )
        }
    }
}

// 디자인 표의 땅·바다 색으로 바탕을 맞춘다 (스타일에 그 층이 없으면 건너뛴다)
private fun applyDesign(style: Style, colors: AppColors) {
    (style.getLayer("background") as? BackgroundLayer)?.setProperties(PropertyFactory.backgroundColor(colors.land.toArgb()))
    (style.getLayer("water") as? FillLayer)?.setProperties(PropertyFactory.fillColor(colors.water.toArgb()))
}

private fun addTerritoryLayers(style: Style, colors: AppColors) {
    // 동 이름 같은 글자는 색칠 위에 보이도록, 첫 글자 층 바로 아래에 얹는다
    val firstLabel = style.layers.firstOrNull { it is SymbolLayer }?.id
    fun add(layer: org.maplibre.android.style.layers.Layer) =
        if (firstLabel != null) style.addLayerBelow(layer, firstLabel) else style.addLayer(layer)

    style.addSource(GeoJsonSource(DONG_SOURCE, EMPTY))
    add(
        FillLayer("dong-fill", DONG_SOURCE).withProperties(
            PropertyFactory.fillColor(byKind(colors.mineFill.expr(), colors.friendFill.expr(), Color.Transparent.expr()))
        )
    )
    // 선: 내 땅·친구 땅은 실선, 주인 없음은 점선 (점선은 값마다 바꿀 수 없어 층을 나눈다)
    add(
        LineLayer("dong-line", DONG_SOURCE).withFilter(Expression.neq(Expression.get("kind"), "none")).withProperties(
            PropertyFactory.lineColor(byKind(colors.mineLine.expr(), colors.friendLine.expr(), colors.noneLine.expr())),
            PropertyFactory.lineWidth(3f),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        )
    )
    add(
        LineLayer("dong-line-none", DONG_SOURCE).withFilter(Expression.eq(Expression.get("kind"), "none")).withProperties(
            PropertyFactory.lineColor(colors.noneLine.expr()),
            PropertyFactory.lineWidth(2f),
            PropertyFactory.lineDasharray(arrayOf(5f, 4f)),
        )
    )
    // 기록 탭에서 고른 여행: 흰 바탕선 위에 파란 선, 정차 지점은 점
    style.addSource(GeoJsonSource(ROUTE_SOURCE, EMPTY))
    style.addSource(GeoJsonSource(STOPS_SOURCE, EMPTY))
    style.addLayer(
        LineLayer("route-casing", ROUTE_SOURCE).withProperties(
            PropertyFactory.lineColor(Color.White.expr()), PropertyFactory.lineWidth(10f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND), PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        )
    )
    style.addLayer(
        LineLayer("route-line", ROUTE_SOURCE).withProperties(
            PropertyFactory.lineColor(colors.primary.expr()), PropertyFactory.lineWidth(6f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND), PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        )
    )
    style.addLayer(
        CircleLayer("stop-dots", STOPS_SOURCE).withProperties(
            PropertyFactory.circleRadius(11f), PropertyFactory.circleColor(colors.primary.expr()),
            PropertyFactory.circleStrokeColor(Color.White.expr()), PropertyFactory.circleStrokeWidth(4f),
        )
    )
    // 차 위치: 파란 점 + 흰 테두리 + 퍼지는 연한 원
    style.addSource(GeoJsonSource(CAR_SOURCE, EMPTY))
    style.addLayer(
        CircleLayer("car-halo", CAR_SOURCE).withProperties(
            PropertyFactory.circleRadius(26f),
            PropertyFactory.circleColor(colors.mineLine.expr()),
            PropertyFactory.circleOpacity(0.22f),
        )
    )
    style.addLayer(
        CircleLayer("car-dot", CAR_SOURCE).withProperties(
            PropertyFactory.circleRadius(9f),
            PropertyFactory.circleColor(colors.primary.expr()),
            PropertyFactory.circleStrokeColor(Color.White.expr()),
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
                onClick = { OfflineMap.download(context, context.resources.displayMetrics.density, { status = it }) },
                modifier = Modifier.heightIn(min = 64.dp),
            ) { Text("강릉 지도 미리 받기") }
            if (status.isNotEmpty()) Text(status, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
