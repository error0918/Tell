package com.taeyeon.tell

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import kotlin.properties.Delegates

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


    object DialogDefaults {
        val Modifier: Modifier = androidx.compose.ui.Modifier
        val MinWidth = 280.dp
        val MaxWidth = 560.dp
        val MinHeight = 0.dp
        val MaxHeight = Dp.Infinity
        val ContainerPadding = PaddingValues(
            start = 24.dp,
            top = 24.dp,
            end = 24.dp,
            bottom = 18.dp
        )
        val Shape: Shape
            @Composable get() { return MaterialTheme.shapes.medium }
        val ContainerColor: Color
            @Composable get() { return MaterialTheme.colorScheme.surface }
        val TonalElevation = 2.dp
        val ListTextAlign = TextAlign.Start
        val IconContentColor: Color
            @Composable get() { return contentColorFor(backgroundColor = ContainerColor) }
        val TitleContentColor: Color
            @Composable get() { return contentColorFor(backgroundColor = ContainerColor) }
        val TextContentColor: Color
            @Composable get() { return contentColorFor(backgroundColor = ContainerColor) }
        val ContentColor: Color
            @Composable get() { return contentColorFor(backgroundColor = ContainerColor) }
        val ListContentColor: Color
            @Composable get() { return MaterialTheme.colorScheme.primary }
        val ButtonContentColor: Color
            @Composable get() { return MaterialTheme.colorScheme.primary }
        val TitleTextStyle: TextStyle
            @Composable get() { return MaterialTheme.typography.headlineSmall }
        val TextTextStyle: TextStyle
            @Composable get() { return MaterialTheme.typography.bodyMedium }
        val ContentTextStyle: TextStyle
            @Composable get() { return MaterialTheme.typography.bodyMedium }
        val ListTextStyle: TextStyle
            @Composable get() { return MaterialTheme.typography.labelLarge }
        val ButtonTextStyle: TextStyle
            @Composable get() { return MaterialTheme.typography.labelLarge }
        val Properties = DialogProperties()
    }

    @Composable
    fun DialogButtonRow(
        modifier: Modifier = Modifier,
        button: @Composable RowScope.() -> Unit
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = button
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun SurfaceDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        minWidth: Dp = DialogDefaults.MinWidth,
        maxWidth: Dp = DialogDefaults.MaxWidth,
        minHeight: Dp = DialogDefaults.MinHeight,
        maxHeight: Dp = DialogDefaults.MaxHeight,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        properties: DialogProperties = DialogDefaults.Properties,
        content: @Composable () -> Unit
    ) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = properties
        ) {
            Surface(
                modifier = modifier.then(
                    Modifier.sizeIn(
                        minWidth = minWidth,
                        maxWidth = maxWidth,
                        minHeight = minHeight,
                        maxHeight = maxHeight
                    )
                ),
                shape = shape,
                color = containerColor,
                tonalElevation = tonalElevation,
                content = content
            )
        }
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun BaseDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        minWidth: Dp = DialogDefaults.MinWidth,
        maxWidth: Dp = DialogDefaults.MaxWidth,
        minHeight: Dp = DialogDefaults.MinHeight,
        maxHeight: Dp = DialogDefaults.MaxHeight,
        containerPadding: PaddingValues = DialogDefaults.ContainerPadding,
        icon: (@Composable () -> Unit)? = null,
        title: (@Composable () -> Unit)? = null,
        text: (@Composable () -> Unit)? = null,
        content: (@Composable () -> Unit)? = null,
        button: (@Composable () -> Unit)? = null,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        contentColor: Color = DialogDefaults.ContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        contentTextStyle: TextStyle = DialogDefaults.ContentTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        SurfaceDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            minWidth = minWidth,
            maxWidth = maxWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            shape = shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            properties = properties
        ) {
            Column(
                modifier = Modifier.padding(containerPadding),
                horizontalAlignment = if (icon != null) Alignment.CenterHorizontally else Alignment.Start
            ) {

                icon?.let {
                    CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                        Box(
                            modifier = Modifier.padding(PaddingValues(bottom = 16.dp))
                        ) {
                            icon()
                        }
                    }
                }

                title?.let {
                    CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                        ProvideTextStyle(titleTextStyle) {
                            Box(
                                modifier = Modifier.padding(PaddingValues(bottom = 16.dp))
                            ) {
                                title()
                            }
                        }
                    }
                }

                text?.let {
                    CompositionLocalProvider(LocalContentColor provides textContentColor) {
                        ProvideTextStyle(textTextStyle) {
                            Box(
                                modifier = Modifier
                                    .weight(weight = 1f, fill = false)
                                    .padding(PaddingValues(bottom = 16.dp))
                                    .align(Alignment.Start)
                            ) {
                                text()
                            }
                        }
                    }
                }

                content?.let {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        ProvideTextStyle(contentTextStyle) {
                            Box(
                                modifier = Modifier
                                    .padding(PaddingValues(bottom = 16.dp))
                                    .align(Alignment.Start)
                            ) {
                                content()
                            }
                        }
                    }
                }

                button?.let {
                    CompositionLocalProvider(LocalContentColor provides buttonContentColor) {
                        ProvideTextStyle(buttonTextStyle) {
                            Box(
                                modifier = Modifier
                                    .padding(PaddingValues(top = 2.dp))
                                    .align(Alignment.End),
                            ) {
                                button()
                            }
                        }
                    }
                }

            }
        }

    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun MessageDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        icon: (@Composable () -> Unit)? = null,
        title: (@Composable () -> Unit)? = null,
        text: (@Composable () -> Unit)? = null,
        button: (@Composable () -> Unit)? = null,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        BaseDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            icon = icon,
            title = title,
            text = text,
            button = button,
            shape= shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun MessageDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        icon: (@Composable () -> Unit)? = null,
        title: (@Composable () -> Unit)? = null,
        text: (@Composable () -> Unit)? = null,
        dismissButton: (@Composable () -> Unit)? = null,
        confirmButton: (@Composable () -> Unit)? = null,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        MessageDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            icon = icon,
            title = title,
            text = text,
            button = {
                DialogButtonRow {
                    if (dismissButton != null) dismissButton()
                    if (confirmButton != null) confirmButton()
                }
            },
            shape= shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun MessageDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        icon: ImageVector? = null,
        title: String? = null,
        text: String? = null,
        dismissButtonText: String? = null,
        confirmButtonText: String? = null,
        onDismissButtonClick: (() -> Unit)? = onDismissRequest,
        onConfirmButtonClick: (() -> Unit)? = null,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        MessageDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            icon = if (icon != null) { -> Icon(imageVector = icon, contentDescription = null) } else null,
            title = if (title != null) { -> Text(text = title) } else null,
            text = if (text != null) { -> Text(text = text) } else null,
            dismissButton = if (dismissButtonText != null && onDismissButtonClick != null)
                { ->
                    TextButton(onClick = onDismissButtonClick) {
                        Text(text = dismissButtonText)
                    }
                } else null,
            confirmButton = if (confirmButtonText != null && onConfirmButtonClick != null)
                { ->
                    TextButton(onClick = onConfirmButtonClick) {
                        Text(text = confirmButtonText)
                    }
                } else null,
            shape= shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun ListDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        minWidth: Dp = DialogDefaults.MinWidth,
        maxWidth: Dp = DialogDefaults.MaxWidth,
        minHeight: Dp = DialogDefaults.MinHeight,
        maxHeight: Dp = DialogDefaults.MaxHeight,
        containerPadding: PaddingValues = DialogDefaults.ContainerPadding,
        icon: (@Composable () -> Unit)? = null,
        title: (@Composable () -> Unit)? = null,
        text: (@Composable () -> Unit)? = null,
        itemCount: Int,
        itemContent: @Composable LazyItemScope.(index: Int) -> Unit,
        button: (@Composable () -> Unit)? = null,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        listContentColor: Color = DialogDefaults.ListContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        listTextStyle: TextStyle = DialogDefaults.ListTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        BaseDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            minWidth = minWidth,
            maxWidth = maxWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            containerPadding = containerPadding,
            icon = icon,
            title = title,
            text = text,
            content = {
                CompositionLocalProvider(LocalContentColor provides listContentColor) {
                    ProvideTextStyle(listTextStyle) {
                        LazyColumn {
                            items(itemCount) { index ->
                                itemContent(index)
                            }
                        }
                    }
                }
            },
            button = button,
            shape = shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun <T> ListDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        minWidth: Dp = DialogDefaults.MinWidth,
        maxWidth: Dp = DialogDefaults.MaxWidth,
        minHeight: Dp = DialogDefaults.MinHeight,
        maxHeight: Dp = DialogDefaults.MaxHeight,
        containerPadding: PaddingValues = DialogDefaults.ContainerPadding,
        icon: (@Composable () -> Unit)? = null,
        title: (@Composable () -> Unit)? = null,
        text: (@Composable () -> Unit)? = null,
        items: List<T>,
        itemContent: @Composable LazyItemScope.(item: T) -> Unit,
        button: (@Composable () -> Unit)? = null,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        listContentColor: Color = DialogDefaults.ListContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        listTextStyle: TextStyle = DialogDefaults.ListTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        BaseDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            minWidth = minWidth,
            maxWidth = maxWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            containerPadding = containerPadding,
            icon = icon,
            title = title,
            text = text,
            content = {
                CompositionLocalProvider(LocalContentColor provides listContentColor) {
                    ProvideTextStyle(listTextStyle) {
                        LazyColumn {
                            items(items) { item ->
                                itemContent(item)
                            }
                        }
                    }
                }
            },
            button = button,
            shape = shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun <T> ListDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        minWidth: Dp = DialogDefaults.MinWidth,
        maxWidth: Dp = DialogDefaults.MaxWidth,
        minHeight: Dp = DialogDefaults.MinHeight,
        maxHeight: Dp = DialogDefaults.MaxHeight,
        containerPadding: PaddingValues = DialogDefaults.ContainerPadding,
        icon: (@Composable () -> Unit)? = null,
        title: (@Composable () -> Unit)? = null,
        text: (@Composable () -> Unit)? = null,
        items: Array<T>,
        itemContent: @Composable LazyItemScope.(item: T) -> Unit,
        button: (@Composable () -> Unit)? = null,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        listContentColor: Color = DialogDefaults.ListContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        listTextStyle: TextStyle = DialogDefaults.ListTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        ListDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            minWidth = minWidth,
            maxWidth = maxWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            containerPadding = containerPadding,
            icon = icon,
            title = title,
            text = text,
            items = items.toList(),
            itemContent = itemContent,
            button = button,
            shape = shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            listContentColor = listContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            listTextStyle = listTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun <T> ListDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        minWidth: Dp = DialogDefaults.MinWidth,
        maxWidth: Dp = DialogDefaults.MaxWidth,
        minHeight: Dp = DialogDefaults.MinHeight,
        maxHeight: Dp = DialogDefaults.MaxHeight,
        containerPadding: PaddingValues = DialogDefaults.ContainerPadding,
        icon: (@Composable () -> Unit)? = null,
        title: (@Composable () -> Unit)? = null,
        text: (@Composable () -> Unit)? = null,
        items: List<T>,
        itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit,
        button: (@Composable () -> Unit)? = null,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        listContentColor: Color = DialogDefaults.ListContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        listTextStyle: TextStyle = DialogDefaults.ListTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        BaseDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            minWidth = minWidth,
            maxWidth = maxWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            containerPadding = containerPadding,
            icon = icon,
            title = title,
            text = text,
            content = {
                CompositionLocalProvider(LocalContentColor provides listContentColor) {
                    ProvideTextStyle(listTextStyle) {
                        LazyColumn {
                            itemsIndexed(items) { index, item ->
                                itemContent(index, item)
                            }
                        }
                    }
                }
            },
            button = button,
            shape = shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun <T> ListDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        minWidth: Dp = DialogDefaults.MinWidth,
        maxWidth: Dp = DialogDefaults.MaxWidth,
        minHeight: Dp = DialogDefaults.MinHeight,
        maxHeight: Dp = DialogDefaults.MaxHeight,
        containerPadding: PaddingValues = DialogDefaults.ContainerPadding,
        icon: (@Composable () -> Unit)? = null,
        title: (@Composable () -> Unit)? = null,
        text: (@Composable () -> Unit)? = null,
        items: Array<T>,
        itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit,
        button: (@Composable () -> Unit)? = null,
        shape: Shape = DialogDefaults.Shape,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        listContentColor: Color = DialogDefaults.ListContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        listTextStyle: TextStyle = DialogDefaults.ListTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        ListDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            minWidth = minWidth,
            maxWidth = maxWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            containerPadding = containerPadding,
            icon = icon,
            title = title,
            text = text,
            items = items.toList(),
            itemContent = itemContent,
            button = button,
            shape = shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            listContentColor = listContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            listTextStyle = listTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun ListDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        icon: ImageVector? = null,
        title: String? = null,
        text: String? = null,
        items: List<String>,
        listTextAlign: TextAlign = DialogDefaults.ListTextAlign,
        onItemClick: (index: Int, item: String) -> Unit,
        dismissButtonText: String? = null,
        confirmButtonText: String? = null,
        onDismissButtonClick: (() -> Unit)? = onDismissRequest,
        onConfirmButtonClick: (() -> Unit)? = null,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        listContentColor: Color = DialogDefaults.ListContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        listTextStyle: TextStyle = DialogDefaults.ListTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        ListDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            icon = if (icon != null) { -> Icon(imageVector = icon, contentDescription = null) } else null,
            title = if (title != null) { -> Text(text = title) } else null,
            text = if (text != null) { -> Text(text = text) } else null,
            items = items,
            itemContent = { index, item ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { onItemClick(index, item) },
                        shape = RectangleShape
                    ) {
                        Text(
                            text = item,
                            textAlign = listTextAlign,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Divider(modifier = Modifier.fillMaxWidth())
                }
            },
            button = {
                DialogButtonRow {
                    if (dismissButtonText != null && onDismissButtonClick != null) {
                        TextButton(onClick = onDismissButtonClick) {
                            Text(text = dismissButtonText)
                        }
                    }
                    if (confirmButtonText != null && onConfirmButtonClick != null) {
                        TextButton(onClick = onConfirmButtonClick) {
                            Text(text = confirmButtonText)
                        }
                    }
                }
            },
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            listContentColor = listContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            listTextStyle = listTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

    @SuppressLint("ModifierParameter")
    @Composable
    fun ListDialog(
        onDismissRequest: () -> Unit,
        modifier: Modifier = DialogDefaults.Modifier,
        icon: ImageVector? = null,
        title: String? = null,
        text: String? = null,
        items: Array<String>,
        listTextAlign: TextAlign = DialogDefaults.ListTextAlign,
        onItemClick: (index: Int, item: String) -> Unit,
        dismissButtonText: String? = null,
        confirmButtonText: String? = null,
        onDismissButtonClick: (() -> Unit)? = onDismissRequest,
        onConfirmButtonClick: (() -> Unit)? = null,
        containerColor: Color = DialogDefaults.ContainerColor,
        tonalElevation: Dp = DialogDefaults.TonalElevation,
        iconContentColor: Color = DialogDefaults.IconContentColor,
        titleContentColor: Color = DialogDefaults.TitleContentColor,
        textContentColor: Color = DialogDefaults.TextContentColor,
        listContentColor: Color = DialogDefaults.ListContentColor,
        buttonContentColor: Color = DialogDefaults.ButtonContentColor,
        titleTextStyle: TextStyle = DialogDefaults.TitleTextStyle,
        textTextStyle: TextStyle = DialogDefaults.TextTextStyle,
        listTextStyle: TextStyle = DialogDefaults.ListTextStyle,
        buttonTextStyle: TextStyle = DialogDefaults.ButtonTextStyle,
        properties: DialogProperties = DialogDefaults.Properties
    ) {
        ListDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            icon = icon,
            title = title,
            text = text,
            items = items.toList(),
            listTextAlign = listTextAlign,
            onItemClick = onItemClick,
            dismissButtonText = dismissButtonText,
            confirmButtonText = confirmButtonText,
            onDismissButtonClick = onDismissButtonClick,
            onConfirmButtonClick = onConfirmButtonClick,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            listContentColor = listContentColor,
            buttonContentColor = buttonContentColor,
            titleTextStyle = titleTextStyle,
            textTextStyle = textTextStyle,
            listTextStyle = listTextStyle,
            buttonTextStyle = buttonTextStyle,
            properties = properties
        )
    }

}