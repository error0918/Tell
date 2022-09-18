package com.taeyeon.tell

import androidx.compose.runtime.*
import com.taeyeon.core.Settings
import java.util.*


var fullScreenMode by mutableStateOf(Settings.INITIAL_SETTINGS_DATA.FullScreenMode)
var screenAlwaysOn by mutableStateOf(Settings.INITIAL_SETTINGS_DATA.ScreenAlwaysOn)
var darkMode by mutableStateOf(Settings.INITIAL_SETTINGS_DATA.DarkMode)
var dynamicColor by mutableStateOf(Settings.INITIAL_SETTINGS_DATA.DynamicColor)
var autoSaveHistory by mutableStateOf(Settings.INITIAL_SETTINGS_DATA.AutoSaveHistory)
var maxPitch by mutableStateOf(Settings.INITIAL_SETTINGS_DATA.MaxPitch)
var maxSpeechRate by mutableStateOf(Settings.INITIAL_SETTINGS_DATA.MaxSpeechRate)

var text by mutableStateOf(TTSCore.INITIAL_COMPONENT.text)
var locale by mutableStateOf(TTSCore.INITIAL_COMPONENT.tts.locale)
var pitch by mutableStateOf(TTSCore.INITIAL_COMPONENT.tts.pitch)
var speechRate by mutableStateOf(TTSCore.INITIAL_COMPONENT.tts.speechRate)

var settingsBackUp = TTSCore.INITIAL_SETTINGS_BACK_UP.toMutableStateList()
var favorites = TTSCore.INITIAL_FAVORITES.toMutableStateList()
var histories = TTSCore.INITIAL_HISTORIES.toMutableStateList()


fun load() {
    Settings.loadSettings()
    TTSCore.load()

    fullScreenMode = Settings.settingsData.FullScreenMode
    screenAlwaysOn = Settings.settingsData.ScreenAlwaysOn
    darkMode = Settings.settingsData.DarkMode
    dynamicColor = Settings.settingsData.DynamicColor
    autoSaveHistory = Settings.settingsData.AutoSaveHistory
    maxPitch = Settings.settingsData.MaxPitch
    maxSpeechRate = Settings.settingsData.MaxSpeechRate

    text = TTSCore.getComponent().text
    locale = TTSCore.getComponent().tts.locale
    pitch = TTSCore.getComponent().tts.pitch
    speechRate = TTSCore.getComponent().tts.speechRate

    settingsBackUp.clear()
    settingsBackUp.addAll(TTSCore.getSettingsBackUp())
    favorites.clear()
    favorites.addAll(TTSCore.getFavorites())
    histories.clear()
    histories.addAll(TTSCore.getHistories())
}

fun save() {
    Settings.settingsData.FullScreenMode = fullScreenMode
    Settings.settingsData.ScreenAlwaysOn = screenAlwaysOn
    Settings.settingsData.DarkMode = darkMode
    Settings.settingsData.DynamicColor = dynamicColor
    Settings.settingsData.AutoSaveHistory = autoSaveHistory
    Settings.settingsData.MaxPitch = maxPitch
    Settings.settingsData.MaxSpeechRate = maxSpeechRate

    TTSCore.getComponent().text = text
    TTSCore.getComponent().tts.locale = locale
    TTSCore.getComponent().tts.pitch = pitch
    TTSCore.getComponent().tts.speechRate = speechRate

    TTSCore.setSettingsBackUp(settingsBackUp.toCollection(ArrayList()))
    TTSCore.setFavorites(favorites.toCollection(ArrayList()))
    TTSCore.setHistories(histories.toCollection(ArrayList()))

    Settings.saveSettings()
    TTSCore.save()
}