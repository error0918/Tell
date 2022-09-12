package com.taeyeon.tell

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.taeyeon.core.Settings
import java.util.*
import kotlin.properties.Delegates


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

object MyView {

    @Composable
    fun FullBackgroundSlider(
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        valueRange: ClosedFloatingPointRange<Float> = 0.01f .. 10f,
        steps: Int = 0,
        onValueChangeFinished: (() -> Unit)? = {},
        interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
        colors: androidx.compose.material3.SliderColors =
            SliderDefaults.colors(
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),

        isShowingPopup: Boolean = true,
        roundingDigits: Int = 2
    ) {
        var trackWidth by remember { mutableStateOf(0.dp) }
        var valueRatio by remember { mutableStateOf(0f) }
        var sliderWidth = 0f

        val sliderPosition by Delegates.observable(if (value in valueRange) value else valueRange.start) { _, _, _ ->
            trackWidth = (sliderWidth - 32f - 20f).dp * valueRatio + 10.dp
        }

        val valueSize = (valueRange.endInclusive - valueRange.start)
        valueRatio = (sliderPosition - valueRange.start) / valueSize

        val sliderOffset = remember { mutableStateOf(Offset.Zero) }
        val sliderSize = remember { mutableStateOf(IntSize.Zero) }
        sliderWidth = with (LocalDensity.current) { sliderSize.value.width.toDp().value }
        val isSliding = remember { mutableStateOf(false) }

        trackWidth = (sliderWidth - 32f - 20f).dp * valueRatio + 10.dp

        val popupSize = remember { mutableStateOf(IntSize.Zero) }
        val popupOffset =
            with (LocalDensity.current) {
                IntOffset(
                    (sliderOffset.value.x + (16.dp + 10.dp).toPx()
                            + (sliderSize.value.width - (32.dp + 20.dp).toPx()) * valueRatio
                            - popupSize.value.width / 2).toInt(),
                    (sliderOffset.value.y - popupSize.value.height).toInt()
                )
            }

        if (isSliding.value && isShowingPopup) {
            Popup(
                properties = PopupProperties(dismissOnBackPress = false),
                popupPositionProvider = object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        return popupOffset
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .onSizeChanged { size ->
                            popupSize.value = size
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${String.format("%.${roundingDigits}f", sliderPosition)} / ${valueRange.endInclusive}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        Box(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(percent = 100)
                )
                .onSizeChanged {
                    sliderSize.value = it
                }
                .onPlaced { onPlaced ->
                    sliderOffset.value =
                        Offset(onPlaced.positionInWindow().x, onPlaced.positionInWindow().y)
                }
        ) {
            Spacer(
                modifier = Modifier
                    .width(if (trackWidth > 20.dp) trackWidth + 16.dp else 20.dp + 16.dp)
                    .height(20.dp)
                    .padding(start = 16.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = if (trackWidth > 15.dp) RoundedCornerShape(
                            topStartPercent = 100,
                            topEndPercent = 0,
                            bottomStartPercent = 100,
                            bottomEndPercent = 0
                        ) else CircleShape
                    )
            )


            Slider(
                value = sliderPosition,
                onValueChange = { value ->
                    onValueChange(value)
                    isSliding.value = true
                    valueRatio = (value - valueRange.start) / valueSize
                },
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                onValueChangeFinished = {
                    isSliding.value = false
                    if (onValueChangeFinished != null) onValueChangeFinished()
                },
                interactionSource = interactionSource,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }

}