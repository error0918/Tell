@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class
)
@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package com.taeyeon.tell

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.inputmethod.InputMethodManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.DismissValue
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.core.animation.doOnEnd
import androidx.core.content.getSystemService
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.taeyeon.core.Core
import com.taeyeon.core.Error
import com.taeyeon.core.Settings
import com.taeyeon.core.Utils
import com.taeyeon.tell.Main.bottomSheetScaffoldState
import com.taeyeon.tell.Main.isLanguageSelectDropDownMenuExpanded
import com.taeyeon.tell.Main.isTextFieldFocused
import com.taeyeon.tell.Main.scope
import com.taeyeon.tell.MyView.FullBackgroundSlider
import com.taeyeon.tell.ui.theme.Theme
import com.taeyeon.tell.ui.theme.surfaceColorAtElevation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : ComponentActivity() {
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                ObjectAnimator.ofPropertyValuesHolder(
                    splashScreenView.iconView,
                    PropertyValuesHolder.ofFloat(View.ALPHA, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 2f, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 2f, 1f)
                ).run {
                    interpolator = AnticipateInterpolator()
                    duration = 100L
                    doOnEnd { splashScreenView.remove() }
                    start()
                }
            }
        }
        installSplashScreen()

        Core.initialize(applicationContext)

        super.onCreate(savedInstanceState)


        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en-US")
