package com.example.interactive

import android.app.Application
import com.example.interactive.map.OfflineMap

/** 앱 프로세스가 사는 동안 기록 장치를 하나만 둔다. 처음 쓰는 순간 만들어진다 */
class InteractiveApp : Application() {
    val engine: RecordingEngine by lazy { RecordingEngine(this) }

    override fun onCreate() {
        super.onCreate()
        OfflineMap.prepare(this)
    }
}
