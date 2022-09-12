package com.taeyeon.tell

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.taeyeon.core.Core
import com.taeyeon.core.SharedPreferencesManager
import com.taeyeon.core.Utils
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

object TTSCore {

    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private const val TTS_KEY = "TTS"

    private const val COMPONENT_KEY = "COMPONENT"
    private const val SETTINGS_BACK_UP_KEY = "SETTINGS_BACK_UP"
    private const val FAVORITES_KEY = "FAVORITES"
    private const val HISTORIES_KEY = "HISTORIES"

    val INITIAL_TTS = TTS()
        get() {
            return field.clone()
        }
    val INITIAL_COMPONENT = Component(
        text = "",
        tts = INITIAL_TTS.clone()
    )
        get() {
            return field.clone()
        }
    val INITIAL_SETTINGS_BACK_UP = arrayListOf(TTSSettings(name = "Default Settings", TTS = INITIAL_TTS.clone())).clone() as ArrayList<TTSSettings>
        get() {
            return field.clone() as ArrayList<TTSSettings>
        }
    val INITIAL_FAVORITES = arrayListOf<TTSHistory>()
        get() {
            return field.clone() as ArrayList<TTSHistory>
        }
    val INITIAL_HISTORIES = arrayListOf<TTSHistory>()
        get() {
            return field.clone() as ArrayList<TTSHistory>
        }

    private lateinit var component: Component
    private lateinit var settingsBackUp: ArrayList<TTSSettings>
    private lateinit var favorites: ArrayList<TTSHistory>
    private lateinit var histories: ArrayList<TTSHistory>


    data class TTSSettings(val name: String, val TTS: TTS)

    data class TTSHistory(val component: Component, val time: LocalDateTime)


    class TTS(
        locale: Locale = Locale.KOREA,
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f
    ): Cloneable {

        companion object {

            val TTS_LOCALES = arrayOf<Locale>(Locale.KOREA, Locale.ENGLISH, Locale.CANADA, Locale.CANADA_FRENCH, Locale.CHINESE, Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN, Locale.JAPANESE, Locale.KOREA, Locale.CHINA)

            var nowTTS: TextToSpeech? = null

            fun stopNowTTS() {
                nowTTS?.stop()
                nowTTS?.shutdown()
                nowTTS = null
            }

            fun startInstallTTS() {
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Core.getContext().startActivity(installIntent)
            }

        }

        @delegate:Transient
        val tts: TextToSpeech by lazy {
            var tts_: TextToSpeech? = null
            tts_ = TextToSpeech(Core.getContext()) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts_?.setLanguage(locale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Utils.toast("오류 : 지원하지 않는 언어입니다.")
                        startInstallTTS()
                    }
                } else {
                    Utils.toast("오류 : TTS 실행중 오류 발생")
                }
            }
            tts_.setPitch(pitch)
            tts_.setSpeechRate(speechRate)

            tts_
        }
        var locale: Locale = if (TTS_LOCALES.indexOf(locale) != -1) locale else Locale.KOREAN
            set(value) {
                field = value
                save()
            }
        var pitch: Float = if (pitch > 0) pitch else 1.0f
            set(value) {
                field = value
                save()
            }
        var speechRate: Float = if (speechRate > 0) speechRate else 1.0f
            set(value) {
                field = value
                save()
            }

        init {
            tts.playSilentUtterance(0L, 0, null)
        }

        fun speak(
            text: String,
            locale: Locale = this.locale,
            pitch: Float = this.pitch,
            speechRate: Float = this.speechRate,
            onLanguageError: () -> Unit = {
                startInstallTTS()
            }
        ) {
            val locale_ = if (Companion.TTS_LOCALES.indexOf(locale) != -1) locale else Locale.KOREAN
            val pitch_ = if (pitch > 0) pitch else 1.0f
            val speechRate_ = if (speechRate > 0) speechRate else 1.0f

            val result = tts.setLanguage(locale_)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                onLanguageError()
            }

            tts.setPitch(pitch_)
            tts.setSpeechRate(speechRate_)

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        public override fun clone(): TTS {
            return super.clone() as TTS
        }

    }

    class Component(
        text: String = "",
        tts: TTS = INITIAL_TTS.clone()
    ): Cloneable {
        var text: String = ""
            set(value) {
                field = value
                TTSCore.save()
            }
        var tts: TTS = INITIAL_TTS.clone()
            set(value) {
                field = value
                TTSCore.save()
            }

        init {
            this.text = text
            this.tts = tts
        }

        public override fun clone(): Component {
            return super.clone() as Component
        }

    }


    fun setComponent(component: Component) {
        this.component = component
        TTSCore.save()
    }

    fun setSettingsBackUp(settingsBackUp: ArrayList<TTSSettings>) {
        this.settingsBackUp = settingsBackUp
        TTSCore.save()
    }
    fun setFavorites(favorites: ArrayList<TTSHistory>) {
        this.favorites = favorites
        TTSCore.save()
    }
    fun setHistories(histories: ArrayList<TTSHistory>) {
        this.histories = histories
        TTSCore.save()
    }

    fun getComponent(): Component = component
    fun getSettingsBackUp(): ArrayList<TTSSettings> = settingsBackUp
    fun getFavorites(): ArrayList<TTSHistory> = favorites
    fun getHistories(): ArrayList<TTSHistory> = histories


    fun load() {
        component = sharedPreferencesManager.getAny(
            COMPONENT_KEY,
            Component::class.java,
            INITIAL_COMPONENT
        )
        settingsBackUp = sharedPreferencesManager.getArrayList(
            SETTINGS_BACK_UP_KEY,
            TTSSettings::class.java,
            INITIAL_SETTINGS_BACK_UP.clone() as ArrayList<TTSSettings>
        ).clone() as ArrayList<TTSSettings>
        favorites = sharedPreferencesManager.getArrayList(
            FAVORITES_KEY,
            TTSHistory::class.java,
            INITIAL_FAVORITES.clone() as ArrayList<TTSHistory>
        ).clone() as ArrayList<TTSHistory>
        histories = sharedPreferencesManager.getArrayList(
            HISTORIES_KEY,
            TTSHistory::class.java,
            INITIAL_HISTORIES.clone() as ArrayList<TTSHistory>
        ).clone() as ArrayList<TTSHistory>
        save()
    }

    fun save() {
        if (Core.isSetUp()) {
            sharedPreferencesManager.putAny(COMPONENT_KEY, component)
            sharedPreferencesManager.putArrayList(SETTINGS_BACK_UP_KEY, settingsBackUp)
            sharedPreferencesManager.putArrayList(FAVORITES_KEY, favorites)
            sharedPreferencesManager.putArrayList(HISTORIES_KEY, histories)
        }
    }


    fun initialize() {
        if (!Core.isSetUp()) {
            sharedPreferencesManager = SharedPreferencesManager(TTS_KEY)
            load()
        }
    }

    fun activityDestroyed() {
        TTS.stopNowTTS()
    }
}