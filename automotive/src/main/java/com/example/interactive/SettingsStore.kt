package com.example.interactive

import android.content.Context
import com.example.interactive.shared.model.Settings

/** 설정을 기기 안에 저장한다 (docs/spec.md F6). 값이 없으면 기본값 */
class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun load(): Settings = Settings(
        carModel = prefs.getString(KEY_CAR, null),
        fuelEfficiencyKmPerLiter = if (prefs.contains(KEY_EFFICIENCY)) prefs.getFloat(KEY_EFFICIENCY, 0f).toDouble() else null,
        fuelPriceWonPerLiter = prefs.getInt(KEY_PRICE, Settings.DEFAULT.fuelPriceWonPerLiter),
        homeDongCode = prefs.getString(KEY_HOME, null),
    )

    fun save(settings: Settings) {
        prefs.edit().apply {
            putString(KEY_CAR, settings.carModel)
            settings.fuelEfficiencyKmPerLiter?.let { putFloat(KEY_EFFICIENCY, it.toFloat()) } ?: remove(KEY_EFFICIENCY)
            putInt(KEY_PRICE, settings.fuelPriceWonPerLiter)
            putString(KEY_HOME, settings.homeDongCode)
        }.apply()
    }

    private companion object {
        const val FILE = "settings"
        const val KEY_CAR = "car"
        const val KEY_EFFICIENCY = "efficiency"
        const val KEY_PRICE = "price"
        const val KEY_HOME = "home"
    }
}