// Call this on the main thread as it may require Activity.restart()
        AppCompatDelegate.setApplicationLocales(appLocale)

        Core.activityCreated(this)

        val backPressed = {
            if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                scope.launch {
                    bottomSheetScaffoldState.bottomSheetState.collapse()
                }
            } else if (isLanguageSelectDropDownMenuExpanded) {
                isLanguageSelectDropDownMenuExpanded = false
            } else {
                scope.launch {
                    if (System.currentTimeMillis() >= backPressedTime + 2000L) {
                        val snackbarResult =
                            bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                message = Core.getContext().resources.getString(R.string.back_press_to_shut_down_message),
                                actionLabel = Core.getContext().resources.getString(R.string.quick_shut_down),
                                duration = SnackbarDuration.Short
                            )
                        if (snackbarResult == SnackbarResult.ActionPerformed) {
                            Utils.shutDownApp()
                        }
                    } else {
                        Utils.shutDownApp()
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                backPressed()
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object: OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        backPressed()
                    }
                }
            )
        }

        setContent {
            Theme {
                Main.Main()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        try {
            if (isTextFieldFocused) {
                val inputMethodManager = getSystemService<InputMethodManager>()
                if (inputMethodManager != null && currentFocus != null) {
                    inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                    currentFocus!!.clearFocus()
                }
            }
        } catch (exception: Exception) {
            Error.toast(exception)
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Settings.applyFullScreenMode()
        com.taeyeon.core.Settings.applyScreenAlwaysOn()

        if (hasFocus) {
            val clipboardManager = applicationContext.getSystemService<ClipboardManager>()
            val getClipboardText = {
                if (clipboardManager?.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                    val clip = clipboardManager.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        clip.getItemAt(0).coerceToText(applicationContext)?.toString()
                    } else null
                } else null
            }

            if (clipboardManager != null) Main.clipboardText = getClipboardText()

            clipboardManager?.addPrimaryClipChangedListener {
                Main.clipboardText = getClipboardText()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TTSCore.activityDestroyed()
    }

    override fun onRestart() {
        super.onRestart()
        Core.activityCreated(this)
    }
}

object Main {
    lateinit var bottomSheetScaffoldState: BottomSheetScaffoldState
    lateinit var scope: CoroutineScope

    var isLanguageSelectDropDownMenuExpanded by mutableStateOf(false)
    var isSpeaking by mutableStateOf(false)
    var clipboardText by mutableStateOf<String?>(null)

    var isTextFieldFocused = false

    @Composable
    fun Main() {
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
        scope = rememberCoroutineScope()

        loadWithMessage()

        BottomSheetScaffold(
            topBar = { Toolbar() },
            floatingActionButton = { Fab() },
            sheetContent = { BottomSheetContent() },
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            scaffoldState = bottomSheetScaffoldState
        ) {
            MainContent()
        }

    }

    @Composable
    fun Toolbar() {
        MediumTopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = stringResource(id = R.string.popup),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = {
                    loadWithMessage()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(id = R.string.refresh),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(
                    onClick = {
                        val intent = Intent(Core.getContext(), SettingsActivity::class.java)
                        Core.getActivity().startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.settings),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .zIndex(1f)
                .shadow(10.dp)
        )
    }

    @Composable
    fun Fab() {
        var fabDragAmount by remember { mutableStateOf(0f) }
        var popupText by remember { mutableStateOf<String?>(null) }

        popupText = if (bottomSheetScaffoldState.bottomSheetState.isCollapsed && fabDragAmount < -0.2) {
            stringResource(id = R.string.open)
        } else if (bottomSheetScaffoldState.bottomSheetState.isExpanded && fabDragAmount > 0.2) {
            stringResource(id = R.string.close)
        } else {
            null
        }

        if (popupText != null) {
            Popup(
                alignment = Alignment.Center,
                properties = PopupProperties(dismissOnBackPress = false)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = popupText ?: "",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        val text: String
        val onClick: () -> Unit
        val iconImage: ImageVector
        val iconImageDescription: String

        if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
            if (isSpeaking) {
                text = stringResource(id = R.string.stop)
                onClick = {
                    Utils.vibrate(25)
                    stop()
                }
                iconImage = Icons.Filled.Warning
                iconImageDescription = stringResource(id = R.string.stop)
            } else {
                text = stringResource(id = R.string.tell)
                onClick = {
                    Utils.vibrate(25)
                    tell()
                }
                iconImage = Icons.Filled.PlayArrow
                iconImageDescription = stringResource(id = R.string.tell)
            }
        } else {
            text = stringResource(id = R.string.collapse)
            onClick = {
                Utils.vibrate(25)
                scope.launch {
                    bottomSheetScaffoldState.bottomSheetState.collapse()
                }
            }
            iconImage = Icons.Filled.KeyboardArrowDown
            iconImageDescription = stringResource(id = R.string.collapse)
        }

        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            icon = {
                Icon(
                    imageVector = iconImage,
                    contentDescription = iconImageDescription,
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            },
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                Utils.vibrate(25)
                                if (bottomSheetScaffoldState.bottomSheetState.isCollapsed && fabDragAmount < -0.2) {
                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                } else if (bottomSheetScaffoldState.bottomSheetState.isExpanded && fabDragAmount > 0.2) {
                                    bottomSheetScaffoldState.bottomSheetState.collapse()
                                }
                                fabDragAmount = 0f
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            fabDragAmount = dragAmount
                        }
                    )
                }
        )
    }

    @Composable
    fun BottomSheetItem(
        isOpened: Boolean = false,
        hasActionButton: Boolean = false,
        editable: Boolean = true,
        deletable: Boolean = true,
        onClick: () -> Unit = {},
        actionIcon: ImageVector = Icons.Filled.PlayArrow,
        onActionButtonClicked: () -> Unit = {},
        onEdited: () -> Unit = { },
        onDeleted: () -> Unit = { },
        title: String = "Title",
        subTitle: String? = null,
        closedContent: @Composable () -> Unit = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                )
                if (subTitle != null && subTitle.isNotEmpty()) {
                    Text(
                        text = subTitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                    )
                }
            }
        },
        openedContent: @Composable () -> Unit = {}
    ) {
        val dismissState = rememberDismissState(
            confirmStateChange = { value ->
                when (value) {
                    DismissValue.DismissedToEnd -> {
                        onEdited()
                        false
                    }
                    DismissValue.DismissedToStart -> {
                        onDeleted()
                        false
                    }
                    else -> false
                }
            }
        )
        var opened by remember { mutableStateOf(isOpened) }

        SwipeToDismiss(
            state = dismissState,
            dismissThresholds = { FractionalThreshold(0.2f) },
            background = {
                val swipeToDismissBackgroundColor by animateColorAsState(
                    targetValue =  when (dismissState.targetValue) {
                        DismissValue.DismissedToEnd -> Color.Blue
                        DismissValue.DismissedToStart -> Color.Red
                        else -> MaterialTheme.colorScheme.onBackground
                    }.copy(0.4f)
                )
                val iconInfo = when (dismissState.targetValue) {
                    DismissValue.DismissedToEnd ->
                        Triple(Icons.Filled.Edit, stringResource(id = R.string.edit), Alignment.CenterStart)
                    DismissValue.DismissedToStart ->
                        Triple(Icons.Filled.Delete, stringResource(id = R.string.delete), Alignment.CenterEnd)
                    else -> null
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = swipeToDismissBackgroundColor,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 30.dp)
                ){
                    if (iconInfo != null) {
                        Icon(
                            imageVector = iconInfo.first,
                            contentDescription = iconInfo.second,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.align(iconInfo.third)
                        )
                    }
                }
            },
            directions = arrayListOf<DismissDirection>()
                .apply {
                    if (editable) add(DismissDirection.StartToEnd)
                    if (deletable) add(DismissDirection.EndToStart)
                }.toSet(),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onClick() },
                        onLongClick = { onEdited() }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {

                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(end = 48.dp)
                        ) {
                            closedContent()

                        }
                        IconButton(
                            onClick = { opened = !opened },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = if (!opened) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                                contentDescription = if (opened) stringResource(id = R.string.close) else stringResource(id = R.string.open),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (opened) {
                        Column(modifier = Modifier.fillMaxSize()) {

                            openedContent()

                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (hasActionButton) {
                                    IconButton(
                                        onClick = { onActionButtonClicked() },
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    ) {
                                        Icon(
                                            imageVector = actionIcon,
                                            contentDescription = stringResource(id = R.string.action),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (editable) {
                                    IconButton(
                                        onClick = { onEdited() },
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = stringResource(id = R.string.edit),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (deletable) {
                                    IconButton(
                                        onClick = { onDeleted() },
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = stringResource(id = R.string.delete),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    @Composable
    fun BottomSheetContent() {
        val scrollState = rememberScrollState()

        var settingsBackUpState by rememberSaveable { mutableStateOf(true) }
        var favoriteState by rememberSaveable { mutableStateOf(true) }
        var historyState by rememberSaveable { mutableStateOf(true) }

        val bottomSheetColor = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
        val bottomSheetContentColor = MaterialTheme.colorScheme.onBackground

        Surface(color = bottomSheetColor) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                Spacer(modifier = Modifier.height(23.dp))
                Spacer(
                    modifier = Modifier
                        .width(80.dp)
                        .height(10.dp)
                        .background(
                            color = bottomSheetContentColor.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(percent = 50)
                        )
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(23.dp)
                        .zIndex(1f)
                        .background(color = bottomSheetColor)
                )
                SmallTopAppBar(
                    title = { Text(text = stringResource(id = R.string.saved_history)) },
                    actions = {
                        IconButton(onClick = {
                            load()
                            scope.launch {
                                if (bottomSheetScaffoldState.snackbarHostState.currentSnackbarData == null) {
                                    bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                        message = Core.getContext().resources.getString(R.string.load_data_message),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(id = R.string.refresh),
                                tint = bottomSheetContentColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = bottomSheetColor,
                        navigationIconContentColor = bottomSheetContentColor,
                        titleContentColor = bottomSheetContentColor,
                        actionIconContentColor = bottomSheetContentColor
                    ),
                    modifier = Modifier.shadow(10.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(
                            start = 16.dp,
                            end = 16.dp
                        )
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))


                    TextButton(
                        onClick = {
                            settingsBackUpState = !settingsBackUpState
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.settings_back_up),
                                color = bottomSheetContentColor,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(0.dp)
                            )
                            Icon(
                                imageVector = if (settingsBackUpState) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (settingsBackUpState) stringResource(id = R.string.close) else stringResource(id = R.string.open),
                                tint = bottomSheetContentColor,
                                modifier = Modifier
                                    .size(
                                        MaterialTheme.typography.labelMedium.fontSize.value.dp
                                                + ButtonDefaults.TextButtonContentPadding.calculateTopPadding()
                                                + ButtonDefaults.TextButtonContentPadding.calculateBottomPadding()
                                    )
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    }

                    if (settingsBackUpState) {
                        settingsBackUp.forEachIndexed { index, item ->
                            BottomSheetItem(
                                title = item.name,
                                onClick = {
                                    Utils.vibrate(25)
                                    //todo
                                },
                                onEdited = {
                                    Utils.vibrate(25)
                                    //todo
                                },
                                onDeleted = {
                                    Utils.vibrate(25)
                                    settingsBackUp.removeAt(index)
                                    save()
                                }
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.settings_info,
                                        locale.displayLanguage, pitch.toString(), speechRate.toString()
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        //todo
                    }

                    Divider(modifier = Modifier.fillMaxWidth())


                    TextButton(
                        onClick = {
                            favoriteState = !favoriteState
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.favorite),
                                color = bottomSheetContentColor,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(0.dp)
                            )
                            Icon(
                                imageVector = if (favoriteState) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (favoriteState) stringResource(id = R.string.close) else stringResource(id = R.string.open),
                                tint = bottomSheetContentColor,
                                modifier = Modifier
                                    .size(
                                        MaterialTheme.typography.labelMedium.fontSize.value.dp
                                                + ButtonDefaults.TextButtonContentPadding.calculateTopPadding()
                                                + ButtonDefaults.TextButtonContentPadding.calculateBottomPadding()
                                    )
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    }

                    if (favoriteState) {
                        for (i in 1 .. 20) BottomSheetItem(subTitle = "Sub Title")
                    }

                    Divider(modifier = Modifier.fillMaxWidth())


                    TextButton(
                        onClick = {
                            historyState = !historyState
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.history),
                                color = bottomSheetContentColor,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(0.dp)
                            )
                            Icon(
                                imageVector = if (historyState) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (historyState) stringResource(id = R.string.close) else stringResource(id = R.string.open),
                                tint = bottomSheetContentColor,
                                modifier = Modifier
                                    .size(
                                        MaterialTheme.typography.labelMedium.fontSize.value.dp
                                                + ButtonDefaults.TextButtonContentPadding.calculateTopPadding()
                                                + ButtonDefaults.TextButtonContentPadding.calculateBottomPadding()
                                    )
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    }

                    if (historyState) {
                        for (i in 1 .. 20) BottomSheetItem(editable = false)
                    }

                    Divider(modifier = Modifier.fillMaxWidth())


                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    @Composable
    fun ManageComponent(
        title: String,
        modifier: Modifier = Modifier,
        isUpperLimitedValueOnTextField: Boolean,
        defaultValue: Float,

        value: Float,
        onValueChange: (Float) -> Unit,
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
        var openEditValueDialog by rememberSaveable { mutableStateOf(false) }
        var openErrorValueDialog by rememberSaveable { mutableStateOf(false) }
        var openInitializeDialog by rememberSaveable { mutableStateOf(false) }

        var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

        if (openEditValueDialog) {
            var editingValue by remember { mutableStateOf(value) }
            var editingValueString by remember { mutableStateOf(value.toString()) }

            errorMessage = null

            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.edit),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                title = {
                    Text(text = stringResource(id = R.string.edit))
                },
                text = {
                    Column(modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .height(IntrinsicSize.Min)) {

                        Text(
                            text = Core.getContext().resources.getString(
                                R.string.edit_rules,
                                "${valueRange.start} ~ ${valueRange.endInclusive}", "(${if (isUpperLimitedValueOnTextField) stringResource(id = R.string.number_can_go_out_of_bounds_message) else ""})", defaultValue.toString()
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (errorMessage != null) {
                            Text(
                                text = Core.getContext().resources.getString(R.string.error_message, errorMessage ?: ""),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        OutlinedTextField(
                            value = editingValueString,
                            onValueChange = { value ->
                                editingValueString = value
                                editingValue =
                                    try {
                                        val valueFloat = value.toFloat()

                                        errorMessage = if (valueFloat < valueRange.start) {
                                            Core.getContext().resources.getString(R.string.edit_too_small_error_message)
                                        } else if (valueFloat > valueRange.endInclusive && !isUpperLimitedValueOnTextField) {
                                            Core.getContext().resources.getString(R.string.edit_too_big_error_message)
                                        } else {
                                            null
                                        }

                                        valueFloat
                                    } catch (exception: NumberFormatException) {
                                        val error = Error(exception)

                                        errorMessage = if (error.message.indexOf("For input string") != -1) {
                                            Core.getContext().resources.getString(R.string.edit_for_input_string_error_message)
                                        } else if (error.message.indexOf("multiple points") != -1) {
                                            Core.getContext().resources.getString(R.string.edit_multiple_points_error_message)
                                        } else if (error.message.indexOf("empty String") != -1) {
                                            Core.getContext().resources.getString(R.string.edit_empty_string_error_message)
                                        } else {
                                            error.message
                                        }

                                        defaultValue
                                    }
                            },
                            textStyle = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Center),
                            shape = MaterialTheme.shapes.large,
                            label = { Text(text = stringResource(id = R.string.edit_message)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    isTextFieldFocused = it.isFocused
                                },

                            )

                    }
                },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                confirmButton = {
                    TextButton(
                        onClick = {
                            openEditValueDialog = false
                            if (errorMessage != null) {
                                openErrorValueDialog = true
                            } else {
                                onValueChange(editingValue)
                            }
                        },
                    ) {
                        Text(text = stringResource(id = R.string.edit))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openEditValueDialog = false
                        },
                    ) {
                        Text(text = stringResource(id = R.string.dismiss))
                    }
                },
                onDismissRequest = {
                    openEditValueDialog = false
                }
            )
        }

        if (openErrorValueDialog) {
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = stringResource(id = R.string.value_error),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                title = {
                    Text(text = stringResource(id = R.string.value_error))
                },
                text = {
                    Text(
                        text = Core.getContext().resources.getString(
                            R.string.value_error_message,
                            errorMessage, defaultValue.toString()
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                confirmButton = {
                    Row {

                        TextButton(
                            onClick = {
                                openErrorValueDialog = false
                            }
                        ) {
                            Text(text = stringResource(id = R.string.dismiss))
                        }

                        TextButton(
                            onClick = {
                                openErrorValueDialog = false
                                openEditValueDialog = true
                            }
                        ) {
                            Text(text = stringResource(id = R.string.value_reedit))
                        }

                        TextButton(
                            onClick = {
                                openErrorValueDialog = false
                                onValueChange(defaultValue)
                            }
                        ) {
                            Text(text = stringResource(id = R.string.initialize))
                        }

                    }
                },
                onDismissRequest = {
                    openErrorValueDialog = false
                }
            )
        }

        if (openInitializeDialog) {
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(id = R.string.initialize),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                title = {
                    Text(text = stringResource(id = R.string.initialize))
                },
                text = {
                    Text(text = Core.getContext().resources.getString(R.string.initialize_message, defaultValue.toString()))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openInitializeDialog = false
                            onValueChange(defaultValue)
                        },
                    ) {
                        Text(text = stringResource(id = R.string.initialize))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openInitializeDialog = false
                        },
                    ) {
                        Text(text = stringResource(id = R.string.dismiss))
                    }
                },
                onDismissRequest = {
                    openInitializeDialog = false
                }
            )
        }

        Card(
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(45.dp)
                            .align(Alignment.CenterEnd)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(percent = 100)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(percent = 100)
                            )
                    ) {
                        TextButton(
                            onClick = {
                                openEditValueDialog = true
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(end = 45.dp)
                        ) {
                            Text(
                                text = value.toString(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(
                            onClick = {
                                openInitializeDialog = true
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(id = R.string.initialize),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FullBackgroundSlider(
                    value = if (value in valueRange) value else valueRange.endInclusive,
                    onValueChange = { value ->
                        onValueChange(value)
                    },
                    valueRange = valueRange,
                    steps = steps,
                    onValueChangeFinished = onValueChangeFinished,
                    interactionSource = interactionSource,
                    colors = colors,

                    isShowingPopup = isShowingPopup,
                    roundingDigits = roundingDigits
                )
            }
        }
    }

    @SuppressLint("NewApi")
    @Composable
    fun MainContent() {
        val scrollState = rememberScrollState()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        top = 0.dp,
                        bottom = BottomSheetScaffoldDefaults.SheetPeekHeight,
                        start = 16.dp,
                        end = 16.dp
                    )
                    .verticalScroll(scrollState)
            ) {
                val interactionSource = remember { MutableInteractionSource() }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { value ->
                        text = value
                        save()
                    },
                    shape = MaterialTheme.shapes.large,
                    label = { Text(text = stringResource(id = R.string.input)) },
                    interactionSource = interactionSource,
                    trailingIcon = {
                        val tintColor = TextFieldDefaults.outlinedTextFieldColors().labelColor(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource
                        ).value

                        if (text.isNotEmpty()) {
                            Box(modifier = Modifier.fillMaxHeight()) {
                                IconButton(
                                    onClick = {
                                        text = ""
                                        save()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = stringResource(id = R.string.clear),
                                        tint = tintColor
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .onFocusChanged {
                            isTextFieldFocused = it.isFocused
                        },
                )

                Spacer(modifier = Modifier.height(10.dp))

                val chipsScrollState = rememberScrollState()
                val hasCopyChip = text.isNotEmpty() && text != clipboardText
                val hasPasteChip = clipboardText != null && text != clipboardText
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(chipsScrollState),
                    horizontalArrangement = Arrangement.End
                ) {
                    AssistChip(
                        onClick = { /*todo*/ },
                        label = { Text(text = stringResource(id = R.string.load_from_website)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.load_from_website),
                                tint = AssistChipDefaults.assistChipColors()
                                    .leadingIconContentColor(enabled = true).value,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    AssistChip(
                        onClick = { /*todo*/ },
                        label = { Text(text = stringResource(id = R.string.load_from_file)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.load_from_file),
                                tint = AssistChipDefaults.assistChipColors()
                                    .leadingIconContentColor(enabled = true).value,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    AssistChip(
                        onClick = { /*todo*/ },
                        label = { Text(text = stringResource(id = R.string.input_through_stt)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.input_through_stt),
                                tint = AssistChipDefaults.assistChipColors()
                                    .leadingIconContentColor(enabled = true).value,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )

                    if (hasCopyChip || hasPasteChip)
                        Spacer(modifier = Modifier.width(10.dp))

                    if (hasCopyChip) {
                        AssistChip(
                            onClick = { Utils.copy(text = text) },
                            label = { Text(text = stringResource(id = R.string.copy)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(id = R.string.copy),
                                    tint = AssistChipDefaults.assistChipColors()
                                        .leadingIconContentColor(enabled = true).value,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                    }

                    if (hasCopyChip && hasPasteChip)
                        Spacer(modifier = Modifier.width(10.dp))

                    if (hasPasteChip) {
                        AssistChip(
                            onClick = {
                                text = clipboardText!!
                                save()
                            },
                            label = { Text(text = stringResource(id = R.string.paste)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(id = R.string.paste),
                                    tint = AssistChipDefaults.assistChipColors()
                                        .leadingIconContentColor(enabled = true).value,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp + 20.dp)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.select_language),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(10.dp)
                        )

                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .align(Alignment.CenterEnd)
                        ) {

                            Button(
                                onClick = { isLanguageSelectDropDownMenuExpanded = true },
                                modifier = Modifier.width(150.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    var iconWidth by remember { mutableStateOf(0.dp) }
                                    val getIconWidth = with(LocalDensity.current) {
                                        { pixel: Int ->
                                            iconWidth = pixel.toDp()
                                        }
                                    }

                                    Text(
                                        text = locale.displayLanguage,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(end = iconWidth)

                                    )
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = stringResource(id = R.string.open),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .onSizeChanged { size ->
                                                getIconWidth(size.width)
                                            }
                                    )
                                }
                            }

                            MaterialTheme(
                                shapes = MaterialTheme.shapes.copy(
                                    extraSmall = RoundedCornerShape(16.dp)
                                )
                            ) {
                                DropdownMenu(
                                    expanded = isLanguageSelectDropDownMenuExpanded,
                                    onDismissRequest = {
                                        isLanguageSelectDropDownMenuExpanded = false
                                    },
                                    properties = PopupProperties(
                                        dismissOnBackPress = true,
                                        dismissOnClickOutside = true
                                    ),
                                    modifier = Modifier.width(150.dp)
                                ) {
                                    val ttsLocales = TTSCore.TTS.TTS_LOCALES.toCollection(ArrayList())

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        ttsLocales.add(
                                            0,
                                            LocalContext.current.resources.configuration.locales[0]
                                        )
                                    }

                                    ttsLocales.forEachIndexed { index, ttsLocale ->

                                        Column {
                                            DropdownMenuItem(
                                                onClick = {
                                                    locale = ttsLocale
                                                    save()
                                                    isLanguageSelectDropDownMenuExpanded = false
                                                },
                                                modifier = Modifier.background(
                                                    if (ttsLocale == locale) MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.2f
                                                    ) else Color.Transparent
                                                )
                                            ) {
                                                Text(text = ttsLocale.displayLanguage)
                                            }
                                            if (index == 0 || index == 2) {
                                                Divider(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .background(
                                                            MaterialTheme.colorScheme.onBackground.copy(
                                                                alpha = 0.4f
                                                            )
                                                        )
                                                )
                                            }
                                        }

                                    }


                                }

                            }
                        }

                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ManageComponent(
                    title = stringResource(id = R.string.pitch),
                    isUpperLimitedValueOnTextField = true,
                    defaultValue = 1f,
                    value = pitch,
                    onValueChange = { value ->
                        pitch = value
                        save()
                    },
                    valueRange = 0.01f..maxPitch.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                ManageComponent(
                    title = stringResource(id = R.string.speech_rate),
                    isUpperLimitedValueOnTextField = true,
                    defaultValue = 1f,
                    value = speechRate,
                    onValueChange = { value ->
                        speechRate = value
                        save()
                    },
                    valueRange = 0.01f..maxSpeechRate.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                var openSettingsBackUpDialog by remember { mutableStateOf(false) }
                var openErrorValueDialog by remember { mutableStateOf(false) }

                var settingsName by remember { mutableStateOf("") }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                if (openSettingsBackUpDialog) {
                    settingsName = ""
                    errorMessage = null

                    AlertDialog(
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(id = R.string.settings_back_up),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        title = {
                            Text(text = stringResource(id = R.string.settings_back_up))
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .width(IntrinsicSize.Min)
                                    .height(IntrinsicSize.Min)
                            ) {

                                if (errorMessage != null) {
                                    Text(
                                        text = Core.getContext().resources.getString(
                                            R.string.error_message,
                                            errorMessage ?: ""
                                        ),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                OutlinedTextField(
                                    value = settingsName,
                                    onValueChange = { value ->
                                        settingsName = value
                                        errorMessage = null
                                        if (value.trim().isEmpty()) {
                                            errorMessage =
                                                Core.getContext().resources.getString(R.string.name_empty_string_error_message)
                                        }
                                        settingsBackUp.forEach {
                                            if (value.trim() == it.name) errorMessage =
                                                Core.getContext().resources.getString(
                                                    R.string.name_duplicate_name_error_message
                                                )
                                        }
                                    },
                                    textStyle = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Center),
                                    shape = MaterialTheme.shapes.large,
                                    label = { Text(text = stringResource(id = R.string.input_name_message)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged {
                                            isTextFieldFocused = it.isFocused
                                        },

                                    )

                            }
                        },
                        properties = DialogProperties(usePlatformDefaultWidth = false),
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    openSettingsBackUpDialog = false
                                    if (errorMessage != null) {
                                        openErrorValueDialog = true
                                    } else {
                                        settingsBackUp.add(
                                            TTSCore.TTSSettings(
                                                settingsName,
                                                TTSCore.TTS(locale, pitch, speechRate)
                                            )
                                        )
                                        save()
                                    }
                                },
                            ) {
                                Text(text = stringResource(id = R.string.settings_back_up))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    openSettingsBackUpDialog = false
                                },
                            ) {
                                Text(text = stringResource(id = R.string.dismiss))
                            }
                        },
                        onDismissRequest = {
                            openSettingsBackUpDialog = false
                        }
                    )
                }

                if (openErrorValueDialog) {
                    settingsName = settingsName.trim()
                    AlertDialog(
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = stringResource(id = R.string.name_error),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        title = {
                            Text(text = stringResource(id = R.string.name_error))
                        },
                        text = {
                            Text(
                                text = Core.getContext().resources.getString(
                                    R.string.name_error_message,
                                    errorMessage
                                )
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    openErrorValueDialog = false
                                    openSettingsBackUpDialog = true
                                },
                            ) {
                                Text(text = stringResource(id = R.string.name_reedit))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    openErrorValueDialog = false
                                },
                            ) {
                                Text(text = stringResource(id = R.string.dismiss))
                            }
                        },
                        onDismissRequest = {
                            openErrorValueDialog = false
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp + 20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.large
                        )
                        .padding(10.dp)
                ) {
                    Button(
                        onClick = {
                            openSettingsBackUpDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(id = R.string.settings_back_up),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .width(1.dp + 5.dp)
                            .fillMaxHeight()
                            .padding(horizontal = 2.5.dp)
                            .background(color = MaterialTheme.colorScheme.onPrimary)
                    )

                    Button(
                        onClick = {
                            val ttsHistory =
                                TTSCore.TTSHistory(TTSCore.getComponent().clone(), LocalDateTime.now())
                            if (text.trim().isNotEmpty()) {
                                var duplicate = false
                                favorites.forEach {
                                    if (
                                        it.component.text == text
                                        && it.component.tts.locale == locale
                                        && it.component.tts.pitch == pitch
                                        && it.component.tts.speechRate == speechRate
                                    ) duplicate = true
                                }

                                if (!duplicate) {
                                    favorites.add(ttsHistory)
                                    save()
                                    if (bottomSheetScaffoldState.snackbarHostState.currentSnackbarData == null) {
                                        scope.launch {
                                            bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                                message = Core.getContext().resources.getString(R.string.favorite_added),
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                } else {
                                    if (bottomSheetScaffoldState.snackbarHostState.currentSnackbarData == null) {
                                        scope.launch {
                                            bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                                message = Core.getContext().resources.getString(R.string.favorite_is_duplicate),
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            } else {
                                if (bottomSheetScaffoldState.snackbarHostState.currentSnackbarData == null) {
                                    scope.launch {
                                        bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                            message = Core.getContext().resources.getString(R.string.favorite_text_is_empty),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                            save()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(id = R.string.add_to_favorite),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .width(1.dp + 5.dp)
                            .fillMaxHeight()
                            .padding(horizontal = 2.5.dp)
                            .background(color = MaterialTheme.colorScheme.onPrimary)
                    )

                    Button(
                        onClick = {
                            //todo
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(id = R.string.save_as_file),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }


    @SuppressLint("NewApi")
    fun tell() {

//todo
        //isSpeaking.value = false
        /*TTSCore.getComponent().tts.tts.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onStart(p0: String?) {
                isSpeaking = true
                //TODO("Not yet implemented")
            }

            override fun onDone(p0: String?) {
                //TODO("Not yet implemented")

                isSpeaking = false
            }

            override fun onError(p0: String?) {
                TODO("Not yet implemented")
            }
        })*/

        TTSCore.getComponent().tts.speak(
            text = text,
            onLanguageError = {
                scope.launch {
                    val snackbarResult = bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                        message = "언어 오류다 시발아!",
                        actionLabel = "너 좆됐어!!!!"
                    )
                    if (snackbarResult == SnackbarResult.ActionPerformed) {
                        TTSCore.TTS.startInstallTTS()
                    }
                }
            }
        )

        histories.add(TTSCore.TTSHistory(TTSCore.getComponent(), LocalDateTime.now()))
        save()
    }

    fun stop() {
        isSpeaking = false
    }


    private fun loadWithMessage() {
        load()
            .also {
                scope.launch {
                    if (bottomSheetScaffoldState.snackbarHostState.currentSnackbarData == null) {
                        bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                            message = Core.getContext().resources.getString(R.string.load_data_message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
    }

}