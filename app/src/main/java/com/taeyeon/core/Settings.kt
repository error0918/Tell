package com.taeyeon.core

import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager

/*
 * NOTICE
 *
 * Add "implementation "com.google.code.gson:gson:2.9.1"" at build.gradle (app)
 * Require "com.taeyeon.core.SharedPreferencesManager"
 *
 */

object Settings {

    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private const val SETTINGS_KEY = "SETTINGS"

    const val SYSTEM_MODE = 0
    const val LIGHT_MODE = 1
    const val DARK_MODE = 2

    val INITIAL_SETTINGS_DATA =
        SettingsData(
            FullScreenMode = false,
            ScreenAlwaysOn = false,
            DarkMode = SYSTEM_MODE,
            DynamicColor = false,
            AutoSaveHistory = true,
            MaxPitch =  10,
            MaxSpeechRate = 10
        )
        get() {
            return field.clone()
        }

    lateinit var settingsData: SettingsData

    class SettingsData(
        FullScreenMode: Boolean = INITIAL_SETTINGS_DATA.FullScreenMode,
        ScreenAlwaysOn: Boolean = INITIAL_SETTINGS_DATA.ScreenAlwaysOn,
        DarkMode: Int = INITIAL_SETTINGS_DATA.DarkMode,
        DynamicColor: Boolean = INITIAL_SETTINGS_DATA.DynamicColor,
        AutoSaveHistory: Boolean = INITIAL_SETTINGS_DATA.AutoSaveHistory,
        MaxPitch: Int = INITIAL_SETTINGS_DATA.MaxPitch,
        MaxSpeechRate: Int = INITIAL_SETTINGS_DATA.MaxSpeechRate
    ) : Cloneable {
        var FullScreenMode: Boolean = false
            set(value) {
                field = value
                saveSettings()
            }
        var ScreenAlwaysOn: Boolean = false
            set(value) {
                field = value
                saveSettings()
            }
        var DarkMode: Int = SYSTEM_MODE
            set(value) {
                field = value
                saveSettings()
            }
        var DynamicColor: Boolean = true
            set(value) {
                field = value
                saveSettings()
            }
        var AutoSaveHistory: Boolean = true
            set(value) {
                field = value
                saveSettings()
            }
        var MaxPitch: Int = 10
            set(value) {
                field = value
                saveSettings()
            }
        var MaxSpeechRate: Int = 10
            set(value) {
                field = value
                saveSettings()
            }

        init {
            this.FullScreenMode = FullScreenMode
            this.ScreenAlwaysOn = ScreenAlwaysOn
            this.DarkMode =
                if (DarkMode == SYSTEM_MODE || DarkMode == LIGHT_MODE || DarkMode == DARK_MODE) DarkMode
                else if (INITIAL_SETTINGS_DATA.DarkMode == SYSTEM_MODE || INITIAL_SETTINGS_DATA.DarkMode == LIGHT_MODE || INITIAL_SETTINGS_DATA.DarkMode == DARK_MODE) INITIAL_SETTINGS_DATA.DarkMode else SYSTEM_MODE
            this.DynamicColor = DynamicColor
            this.AutoSaveHistory = AutoSaveHistory
            this.MaxPitch = MaxPitch
            this.MaxSpeechRate = MaxSpeechRate
        }

        public override fun clone(): SettingsData {
            return super.clone() as SettingsData
        }

    }

    fun loadSettings() {
        settingsData = sharedPreferencesManager.getAny(SETTINGS_KEY, SettingsData::class.java, INITIAL_SETTINGS_DATA.clone()).clone()
    }

    fun saveSettings() {
        if (Core.isSetUp()) sharedPreferencesManager.putAny(SETTINGS_KEY, settingsData)
    }

    fun applyFullScreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val window = Core.getActivity().window
            if (settingsData.FullScreenMode) {
                window.setDecorFitsSystemWindows(false)

                val controller = window.insetsController
                controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                window.setDecorFitsSystemWindows(true)
            }
        } else {
            if (settingsData.FullScreenMode) {
                val flag: Int = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                Core.getActivity().window.decorView.systemUiVisibility = flag
            } else {
                val flag: Int = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                Core.getActivity().window.decorView.systemUiVisibility = flag
            }
            Core.getActivity().window.decorView.requestLayout()
        }
    }

    fun applyScreenAlwaysOn() {
        if (settingsData.ScreenAlwaysOn) Core.getActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else Core.getActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun initializeSettings() {
        settingsData = INITIAL_SETTINGS_DATA.clone()
        saveSettings()
    }

    fun initialize() {
        sharedPreferencesManager = SharedPreferencesManager(SETTINGS_KEY)
        loadSettings()
    }

}