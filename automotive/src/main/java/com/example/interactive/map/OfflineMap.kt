package com.example.interactive.map

import android.app.Application
import android.content.Context
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File

/**
 * 지도 준비와 강릉 지도 미리 받기. 타일은 OpenFreeMap에서 받는다 (docs/data-sources.md).
 * OpenFreeMap 오프라인 조건: 한 번에 타일 3,000개·50MB 이하, 동시 요청 4개 이하, 고유한 앱 이름 보내기.
 */
object OfflineMap {

    const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

    /** 강릉시 경계 전체를 덮는 범위 (남서쪽 위도·경도, 북동쪽 위도·경도) */
    private val GANGNEUNG = LatLngBounds.from(37.918, 129.073, 37.503, 128.580)
    private const val MAX_ZOOM = 14.0 // OpenFreeMap 타일의 최대 확대 단계
    private const val TILE_LIMIT = 3_000L
    private const val SIZE_LIMIT_BYTES = 45L * 1024 * 1024 // 50MB 조건에 여유를 둔다
    private const val BUNDLED_DB = "mbgl-offline.db"

    /** 앱을 켤 때 한 번: 미리 받아 둔 지도 파일이 있으면 복사하고, 요청 규칙을 지키도록 설정한다 */
    fun prepare(app: Application) {
        copyBundledDb(app)
        MapLibre.getInstance(app)
        HttpRequestUtil.setOkHttpClient(
            OkHttpClient.Builder()
                .dispatcher(Dispatcher().apply { maxRequests = 4; maxRequestsPerHost = 4 })
                .addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder().header("User-Agent", "Interactive-contest-app/1.0").build())
                }
                .build()
        )
        OfflineManager.getInstance(app).setOfflineMapboxTileCountLimit(TILE_LIMIT)
    }

    // 미리 받은 지도(assets/mbgl-offline.db)를 MapLibre가 읽는 자리로 한 번만 복사한다
    private fun copyBundledDb(app: Application) {
        val target = File(app.filesDir, BUNDLED_DB)
        if (target.exists()) return
        runCatching {
            app.assets.open(BUNDLED_DB).use { input -> target.outputStream().use { input.copyTo(it) } }
        }
    }

    fun hasRegion(context: Context, onResult: (Boolean) -> Unit) {
        OfflineManager.getInstance(context).listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) = onResult(!offlineRegions.isNullOrEmpty())
            override fun onError(error: String) = onResult(false)
        })
    }

    /** 강릉 지도를 받는다. 진행 상황을 글자로 알려 주고, 크기 조건을 넘을 것 같으면 멈춘다 */
    fun download(context: Context, density: Float, onStatus: (String) -> Unit) {
        val definition = OfflineTilePyramidRegionDefinition(STYLE_URL, GANGNEUNG, 0.0, MAX_ZOOM, density)
        OfflineManager.getInstance(context).createOfflineRegion(
            definition, "gangneung".toByteArray(),
            object : OfflineManager.CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                        override fun onStatusChanged(status: OfflineRegionStatus) {
                            val mb = status.completedResourceSize / 1024.0 / 1024.0
                            onStatus("받는 중 ${status.completedResourceCount}/${status.requiredResourceCount}개 · %.1fMB".format(mb))
                            if (status.completedResourceSize > SIZE_LIMIT_BYTES && !status.isComplete) {
                                offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE)
                                onStatus("멈춤: 45MB를 넘어서 중단했어요 (OpenFreeMap 조건 50MB)")
                            } else if (status.isComplete) {
                                offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE)
                                onStatus("완료 · %.1fMB · 타일 ${status.completedTileCount}개".format(mb))
                            }
                        }

                        override fun onError(error: OfflineRegionError) = onStatus("오류 ${error.reason}: ${error.message}")
                        override fun mapboxTileCountLimitExceeded(limit: Long) = onStatus("타일 $limit 개 한도 초과")
                    })
                    offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                }

                override fun onError(error: String) = onStatus("만들기 실패: $error")
            }
        )
    }
}
