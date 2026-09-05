package com.alzimerahmed.oasisbrowser.browser

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.ThemableBrowserActivity
import com.alzimerahmed.oasisbrowser.animation.AnimationUtils
import com.alzimerahmed.oasisbrowser.browser.bookmark.BookmarkRecyclerViewAdapter
import com.alzimerahmed.oasisbrowser.browser.color.ColorAnimator
import com.alzimerahmed.oasisbrowser.browser.di.MainHandler
import com.alzimerahmed.oasisbrowser.browser.di.injector
import com.alzimerahmed.oasisbrowser.browser.image.ImageLoader
import com.alzimerahmed.oasisbrowser.browser.keys.KeyEventAdapter
import com.alzimerahmed.oasisbrowser.browser.menu.MenuItemAdapter
import com.alzimerahmed.oasisbrowser.browser.menu.MenuSelection
import com.alzimerahmed.oasisbrowser.browser.search.IntentExtractor
import com.alzimerahmed.oasisbrowser.browser.search.SearchListener
import com.alzimerahmed.oasisbrowser.browser.search.StyleRemovingTextWatcher
import com.alzimerahmed.oasisbrowser.browser.tab.BottomDrawerTabRecyclerViewAdapter
import com.alzimerahmed.oasisbrowser.browser.tab.DesktopTabRecyclerViewAdapter
import com.alzimerahmed.oasisbrowser.browser.tab.TabGroup
import com.alzimerahmed.oasisbrowser.browser.tab.GroupedTabRecyclerViewAdapter
import com.alzimerahmed.oasisbrowser.browser.tab.DrawerTabRecyclerViewAdapter
import com.alzimerahmed.oasisbrowser.browser.tab.TabPager
import com.alzimerahmed.oasisbrowser.browser.tab.TabViewHolder
import com.alzimerahmed.oasisbrowser.browser.view.ViewDelegate
import com.alzimerahmed.oasisbrowser.reader.ReaderView
import com.alzimerahmed.oasisbrowser.browser.tab.TabListItem
import com.alzimerahmed.oasisbrowser.browser.tab.TabViewState
import com.alzimerahmed.oasisbrowser.browser.theme.ThemeProvider
import com.alzimerahmed.oasisbrowser.browser.ui.BookmarkConfiguration
import com.alzimerahmed.oasisbrowser.browser.ui.TabConfiguration
import com.alzimerahmed.oasisbrowser.browser.ui.RailUtilityAction
import com.alzimerahmed.oasisbrowser.browser.ui.RailActionId
import com.alzimerahmed.oasisbrowser.browser.ui.UiConfiguration
import com.alzimerahmed.oasisbrowser.browser.view.delegates.BottomTabViewDelegate
import com.alzimerahmed.oasisbrowser.browser.view.delegates.DesktopTabViewDelegate
import com.alzimerahmed.oasisbrowser.browser.view.delegates.DrawerTabViewDelegate
import com.alzimerahmed.oasisbrowser.browser.download.DownloadPermissionsHelper
import com.alzimerahmed.oasisbrowser.browser.data.CookieManagerDialog
import com.alzimerahmed.oasisbrowser.browser.data.CookieManagerRepository
import com.alzimerahmed.oasisbrowser.browser.view.delegates.OasisBrowserRailViewDelegate
import com.alzimerahmed.oasisbrowser.browser.view.targetUrl.LongPress
import com.alzimerahmed.oasisbrowser.system.SystemBarsController
import com.alzimerahmed.oasisbrowser.browser.history.DecoyTimeframe
import com.alzimerahmed.oasisbrowser.constant.HTTP
import com.alzimerahmed.oasisbrowser.database.Bookmark
import com.alzimerahmed.oasisbrowser.database.HistoryEntry
import com.alzimerahmed.oasisbrowser.database.SearchSuggestion
import com.alzimerahmed.oasisbrowser.database.WebPage
import com.alzimerahmed.oasisbrowser.database.downloads.DownloadEntry
import com.alzimerahmed.oasisbrowser.databinding.BrowserActivityBottomBinding
import com.alzimerahmed.oasisbrowser.databinding.BrowserActivityDesktopBinding
import com.alzimerahmed.oasisbrowser.databinding.BrowserActivityDrawerBinding
import com.alzimerahmed.oasisbrowser.databinding.BrowserActivityOasisBrowserBinding
import com.alzimerahmed.oasisbrowser.databinding.BrowserBottomTabsBinding
import com.alzimerahmed.oasisbrowser.dialog.BrowserDialog
import com.alzimerahmed.oasisbrowser.dialog.DialogItem
import com.alzimerahmed.oasisbrowser.dialog.OasisBrowserDialogBuilder
import com.alzimerahmed.oasisbrowser.extensions.color
import com.alzimerahmed.oasisbrowser.extensions.drawable
import com.alzimerahmed.oasisbrowser.extensions.resizeAndShow
import com.alzimerahmed.oasisbrowser.extensions.snackbar
import com.alzimerahmed.oasisbrowser.extensions.takeIfInstance
import com.alzimerahmed.oasisbrowser.extensions.tint
import com.alzimerahmed.oasisbrowser.qr.QrScannerActivity
import com.alzimerahmed.oasisbrowser.vault.VaultActivity
import com.alzimerahmed.oasisbrowser.screenshot.ScreenshotStudioActivity
import com.alzimerahmed.oasisbrowser.browser.engine.BrowserCore
import com.alzimerahmed.oasisbrowser.browser.engine.BrowserCoreChooserActivity
import com.alzimerahmed.oasisbrowser.browser.engine.BrowserCoreSwitchRequest
import com.alzimerahmed.oasisbrowser.browser.engine.AntaresMediaPlayerView
import com.alzimerahmed.oasisbrowser.search.SuggestionsAdapter
import com.alzimerahmed.oasisbrowser.ssl.SslCertificateInfo
import com.alzimerahmed.oasisbrowser.ssl.createSslDrawableForState
import com.alzimerahmed.oasisbrowser.ssl.showSslDialog as showSslCertificateDialog
import com.alzimerahmed.oasisbrowser.utils.ProxyUtils
import com.alzimerahmed.oasisbrowser.utils.value
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import android.view.animation.PathInterpolator
import android.widget.AdapterView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.content.ClipboardManager
import android.util.Patterns
import androidx.appcompat.widget.PopupMenu
import com.alzimerahmed.oasisbrowser.search.SearchEngineProvider
import androidx.core.content.ContextCompat
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import androidx.annotation.OptIn
import androidx.core.graphics.drawable.toDrawable
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.alzimerahmed.oasisbrowser.release.ReleaseUpdateCoordinator
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The base browser activity that governs the browsing experience for both default and incognito
 * browsers.
 */
abstract class BrowserActivity : ThemableBrowserActivity(), BrowserContract.View {

    private var textToSpeech: TextToSpeech? = null
    private var textToSpeechReady = false
    private var pendingSpeechText: String? = null

    private lateinit var binding: ViewDelegate
    private lateinit var systemBarsController: SystemBarsController
    private lateinit var tabsAdapter: ListAdapter<TabListItem, out RecyclerView.ViewHolder>
    private lateinit var bookmarksAdapter: BookmarkRecyclerViewAdapter
    private var activeRecyclerView: RecyclerView? = null
    private var customView: View? = null
    private var customViewOriginalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private var customViewHidSystemUi = false
    private var immersiveFullscreen = false
    private var browserMenuPopup: PopupWindow? = null
    private var bookmarkQuery = ""
    private var currentBookmarks: List<Bookmark> = emptyList()
    private var urlRailTransition: BrowserPresenter.UrlBarTabTransition? = null
    private var railHapticActive = false
    private var railHapticLastMovementAt = 0L
    private var browserCoreReceiverRegistered = false
    private var addressOverlayOpen = false
    private var addressOverlayGeneration = 0
    private var browserChromeGestureActive = false
    private var findCountView: TextView? = null
    private var readerView: ReaderView? = null

    private val browserCoreSwitchReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val core = BrowserCore.fromPreference(
                intent?.getStringExtra(BrowserCoreSwitchRequest.EXTRA_CORE)
            )
            presenter.onBrowserCoreSelected(core)
        }
    }

    private var menuItemShare: MenuItem? = null
    private var menuItemCopyLink: MenuItem? = null

    @Inject internal lateinit var presenter: BrowserPresenter
    @Inject internal lateinit var imageLoader: ImageLoader
    @Inject internal lateinit var themeProvider: ThemeProvider
    @Inject internal lateinit var uiConfiguration: UiConfiguration
    @Inject internal lateinit var intentExtractor: IntentExtractor
    @Inject internal lateinit var downloadPermissionsHelper: DownloadPermissionsHelper
    @Inject internal lateinit var cookieManagerRepository: CookieManagerRepository
    @Inject internal lateinit var oasisbrowserDialogBuilder: OasisBrowserDialogBuilder
    @Inject internal lateinit var searchEngineProvider: SearchEngineProvider
    @Inject internal lateinit var tabPager: TabPager
    @Inject @MainHandler internal lateinit var mainHandler: Handler

    private val clipboardManager: ClipboardManager by lazy {
        getSystemService(ClipboardManager::class.java)
    }

    private val inputMethodManager: InputMethodManager by lazy {
        getSystemService(InputMethodManager::class.java)
    }

    private val vibrator: Vibrator? by lazy {
        getSystemService(Vibrator::class.java)
    }

    private val railHapticStopRunnable: Runnable = Runnable {
        if (System.currentTimeMillis() - railHapticLastMovementAt >= RAIL_HAPTIC_IDLE_TIMEOUT_MS) {
            stopContinuousRailHaptic()
        } else {
            mainHandler.postDelayed(railHapticStopRunnable, RAIL_HAPTIC_IDLE_TIMEOUT_MS)
        }
    }

    private val expressiveSpatialInterpolator by lazy {
        PathInterpolator(0.2f, 0f, 0f, 1f)
    }

    private val expressiveEffectsInterpolator by lazy {
        PathInterpolator(0.3f, 0f, 0.8f, 0.15f)
    }

    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val scannedValue = result.data?.getStringExtra(QrScannerActivity.EXTRA_SCAN_RESULT)
        if (result.resultCode == RESULT_OK && scannedValue != null) {
            presenter.onSearch(scannedValue)
        }
    }

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        presenter.onFileChooserResult(result)
    }

    private fun applyOasisBrowserRailPreferences() {
        configureSearchRefreshOrUtilityButton()
        val barHeight = userPreferences.oasisbrowserRailSize.coerceIn(
            MIN_OasisBrowser_RAIL_WIDTH_DP,
            MAX_OasisBrowser_RAIL_WIDTH_DP
        ).dp
        val superCompact = userPreferences.oasisbrowserRailSize <= SUPER_COMPACT_RAIL_WIDTH_DP &&
            !userPreferences.largeAccessibilityTargetsEnabled
        val hideBar = (userPreferences.fullScreenEnabled && userPreferences.hideRailInFullscreen) ||
            immersiveFullscreen

        binding.toolbarLayout.visibility = if (hideBar) View.GONE else View.VISIBLE
        binding.actionHome.visibility = View.GONE
        binding.actionAddBookmark.visibility = View.GONE
        applyTopBarPreferences(barHeight, superCompact)
        applyQrAndTabsButtonPositions()
    }

    private fun configureSearchRefreshOrUtilityButton() {
        if (uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser) {
            val action = userPreferences.railUtilityAction
            binding.searchRefresh.apply {
                setImageResource(action.iconRes)
                contentDescription = getString(action.labelRes)
                setOnClickListener {
                    when (action) {
                        RailUtilityAction.QR -> presenter.onQrButtonClick()
                        RailUtilityAction.VAULT -> presenter.onVaultButtonClick()
                        RailUtilityAction.SCREENSHOT -> presenter.onScreenshotClick()
                    }
                }
                setOnLongClickListener {
                    when (action) {
                        RailUtilityAction.QR -> presenter.onQrButtonLongClick()
                        RailUtilityAction.VAULT -> presenter.onVaultButtonLongClick()
                        RailUtilityAction.SCREENSHOT -> return@setOnLongClickListener false
                    }
                    true
                }
            }
        } else {
            binding.searchRefresh.setOnClickListener { presenter.onRefreshOrStopClick() }
            binding.searchRefresh.setOnLongClickListener { false }
        }
    }

    private fun setImmersiveFullscreen(enabled: Boolean) {
        immersiveFullscreen = enabled
        if (::systemBarsController.isInitialized) {
            systemBarsController.setImmersiveHidden(enabled || customViewHidSystemUi)
        }
        if (::binding.isInitialized && ::uiConfiguration.isInitialized &&
            uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser
        ) {
            applyOasisBrowserRailPreferences()
        }
        snackbar(if (enabled) R.string.fullscreen_enabled else R.string.fullscreen_disabled)
    }

    /** The top bar is a fixed Chrome-style row; the size preference only sets its height. */
    private fun applyTopBarPreferences(barHeight: Int, superCompact: Boolean) {
        // One adaptive row. Every action keeps a 48dp touch target while the address field
        // expands into the space left between the action groups.
        val buttonSize = HORIZONTAL_RAIL_ACTION_SIZE_DP.dp
        val iconPadding = 12.dp
        val railBinding = binding as? OasisBrowserRailViewDelegate ?: return
        val verticalPadding = 2 * if (superCompact) 1.dp else 8.dp
        binding.toolbarLayout.updateLayoutParams<LinearLayout.LayoutParams> {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        binding.toolbarLayout.minimumHeight = maxOf(barHeight, buttonSize + verticalPadding)
        binding.toolbarLayout.setBackgroundResource(R.drawable.oasisbrowser_rail_background_top)
        binding.toolbarLayout.setPaddingRelative(
            if (superCompact) 1.dp else 10.dp,
            if (superCompact) 1.dp else 8.dp,
            if (superCompact) 1.dp else 10.dp,
            if (superCompact) 1.dp else 8.dp
        )
        binding.tabCountView.setShowCount(!superCompact)
        applySuperCompactTabsButton(superCompact)
        binding.homeButton.setSquareSize(buttonSize)
        binding.settingsButton?.setSquareSize(buttonSize)
        binding.searchRefresh.setSquareSize(buttonSize)
        binding.actionBack.setSquareSize(buttonSize)
        binding.actionForward.setSquareSize(buttonSize)
        binding.actionHome.setSquareSize(buttonSize)
        binding.actionAddBookmark.setSquareSize(buttonSize)
        binding.actionHome.visibility = View.GONE
        binding.actionAddBookmark.visibility = View.GONE
        binding.toolbar.setSquareSize(buttonSize)
        binding.toolbar.minimumHeight = buttonSize
        binding.verticalUrlText?.apply {
            rotation = 0f
            textSize = if (superCompact) 11.5f else 15f
            updateLayoutParams<LinearLayout.LayoutParams> {
                width = 0
                height = ViewGroup.LayoutParams.MATCH_PARENT
                weight = 1f
            }
        }
        binding.addressRail?.apply {
            orientation = LinearLayout.HORIZONTAL
            setPaddingRelative(8.dp, 0, 8.dp, 0)
        }
        railBinding.railTopActions.apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            updateLayoutParams<androidx.constraintlayout.widget.ConstraintLayout.LayoutParams> {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                endToStart = R.id.address_rail
                topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            }
        }
        railBinding.addressTopActions.apply {
            orientation = LinearLayout.HORIZONTAL
            updateLayoutParams<LinearLayout.LayoutParams> {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
        railBinding.addressBottomActions.apply {
            orientation = LinearLayout.HORIZONTAL
            updateLayoutParams<LinearLayout.LayoutParams> {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
        railBinding.railNav.apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        railBinding.railBottomActions.apply {
            orientation = LinearLayout.HORIZONTAL
            updateLayoutParams<LinearLayout.LayoutParams> {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
        listOfNotNull(
            binding.settingsButton,
            binding.searchRefresh,
            binding.actionBack,
            binding.actionForward,
            binding.actionHome,
            binding.actionAddBookmark
        ).forEach { it.setPadding(iconPadding, iconPadding, iconPadding, iconPadding) }
        binding.toolbar.overflowIcon = drawable(R.drawable.ic_action_more_vertical)?.also {
            it.tint(themeProvider.color(R.attr.colorOnSurfaceVariant))
        }
        binding.toolbar.contentInsetStartWithNavigation = 0
        binding.toolbar.setContentInsetsRelative(0, 0)
    }

    private fun applySuperCompactTabsButton(superCompact: Boolean) {
        if (superCompact) {
            binding.homeImageView.apply {
                setImageResource(R.drawable.ic_action_book)
                contentDescription = getString(R.string.tabs)
                visibility = View.VISIBLE
            }
            binding.tabCountView.visibility = View.GONE
        } else {
            binding.homeImageView.visibility = View.GONE
            binding.tabCountView.visibility = View.VISIBLE
        }
    }

    private fun View.setSquareSize(size: Int) {
        updateLayoutParams<ViewGroup.LayoutParams> {
            width = size
            height = size
        }
    }

    /** Renders the user-owned action arrangement without recreating controls or listeners. */
    private fun applyQrAndTabsButtonPositions() {
        renderRailMenuLayout()
    }

    private fun renderRailMenuLayout() {
        val railBinding = binding as? OasisBrowserRailViewDelegate ?: return
        val layout = userPreferences.railMenuLayout
        val staticControls = mapOf(
            RailActionId.TABS to binding.homeButton,
            RailActionId.REFRESH to (binding.settingsButton ?: return),
            RailActionId.UTILITY to binding.searchRefresh,
            RailActionId.BACK to binding.actionBack,
            RailActionId.FORWARD to binding.actionForward,
            RailActionId.HOME to binding.actionHome,
            RailActionId.ADD_BOOKMARK to binding.actionAddBookmark
        )
        val controls = staticControls.values + binding.toolbar
        controls.forEach { control -> (control.parent as? ViewGroup)?.removeView(control) }
        railBinding.railTopActions.removeAllViews()
        railBinding.addressTopActions.removeAllViews()
        railBinding.addressBottomActions.removeAllViews()
        railBinding.railBottomActions.removeAllViews()
        railBinding.railNav.removeAllViews()

        layout.topActions.forEach { action ->
            val control = staticControls[action] ?: createConfiguredRailAction(action)
            railBinding.railTopActions.addView(control, railActionLayoutParams())
        }
        layout.addressActions.forEach { action ->
            val control = staticControls[action] ?: createConfiguredRailAction(action)
            val container = if (layout.addressActions.indexOf(action) == 0) {
                railBinding.addressTopActions
            } else {
                railBinding.addressBottomActions
            }
            container.addView(control, railActionLayoutParams())
        }
        // Preserve the familiar refresh-at-top / utility-at-bottom address preset while still
        // allowing Studio to move any action into this region. The first address action is above
        // the URL text and all following actions are below it.
        railBinding.railNav.addView(railBinding.railBottomActions)
        layout.bottomActions.forEach { action ->
            val control = staticControls[action] ?: createConfiguredRailAction(action)
            railBinding.railBottomActions.addView(control, railActionLayoutParams())
        }
        railBinding.railNav.addView(binding.toolbar, railActionLayoutParams())
    }

    private fun railActionLayoutParams(): LinearLayout.LayoutParams {
        val size = binding.actionBack.layoutParams.width.takeIf { it > 0 } ?: 42.dp
        return LinearLayout.LayoutParams(size, size).apply {
            gravity = Gravity.CENTER
        }
    }

    private fun createConfiguredRailAction(action: RailActionId): ImageButton = ImageButton(this).apply {
        background = drawable(R.drawable.oasisbrowser_blend_button_background)
        contentDescription = railActionContentDescription(action)
        setImageResource(railActionIcon(action))
        setColorFilter(themeProvider.color(R.attr.iconColor))
        setPadding(8.dp, 8.dp, 8.dp, 8.dp)
        isFocusable = true
        isClickable = true
        isEnabled = railActionAvailable(action)
        setOnClickListener { runConfiguredRailAction(action) }
    }

    private fun railActionAvailable(action: RailActionId): Boolean = when (action) {
        RailActionId.BLOCK_ELEMENT, RailActionId.COOKIE_MANAGER ->
            presenter.viewState.displayUrl.startsWith("http://") || presenter.viewState.displayUrl.startsWith("https://")
        else -> true
    }

    private fun runConfiguredRailAction(action: RailActionId) {
        if (!railActionAvailable(action)) return
        when (action) {
            RailActionId.REFRESH -> presenter.onReloadClick()
            RailActionId.UTILITY -> binding.searchRefresh.performClick()
            RailActionId.BACK -> presenter.onBackClick()
            RailActionId.FORWARD -> presenter.onForwardClick()
            RailActionId.HOME -> presenter.onHomeClick()
            RailActionId.ADD_BOOKMARK -> presenter.onStarClick()
            RailActionId.NEW_TAB -> presenter.onMenuClick(MenuSelection.NEW_TAB)
            RailActionId.INCOGNITO -> presenter.onMenuClick(MenuSelection.NEW_INCOGNITO_TAB)
            RailActionId.FEELING_LUCKY -> presenter.onMenuClick(MenuSelection.FEELING_LUCKY)
            RailActionId.ADD_TO_HOME -> presenter.onMenuClick(MenuSelection.ADD_TO_HOME)
            RailActionId.HISTORY -> presenter.onMenuClick(MenuSelection.HISTORY)
            RailActionId.DOWNLOADS -> presenter.onMenuClick(MenuSelection.DOWNLOADS)
            RailActionId.BOOKMARKS -> presenter.onMenuClick(MenuSelection.BOOKMARKS)
            RailActionId.FIND -> presenter.onMenuClick(MenuSelection.FIND)
            RailActionId.READ_ALOUD -> presenter.onReadPageAloud()
            RailActionId.COPY_LINK -> presenter.onMenuClick(MenuSelection.COPY_LINK)
            RailActionId.SCREENSHOT -> presenter.onScreenshotClick()
            RailActionId.USER_AGENT -> presenter.onUserAgentMenuClick()
            RailActionId.BLOCK_ELEMENT -> presenter.onPickElement()
            RailActionId.COOKIE_MANAGER -> presenter.onCookieManager()
            RailActionId.SETTINGS -> presenter.onMenuClick(MenuSelection.SETTINGS)
            else -> Unit
        }
    }

    @DrawableRes
    private fun railActionIcon(action: RailActionId): Int = when (action) {
        RailActionId.TABS -> R.drawable.ic_action_tabs
        RailActionId.REFRESH -> R.drawable.ic_action_refresh
        RailActionId.UTILITY -> userPreferences.railUtilityAction.iconRes
        RailActionId.BACK -> R.drawable.ic_action_back
        RailActionId.FORWARD -> R.drawable.ic_action_forward
        RailActionId.HOME -> R.drawable.ic_action_home
        RailActionId.ADD_BOOKMARK -> R.drawable.ic_action_star
        RailActionId.NEW_TAB -> R.drawable.ic_action_plus
        RailActionId.INCOGNITO -> R.drawable.incognito_mode
        RailActionId.FEELING_LUCKY -> R.drawable.ic_action_invert
        RailActionId.ADD_TO_HOME -> R.drawable.ic_webpage
        RailActionId.HISTORY -> R.drawable.ic_history
        RailActionId.DOWNLOADS -> R.drawable.ic_settings_download
        RailActionId.BOOKMARKS -> R.drawable.ic_bookmark
        RailActionId.FIND -> R.drawable.ic_search
        RailActionId.READ_ALOUD -> R.drawable.ic_settings_audio
        RailActionId.COPY_LINK -> R.drawable.ic_insert
        RailActionId.SCREENSHOT -> R.drawable.ic_action_screenshot
        RailActionId.USER_AGENT -> R.drawable.ic_action_desktop
        RailActionId.BLOCK_ELEMENT -> R.drawable.ic_settings_text
        RailActionId.COOKIE_MANAGER -> R.drawable.ic_settings_privacy
        RailActionId.SETTINGS -> R.drawable.ic_action_settings
        else -> R.drawable.ic_action_more_vertical
    }

    private fun railActionLabel(action: RailActionId): Int = when (action) {
        RailActionId.TABS -> R.string.tabs
        RailActionId.REFRESH -> R.string.action_refresh
        RailActionId.UTILITY -> userPreferences.railUtilityAction.labelRes
        RailActionId.BACK -> R.string.action_back
        RailActionId.FORWARD -> R.string.action_forward
        RailActionId.HOME -> R.string.action_homepage
        RailActionId.ADD_BOOKMARK -> R.string.action_add_bookmark
        RailActionId.NEW_TAB -> R.string.action_new_tab
        RailActionId.INCOGNITO -> R.string.action_incognito
        RailActionId.FEELING_LUCKY -> R.string.action_feeling_lucky
        RailActionId.ADD_TO_HOME -> R.string.action_add_to_homescreen
        RailActionId.HISTORY -> R.string.action_history
        RailActionId.DOWNLOADS -> R.string.action_downloads
        RailActionId.BOOKMARKS -> R.string.action_bookmarks
        RailActionId.FIND -> R.string.action_find
        RailActionId.READ_ALOUD -> R.string.action_read_aloud
        RailActionId.COPY_LINK -> R.string.action_copy
        RailActionId.SCREENSHOT -> R.string.action_screenshot
        RailActionId.USER_AGENT -> R.string.display_as
        RailActionId.BLOCK_ELEMENT -> R.string.block_element
        RailActionId.COOKIE_MANAGER -> R.string.cookie_manager
        RailActionId.SETTINGS -> R.string.settings
        else -> R.string.action_more
    }

    private fun railActionContentDescription(action: RailActionId): String =
        if (action == RailActionId.USER_AGENT) {
            getString(
                R.string.display_as_current,
                when (userPreferences.userAgentChoice) {
                    2 -> getString(R.string.agent_desktop)
                    3 -> getString(R.string.agent_mobile)
                    4 -> getString(R.string.agent_custom)
                    5 -> getString(R.string.agent_folding)
                    else -> getString(R.string.agent_default)
                }
            )
        } else {
            getString(railActionLabel(action))
        }

    private fun toolbarLayoutForRail(): androidx.constraintlayout.widget.ConstraintLayout = binding.toolbarLayout

    private fun horizontalRailButtonParams(width: Int, height: Int) =
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(width, height).apply {
            startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
        }

    private fun View.setTopMargin(topMargin: Int) {
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.topMargin = topMargin
        }
    }

    private fun View.setStartMargin(startMargin: Int) {
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = startMargin
        }
    }

    private inline fun <reified T : ViewGroup.LayoutParams> View.updateLayoutParams(
        block: T.() -> Unit
    ) {
        val params = layoutParams as? T ?: return
        params.block()
        layoutParams = params
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).roundToInt()

    protected abstract fun isIncognito(): Boolean

    @MenuRes
    protected abstract fun menu(): Int

    @DrawableRes
    protected abstract fun homeIcon(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabConfiguration = TabConfiguration.OasisBrowser
        val bottomTabsBinding = if (tabConfiguration == TabConfiguration.DRAWER_BOTTOM) {
            BrowserBottomTabsBinding.inflate(layoutInflater)
        } else {
            null
        }

        binding = when (tabConfiguration) {
            TabConfiguration.DESKTOP -> DesktopTabViewDelegate(
                BrowserActivityDesktopBinding.inflate(layoutInflater)
            )
            TabConfiguration.DRAWER_SIDE -> DrawerTabViewDelegate(
                BrowserActivityDrawerBinding.inflate(layoutInflater)
            )
            TabConfiguration.DRAWER_BOTTOM -> BottomTabViewDelegate(
                BrowserActivityBottomBinding.inflate(layoutInflater)
            )
            TabConfiguration.OasisBrowser -> OasisBrowserRailViewDelegate(
                BrowserActivityOasisBrowserBinding.inflate(layoutInflater)
            )
        }

        setContentView(binding.root)
        initFindInPageUi()
        systemBarsController = SystemBarsController(
            activity = this,
            protectionView = binding.root.findViewById(R.id.status_bar_protection),
            userPreferences = userPreferences
        )
        systemBarsController.apply()
        setSupportActionBar(binding.toolbar)

        injector.browser2ComponentBuilder()
            .activity(this)
            .browserFrame(binding.contentFrame)
            .toolbarRoot(binding.uiLayout)
            .browserRoot(binding.browserLayoutContainer)
            .bottomTabsLayout(bottomTabsBinding)
            .toolbar(binding.toolbarLayout)
            .initialIntent(intent)
            .incognitoMode(isIncognito())
            .build()
            .inject(this)
        // Rail configuration reads UiConfiguration, which is provided by the activity component.
        // Apply it only after injection so cold starts do not access an uninitialised property.
        applyOasisBrowserRailPreferences()
        configureOasisBrowserOverflowMenu()

        if (uiConfiguration.tabConfiguration == TabConfiguration.DESKTOP) {
            tabsAdapter = DesktopTabRecyclerViewAdapter(
                context = this,
                onClick = presenter::onTabClick,
                onLongClick = presenter::onTabLongClick,
                onCloseClick = presenter::onTabClose
            )
            binding.desktopTabsList.adapter = tabsAdapter
            binding.desktopTabsList.layoutManager =
                LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
            binding.desktopTabsList.itemAnimator?.takeIfInstance<SimpleItemAnimator>()
                ?.supportsChangeAnimations = false
            binding.drawerTabsList.isVisible = false
            activeRecyclerView = binding.desktopTabsList
        } else {
            tabsAdapter = if (uiConfiguration.tabConfiguration == TabConfiguration.DRAWER_BOTTOM) {
                BottomDrawerTabRecyclerViewAdapter(
                    themeProvider = themeProvider,
                    onClick = presenter::onTabClick,
                    onLongClick = presenter::onTabLongClick,
                    onCloseClick = presenter::onTabClose,
                    onBackClick = { presenter.onBackClick() },
                    onForwardClick = { presenter.onForwardClick() },
                    onHomeClick = { presenter.onHomeClick() }
                )
            } else {
                GroupedTabRecyclerViewAdapter(
                    onClick = presenter::onTabClick,
                    onLongClick = presenter::onTabLongClick,
                    onCloseClick = presenter::onTabClose,
                    onGroupHeaderClick = presenter::onTabGroupHeaderClick,
                    onGroupCloseClick = presenter::onTabGroupCloseClick
                )
            }
            binding.drawerTabsList.adapter = tabsAdapter
            binding.drawerTabsList.layoutManager = LinearLayoutManager(this)
            binding.drawerTabsList.itemAnimator?.takeIfInstance<SimpleItemAnimator>()
                ?.supportsChangeAnimations = false
            binding.desktopTabsList.isVisible = false
            activeRecyclerView = binding.drawerTabsList

            if (uiConfiguration.tabConfiguration == TabConfiguration.DRAWER_SIDE || uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser) {
                binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        if (drawerView == binding.tabDrawer) {
                            presenter.onTabDrawerMoved(slideOffset > 0f)
                        } else if (drawerView == binding.bookmarkDrawer) {
                            presenter.onBookmarkDrawerMoved(slideOffset > 0f)
                        }
                    }

                    override fun onDrawerOpened(drawerView: View) {
                        if (drawerView == binding.tabDrawer) {
                            presenter.onTabDrawerMoved(true)
                        } else if (drawerView == binding.bookmarkDrawer) {
                            presenter.onBookmarkDrawerMoved(true)
                        }
                    }

                    override fun onDrawerClosed(drawerView: View) {
                        if (drawerView == binding.tabDrawer) {
                            presenter.onTabDrawerMoved(false)
                        } else if (drawerView == binding.bookmarkDrawer) {
                            presenter.onBookmarkDrawerMoved(false)
                        }
                    }
                })
            }
        }

        bookmarksAdapter = BookmarkRecyclerViewAdapter(
            onClick = presenter::onBookmarkClick,
            onLongClick = presenter::onBookmarkLongClick,
            imageLoader = imageLoader,
            showFavicons = { userPreferences.bookmarkFaviconsEnabled }
        )
        binding.bookmarkListView.adapter = bookmarksAdapter
        binding.bookmarkListView.layoutManager = LinearLayoutManager(this)
        binding.bookmarkSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                bookmarkQuery = s?.toString().orEmpty()
                updateBookmarkList(currentBookmarks)
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        presenter.onViewAttached(BrowserStateAdapter(this))
        ContextCompat.registerReceiver(
            this,
            browserCoreSwitchReceiver,
            IntentFilter(BrowserCoreSwitchRequest.ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        browserCoreReceiverRegistered = true
        maybeShowFirstRunDonationDialog()
        window.decorView.post {
            lifecycleScope.launch {
                ReleaseUpdateCoordinator(this@BrowserActivity, userPreferences).check(this@BrowserActivity)
            }
        }

        val suggestionsAdapter = SuggestionsAdapter(this, isIncognito = isIncognito()).apply {
            onSuggestionInsertClick = {
                if (it is SearchSuggestion) {
                    binding.search.setText(it.title)
                    binding.search.setSelection(it.title.length)
                } else {
                    binding.search.setText(it.url)
                    binding.search.setSelection(it.url.length)
                }
            }
        }
        binding.search.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            binding.search.clearFocus()
            hideAddressOverlay()
            presenter.onSearchSuggestionClicked(suggestionsAdapter.getItem(position) as WebPage)
            inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
        binding.search.setAdapter(suggestionsAdapter)
        val searchListener = SearchListener(
            onConfirm = {
                presenter.onSearch(binding.search.text.toString())
                hideAddressOverlay()
            },
            inputMethodManager = inputMethodManager
        )
        binding.search.setOnEditorActionListener(searchListener)
        binding.search.setOnKeyListener(searchListener)
        binding.search.addTextChangedListener(StyleRemovingTextWatcher())
        binding.search.setOnFocusChangeListener { _, hasFocus ->
            presenter.onSearchFocusChanged(hasFocus)
            if (hasFocus) {
                binding.search.selectAll()
                if (!addressOverlayOpen) showAddressOverlay()
            } else if (addressOverlayOpen) {
                hideAddressOverlay()
            }
        }
        binding.search.isLongClickable = true
        binding.search.setOnLongClickListener {
            showSearchEnginePopup()
            true
        }

        binding.findPrevious.setOnClickListener { presenter.onFindPrevious() }
        binding.findNext.setOnClickListener { presenter.onFindNext() }
        binding.findQuit.setOnClickListener {
            binding.findBar.isVisible = false
            binding.findQuery.clearFocus()
            inputMethodManager.hideSoftInputFromWindow(binding.findQuery.windowToken, 0)
            presenter.onFindDismiss()
        }
        binding.findQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onFindInPage(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
        binding.findQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                presenter.onFindNext()
                true
            } else {
                false
            }
        }
        listOfNotNull(
            binding.homeButton,
            binding.actionBack,
            binding.actionForward,
            binding.actionHome,
            binding.newTabButton,
            binding.searchRefresh,
            binding.searchQr,
            binding.actionAddBookmark,
            binding.tabHeaderButton,
            binding.bookmarkBackButton,
            binding.settingsButton,
            binding.verticalUrlText?.parent as? View
        ).forEach {
            it.applyPhysicalPressFeedback(
                pressInterpolator = expressiveEffectsInterpolator,
                releaseInterpolator = expressiveSpatialInterpolator
            )
        }

        binding.homeButton.setOnClickListener {
            if (uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser) {
                toggleTabDrawerDirectly()
            } else {
                presenter.onTabCountViewClick(
                    drawerIsOpen = binding.drawerLayout.isDrawerOpen(binding.tabDrawer)
                )
            }
        }
        if (uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser) {
            // DrawerLayout's edge swipe begins on ACTION_DOWN and remains reliable even while an
            // embedded Antares hierarchy owns page focus. Mirror that path for the rail button:
            // invoke the same DrawerLayout operation immediately instead of waiting for Android's
            // focus-sensitive ACTION_UP click synthesis. performClick preserves accessibility and
            // keyboard activation through the normal click listener above.
            binding.homeButton.setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        view.isPressed = true
                        view.performClick()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.isPressed = false
                        true
                    }
                    else -> true
                }
            }
        }
        binding.actionBack.setOnClickListener { presenter.onBackClick() }
        binding.actionForward.setOnClickListener { presenter.onForwardClick() }
        binding.actionHome.setOnClickListener { presenter.onHomeClick() }
        binding.actionHome.setOnLongClickListener {
            setImmersiveFullscreen(!immersiveFullscreen)
            true
        }
        binding.newTabButton.setOnClickListener { presenter.onNewTabClick() }
        binding.newTabButton.setOnLongClickListener {
            presenter.onNewTabLongClick()
            true
        }
        configureSearchRefreshOrUtilityButton()
        binding.actionAddBookmark.setOnClickListener { presenter.onStarClick() }
        binding.actionPageTools.setOnClickListener { presenter.onToolsClick() }
        binding.tabHeaderButton.setOnClickListener { presenter.onTabMenuClick() }
        binding.bookmarkBackButton.setOnClickListener { presenter.onBookmarkMenuClick() }
        binding.searchSslStatus.setOnClickListener { presenter.onSslIconClick() }
        binding.verticalUrlText?.setOnClickListener { showAddressOverlay() }
        (binding.verticalUrlText?.parent as? View)?.setOnClickListener { showAddressOverlay() }
        installUrlRailGestures()
        binding.settingsButton?.setOnClickListener {
            presenter.onReloadClick()
        }
        binding.settingsButton?.setOnLongClickListener {
            presenter.onJavaScriptDisabledReload()
            true
        }

        binding.searchQr?.setOnClickListener { presenter.onQrButtonClick() }
        binding.searchQr?.setOnLongClickListener {
            presenter.onQrButtonLongClick()
            true
        }

        tabPager.longPressListener = presenter::onPageLongPress

        onBackPressedDispatcher.addCallback {
            if (readerView?.isVisible == true) {
                presenter.onExitReaderMode()
            } else if (immersiveFullscreen) {
                setImmersiveFullscreen(false)
            } else {
                presenter.onNavigateBack()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        intentExtractor.extractUrlFromIntent(intent)?.let(presenter::onNewAction)
        super.onNewIntent(intent)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (::binding.isInitialized && event.actionMasked == MotionEvent.ACTION_DOWN) {
            // The homepage is a native surface, so it does not provide the WebView's usual
            // focus-loss path. Dismiss the expanded address editor when the user taps anywhere
            // outside it, including homepage shortcuts and rail actions.
            if (addressOverlayOpen && !event.isInside(binding.addressOverlay ?: binding.toolbarLayout)) {
                hideAddressOverlay()
            }
            if (event.isInside(binding.toolbarLayout)) {
            browserChromeGestureActive = true
            // Release embedded Antares input before dispatching the rail gesture. Persistent
            // chrome such as the address editor, drawer or overflow menu keeps it released after
            // ACTION_UP through the presenter's combined state calculation.
            presenter.onBrowserChromeGestureMoved(true)
            }
        }

        val handled = super.dispatchTouchEvent(event)
        if (browserChromeGestureActive &&
            (event.actionMasked == MotionEvent.ACTION_UP ||
                event.actionMasked == MotionEvent.ACTION_CANCEL)
        ) {
            browserChromeGestureActive = false
            presenter.onBrowserChromeGestureMoved(false)
        }
        return handled
    }

    private fun MotionEvent.isInside(view: View): Boolean {
        if (!view.isShown) return false
        val bounds = Rect()
        return view.getGlobalVisibleRect(bounds) && bounds.contains(rawX.toInt(), rawY.toInt())
    }

    override fun onDestroy() {
        if (browserCoreReceiverRegistered) {
            unregisterReceiver(browserCoreSwitchReceiver)
            browserCoreReceiverRegistered = false
        }
        stopContinuousRailHaptic()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        browserMenuPopup?.dismiss()
        super.onDestroy()
        presenter.onViewDetached()
    }

    override fun onPause() {
        stopContinuousRailHaptic()
        super.onPause()
        presenter.onViewHidden()
    }

    override fun onResume() {
        super.onResume()
        presenter.onViewResumed()
        if (::systemBarsController.isInitialized) systemBarsController.apply()
        if (::binding.isInitialized && ::uiConfiguration.isInitialized &&
            uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser
        ) {
            applyOasisBrowserRailPreferences()
        }
        intentExtractor.extractUrlFromIntent(intent)?.let(presenter::onNewAction)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && ::systemBarsController.isInitialized) {
            systemBarsController.applyAfterWindowFocus()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (::uiConfiguration.isInitialized &&
            uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser
        ) {
            return false
        }
        menuInflater.inflate(R.menu.main, menu)
        runCatching {
            menu.javaClass
                .getDeclaredMethod("setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
                .invoke(menu, true)
        }
        menuItemShare = menu.findItem(R.id.action_share)
        menuItemCopyLink = menu.findItem(R.id.action_copy)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (::uiConfiguration.isInitialized &&
            uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser
        ) {
            return false
        }
        menuItemShare?.isVisible = presenter.viewState.enableFullMenu
        menuItemCopyLink?.isVisible = presenter.viewState.enableFullMenu
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_tab -> presenter.onMenuClick(MenuSelection.NEW_TAB)
            R.id.action_incognito -> presenter.onMenuClick(MenuSelection.NEW_INCOGNITO_TAB)
            R.id.action_share -> presenter.onMenuClick(MenuSelection.SHARE)
            R.id.action_history -> presenter.onMenuClick(MenuSelection.HISTORY)
            R.id.action_downloads -> presenter.onMenuClick(MenuSelection.DOWNLOADS)
            R.id.action_reading_list -> presenter.onMenuClick(MenuSelection.READING_LIST)
            R.id.action_find -> presenter.onMenuClick(MenuSelection.FIND)
            R.id.action_reader_mode -> presenter.onMenuClick(MenuSelection.READER_MODE)
            R.id.action_copy -> presenter.onMenuClick(MenuSelection.COPY_LINK)
            R.id.action_add_to_collection -> presenter.onMenuClick(MenuSelection.ADD_TO_COLLECTION)
            R.id.action_bookmarks -> presenter.onMenuClick(MenuSelection.BOOKMARKS)
            R.id.action_settings -> presenter.onMenuClick(MenuSelection.SETTINGS)
            R.id.action_browser_core -> openBrowserCoreChooser()
            R.id.action_add_to_homescreen -> presenter.onMenuClick(MenuSelection.ADD_TO_HOME)
            R.id.action_add_bookmark -> presenter.onMenuClick(MenuSelection.ADD_BOOKMARK)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configureOasisBrowserOverflowMenu() {
        if (uiConfiguration.tabConfiguration != TabConfiguration.OasisBrowser) {
            return
        }
        binding.toolbar.menu.clear()
        binding.toolbar.navigationIcon = drawable(R.drawable.ic_action_more_vertical)?.also {
            it.tint(themeProvider.color(R.attr.colorOnSurfaceVariant))
        }
        binding.toolbar.setNavigationOnClickListener { showBrowserOverflowMenu() }
        binding.toolbar.setOnClickListener { showBrowserOverflowMenu() }
    }

    private fun showBrowserOverflowMenu() {
        browserMenuPopup?.dismiss()
        presenter.onBrowserMenuMoved(true)
        val menuView = buildBrowserOverflowMenuView()
        val popupWidth = resources.displayMetrics.widthPixels
            .coerceAtMost(BROWSER_MENU_MAX_WIDTH_DP.dp + (BROWSER_MENU_SCREEN_MARGIN_DP * 2).dp) -
            (BROWSER_MENU_SCREEN_MARGIN_DP * 2).dp
        browserMenuPopup = PopupWindow(
            menuView,
            popupWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(android.graphics.Color.TRANSPARENT.toDrawable())
            elevation = 18.dp.toFloat()
            isOutsideTouchable = true
            setAnimationStyle(android.R.style.Animation_Dialog)
            setOnDismissListener {
                presenter.onBrowserMenuMoved(false)
                if (browserMenuPopup === this) browserMenuPopup = null
            }
        }

        val location = IntArray(2)
        binding.toolbar.getLocationOnScreen(location)
        val x = ((resources.displayMetrics.widthPixels - popupWidth) / 2).coerceAtLeast(0)
        val y = (location[1] + binding.toolbarLayout.height + 12.dp)
            .coerceAtMost(resources.displayMetrics.heightPixels - BROWSER_MENU_SCREEN_MARGIN_DP.dp)
        browserMenuPopup?.showAtLocation(binding.root, Gravity.TOP or Gravity.START, x, y)
        menuView.alpha = 0f
        menuView.scaleX = 0.96f
        menuView.scaleY = 0.96f
        menuView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(220L)
            .setInterpolator(expressiveSpatialInterpolator)
            .start()
    }

    private fun buildBrowserOverflowMenuView(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = drawable(R.drawable.browser_overflow_menu_background)
            clipToOutline = true
            elevation = 18.dp.toFloat()
            setPadding(9.dp, 9.dp, 9.dp, 9.dp)
        }

        val layout = userPreferences.railMenuLayout
        val quickActions = layout.quickActions.filter(::railActionAvailable)
        if (layout.quickActionsEnabled && quickActions.isNotEmpty()) {
            container.addView(LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    42.dp
                ).apply { bottomMargin = 5.dp }
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL
                quickActions.forEach { action -> addView(createQuickActionButton(action)) }
            })
            container.addView(createMenuDivider())
        }

        var browserCoreAdded = false
        layout.visibleOverflowActions
            .filter(::railActionAvailable)
            .forEach { action ->
                if (action == RailActionId.FEELING_LUCKY) {
                    container.addBrowserCoreMenuRow()
                    browserCoreAdded = true
                }
                container.addView(createActionMenuRow(railActionIcon(action), railActionContentDescription(action)) {
                    browserMenuPopup?.dismiss()
                    runConfiguredRailAction(action)
                })
            }

        // Feeling Lucky can be moved out of the overflow menu in Rail & Menu Studio. Keep the
        // core chooser available in that case, but otherwise place it exactly above that action.
        if (!browserCoreAdded) {
            container.addView(createMenuDivider())
            container.addBrowserCoreMenuRow()
        }

        return container
    }

    private fun LinearLayout.addBrowserCoreMenuRow() {
        addView(
            createActionMenuRow(
                R.drawable.ic_settings_globe,
                getString(R.string.browser_core_menu_shortcut),
                ::openBrowserCoreChooser
            )
        )
    }

    private fun openBrowserCoreChooser() {
        browserMenuPopup?.dismiss()
        startActivity(
            Intent(this, BrowserCoreChooserActivity::class.java)
                .putExtra(BrowserCoreChooserActivity.EXTRA_MANAGE_ONLY, true)
        )
    }

    private fun createQuickActionButton(action: RailActionId): ImageButton =
        ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(40.dp, 40.dp).apply {
                marginStart = 2.dp
                marginEnd = 2.dp
            }
            background = drawable(R.drawable.browser_overflow_quick_button_background)
            contentDescription = railActionContentDescription(action)
            setPadding(8.dp, 8.dp, 8.dp, 8.dp)
            setImageResource(railActionIcon(action))
            setColorFilter(themeProvider.color(R.attr.colorOnSurface))
            scaleType = ImageView.ScaleType.CENTER
            isEnabled = railActionAvailable(action)
            setOnClickListener {
                browserMenuPopup?.dismiss()
                runConfiguredRailAction(action)
            }
        }

    private fun createMenuRow(
        icon: Int,
        title: Int,
        selection: MenuSelection
    ): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = drawable(R.drawable.browser_overflow_menu_item_background)
        isClickable = true
        isFocusable = true
        setPadding(4.dp, 3.dp, 6.dp, 3.dp)
        minimumHeight = 38.dp
        addView(ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(30.dp, 30.dp)
            setPadding(6.dp, 6.dp, 6.dp, 6.dp)
            setImageResource(icon)
            setColorFilter(themeProvider.color(R.attr.colorOnSurfaceVariant))
        })
        addView(TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginStart = 7.dp }
            text = getString(title)
            setTextColor(themeProvider.color(R.attr.colorOnSurface))
            textSize = 15f
            maxLines = 1
            includeFontPadding = true
        })
        setOnClickListener {
            browserMenuPopup?.dismiss()
            presenter.onMenuClick(selection)
        }
    }

    private fun createActionMenuRow(icon: Int, title: String, action: () -> Unit): View =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = drawable(R.drawable.browser_overflow_menu_item_background)
            isClickable = true
            isFocusable = true
            setPadding(4.dp, 3.dp, 6.dp, 3.dp)
            minimumHeight = 38.dp
            addView(ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(30.dp, 30.dp)
                setPadding(6.dp, 6.dp, 6.dp, 6.dp)
                setImageResource(icon)
                setColorFilter(themeProvider.color(R.attr.colorOnSurfaceVariant))
            })
            addView(TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { marginStart = 7.dp }
                text = title
                setTextColor(themeProvider.color(R.attr.colorOnSurface))
                textSize = 15f
                maxLines = 1
            })
            setOnClickListener {
                browserMenuPopup?.dismiss()
                action()
            }
        }

    private fun createMenuDivider(): View =
        View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
                1.dp
            ).apply {
                setMargins(4.dp, 5.dp, 4.dp, 5.dp)
            }
            setBackgroundColor(themeProvider.color(R.attr.colorOutlineVariant))
            alpha = 0.7f
        }

    /**
     * @see BrowserContract.View.openBookmarkDrawer
     */
    override fun openBookmarkDrawer() {
        binding.drawerLayout.openDrawer(binding.bookmarkDrawer)
    }

    /**
     * @see BrowserContract.View.closeBookmarkDrawer
     */
    override fun closeBookmarkDrawer() {
        binding.drawerLayout.closeDrawer(binding.bookmarkDrawer)
    }

    /**
     * @see BrowserContract.View.openTabDrawer
     */
    override fun openTabDrawer() {
        binding.drawerLayout.openDrawer(binding.tabDrawer)
    }

    /**
     * @see BrowserContract.View.closeTabDrawer
     */
    override fun closeTabDrawer() {
        binding.drawerLayout.closeDrawer(binding.tabDrawer)
    }

    private fun toggleTabDrawerDirectly() {
        if (binding.drawerLayout.isDrawerOpen(binding.tabDrawer)) {
            binding.drawerLayout.closeDrawer(binding.tabDrawer, true)
        } else {
            // Set the persistent chrome state before the opening animation begins. The drawer's
            // own callbacks keep this state in sync from this point, exactly as with an edge swipe.
            presenter.onTabDrawerMoved(true)
            binding.drawerLayout.openDrawer(binding.tabDrawer, true)
        }
    }

    /**
     * @see BrowserContract.View.showToolbar
     */
    override fun showToolbar() {
        if (uiConfiguration.tabConfiguration != TabConfiguration.OasisBrowser) {
            binding.uiLayout.animate().translationY(0f).setDuration(200).start()
        }
    }

    /**
     * @see BrowserContract.View.showToolsDialog
     */
    override fun showToolsDialog(
        areAdsAllowed: Boolean,
        shouldShowAdBlockOption: Boolean,
        shouldShowElementPicker: Boolean
    ) {
        BrowserDialog.showWithIcons(
            this, getString(R.string.dialog_tools_title),
            DialogItem(
                title = R.string.dialog_toggle_desktop,
                icon = R.drawable.ic_action_desktop,
                onClick = presenter::onToggleDesktopAgent
            ),
            DialogItem(
                title = if (areAdsAllowed) R.string.dialog_adblock_disable_for_site else R.string.dialog_adblock_enable_for_site,
                icon = R.drawable.ic_block,
                isConditionMet = shouldShowAdBlockOption,
                onClick = presenter::onToggleAdBlocking
            ),
            DialogItem(
                title = R.string.block_element,
                icon = R.drawable.ic_settings_text,
                isConditionMet = shouldShowElementPicker,
                onClick = presenter::onPickElement
            ),
            DialogItem(
                title = R.string.cookie_manager,
                icon = R.drawable.ic_settings_privacy,
                isConditionMet = shouldShowElementPicker,
                onClick = presenter::onCookieManager
            )
        )
    }

    override fun showUserAgentDialog(currentChoice: Int) {
        val choices = resources.getStringArray(R.array.user_agent)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.display_as)
            .setSingleChoiceItems(choices, (currentChoice - 1).coerceIn(0, choices.lastIndex)) { dialog, which ->
                presenter.onUserAgentChoiceSelected(which + 1)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .resizeAndShow()
    }

    override fun showCustomUserAgentDialog(currentValue: String) {
        BrowserDialog.showEditText(
            this,
            R.string.display_as,
            R.string.agent_custom,
            currentValue,
            R.string.action_ok
        ) { presenter.onCustomUserAgentEntered(it) }
    }

    override fun showCookieManager(url: String) {
        CookieManagerDialog.show(this, url, cookieManagerRepository)
    }

    override fun showScreenshot(bitmap: Bitmap) {
        showScreenshotAnimation(bitmap)
        lifecycleScope.launch {
            runCatching {
                val file = withContext(Dispatchers.IO) {
                    File(cacheDir, "shared/screenshot-studio-source.png").also { it.parentFile?.mkdirs() }.apply {
                        FileOutputStream(this).use { output -> check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) }
                    }
                }
                delay(SCREENSHOT_ANIMATION_DURATION_MS)
                startActivity(Intent(this@BrowserActivity, ScreenshotStudioActivity::class.java).apply {
                    putExtra(ScreenshotStudioActivity.EXTRA_PATH, file.absolutePath)
                })
            }
                .onFailure { snackbar(R.string.screenshot_failed) }
        }
    }

    override fun showScreenshotCaptureFailed() {
        snackbar(R.string.screenshot_failed)
    }

    override fun showBrowserCoreSwitchFailed() {
        snackbar(R.string.antares_switch_failed)
    }

    override fun showUndoTabCloseSnackbar(onUndo: () -> Unit) {
        Snackbar.make(binding.root, R.string.tab_closed, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) { onUndo() }
            .setActionTextColor(MaterialColors.getColor(this, R.attr.colorPrimary, 0))
            .show()
    }

    override fun openVault() {
        startActivity(Intent(this, VaultActivity::class.java))
    }

    override fun showVaultSaved() {
        snackbar(R.string.vault_saved)
    }

    override fun showVaultSaveFailed() {
        snackbar(R.string.vault_save_failed)
    }

    private fun showScreenshotAnimation(bitmap: Bitmap) {
        val contentFrame = binding.contentFrame
        if (contentFrame.width <= 0 || contentFrame.height <= 0) return

        val radius = 24.dp.toFloat()
        val outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
        fun View.applyScreenshotShape() {
            this.outlineProvider = outlineProvider
            this.clipToOutline = true
            this.pivotX = contentFrame.width / 2f
            this.pivotY = contentFrame.height / 2f
        }

        val snapshot = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.FIT_XY
            applyScreenshotShape()
        }
        val whiteFilter = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
            alpha = 0.5f
            applyScreenshotShape()
        }

        contentFrame.addView(snapshot)
        contentFrame.addView(whiteFilter)
        if (userPreferences.hapticsEnabled && userPreferences.screenshotHapticsEnabled) {
            vibrator?.takeIf { it.hasVibrator() }?.vibrate(
                    VibrationEffect.createOneShot(
                        userPreferences.screenshotHapticDurationMs.coerceIn(50, 1000).toLong(),
                        ((255 * userPreferences.screenshotHapticIntensity.coerceIn(0, 100)) / 100)
                            .coerceIn(1, 255)
                )
            )
        }

        snapshot.animate()
            .scaleX(SCREENSHOT_SHRINK_SCALE)
            .scaleY(SCREENSHOT_SHRINK_SCALE)
            .setDuration(SCREENSHOT_ANIMATION_DURATION_MS / 2)
            .setInterpolator(expressiveEffectsInterpolator)
            .withEndAction {
                snapshot.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(SCREENSHOT_ANIMATION_DURATION_MS / 2)
                    .setInterpolator(expressiveSpatialInterpolator)
                    .withEndAction {
                        contentFrame.removeView(whiteFilter)
                        contentFrame.removeView(snapshot)
                    }
                    .start()
            }
            .start()
        whiteFilter.animate()
            .alpha(0f)
            .setDuration(SCREENSHOT_ANIMATION_DURATION_MS)
            .setInterpolator(expressiveEffectsInterpolator)
            .start()
    }

    private fun saveScreenshot(bitmap: Bitmap) {
        val fileName = "OasisBrowser_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = android.content.ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/OasisBrowser")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create screenshot media entry")
            try {
                contentResolver.openOutputStream(uri)?.use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                } ?: error("Unable to open screenshot output")
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, values, null, null)
            } catch (error: Throwable) {
                contentResolver.delete(uri, null, null)
                throw error
            }
        } else {
            check(MediaStore.Images.Media.insertImage(contentResolver, bitmap, fileName, null) != null)
        }
    }

    /**
     * @see BrowserContract.View.showLocalFileBlockedDialog
     */
    override fun showLocalFileBlockedDialog() {
        BrowserDialog.showPositiveNegativeDialog(
            this,
            R.string.title_warning,
            R.string.message_blocked_local,
            positiveButton = DialogItem(title = R.string.action_allow) { presenter.onConfirmOpenLocalFile(true) },
            negativeButton = DialogItem(title = R.string.action_dont_allow) { presenter.onConfirmOpenLocalFile(false) },
            onCancel = { presenter.onConfirmOpenLocalFile(false) }
        )
    }

    /**
     * @see BrowserContract.View.showFileChooser
     */
    override fun showFileChooser(intent: Intent) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        fileChooserLauncher.launch(Intent.createChooser(intent, getString(R.string.title_file_chooser)))
    }

    /**
     * @see BrowserContract.View.showCustomView
     */
    @OptIn(markerClass = [UnstableApi::class])
    override fun showCustomView(view: View) {
        customView?.let(binding.root::removeView)
        customView = view
        customViewOriginalOrientation = requestedOrientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        view.setBackgroundColor(color(android.R.color.black))
        binding.root.addView(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.root.bringChildToFront(view)
        binding.uiLayout.isVisible = false
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        view.requestLayout()
        if (view is AntaresMediaPlayerView) {
            // Media3 starts with controls visible. Once playback actually begins the player asks
            // us to enter immersive mode; pausing restores the title and system bars.
            view.onPlaybackActiveChanged = ::setCustomViewPlaybackActive
            setCustomViewPlaybackActive(false)
        } else {
            setCustomViewPlaybackActive(true)
        }
    }

    /**
     * @see BrowserContract.View.hideCustomView
     */
    @OptIn(markerClass = [UnstableApi::class])
    override fun hideCustomView() {
        (customView as? AntaresMediaPlayerView)?.onPlaybackActiveChanged = null
        setCustomViewPlaybackActive(false)
        customView?.let(binding.root::removeView)
        customView = null
        binding.uiLayout.isVisible = true
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        requestedOrientation = customViewOriginalOrientation
    }

    private fun setCustomViewPlaybackActive(active: Boolean) {
        customViewHidSystemUi = active
        if (::systemBarsController.isInitialized) {
            systemBarsController.setImmersiveHidden(immersiveFullscreen || active)
        }
    }

    /**
     * @see BrowserContract.View.clearSearchFocus
     */
    override fun clearSearchFocus() {
        binding.search.clearFocus()
    }

    override fun launchQrScanner() {
        qrScannerLauncher.launch(Intent(this, QrScannerActivity::class.java))
    }

    private fun installUrlRailGestures() {
        val rail = binding.verticalUrlText?.parent as? View ?: return
        var downY = 0f
        var downX = 0f
        var dragProgress = 0f
        var gestureMoved = false
        var pendingTapView: View? = null
        var fallbackPerformedClick = false
        val interruptedTapFallback = Runnable {
            val target = pendingTapView ?: return@Runnable
            pendingTapView = null
            fallbackPerformedClick = true
            target.performClick()
        }
        fun cancelInterruptedTapFallback() {
            mainHandler.removeCallbacks(interruptedTapFallback)
            pendingTapView = null
        }
        val listener = View.OnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    cancelInterruptedTapFallback()
                    downY = event.rawY
                    downX = event.rawX
                    dragProgress = 0f
                    gestureMoved = false
                    fallbackPerformedClick = false
                    pendingTapView = view
                    mainHandler.postDelayed(
                        interruptedTapFallback,
                        URL_RAIL_INTERRUPTED_TAP_FALLBACK_MS,
                    )
                    urlRailTransition = null
                    tabPager.cancelVerticalTabSwitch()
                    rail.animate().cancel()
                    rail.animate()
                        .scaleX(0.96f)
                        .scaleY(0.96f)
                        .setDuration(120L)
                        .setInterpolator(expressiveEffectsInterpolator)
                        .start()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dy = event.rawY - downY
                    val dx = event.rawX - downX
                    if (abs(dy) > URL_RAIL_DRAG_START_DP.dp &&
                        abs(dy) > abs(dx) * 1.15f
                    ) {
                        gestureMoved = true
                        cancelInterruptedTapFallback()
                        val direction = if (dy < 0f) 1 else -1
                        val transition = urlRailTransition
                            ?.takeIf { it.direction == direction }
                            ?: presenter.previewUrlBarSwipeTab(direction)
                        if (transition != null) {
                            urlRailTransition = transition
                            dragProgress = (abs(dy) / URL_RAIL_SWIPE_THRESHOLD_DP.dp.toFloat())
                                .coerceIn(0f, 0.98f)
                            tabPager.previewVerticalTabSwitch(
                                currentId = transition.currentId,
                                targetId = transition.targetId,
                                direction = direction,
                                progress = dragProgress
                            )
                            continueRailHaptic(dragProgress)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val dy = event.rawY - downY
                    val dx = event.rawX - downX
                    cancelInterruptedTapFallback()
                    rail.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(180L)
                        .setInterpolator(expressiveSpatialInterpolator)
                        .start()
                    val transition = urlRailTransition
                    if (transition != null && dragProgress >= URL_RAIL_COMMIT_PROGRESS) {
                        stopContinuousRailHaptic()
                        animateUrlRailTabSwitch(rail, transition.direction)
                        fadePixelHaptic()
                        tabPager.commitVerticalTabSwitch(
                            targetId = transition.targetId,
                            direction = transition.direction
                        ) {
                            presenter.commitUrlBarSwipeTab(transition.targetId)
                        }
                    } else if (abs(dy) > URL_RAIL_SWIPE_THRESHOLD_DP.dp &&
                        abs(dy) > abs(dx) * 1.2f
                    ) {
                        val direction = if (dy < 0f) 1 else -1
                        val quickTransition = presenter.previewUrlBarSwipeTab(direction)
                        if (quickTransition != null) {
                            stopContinuousRailHaptic()
                            animateUrlRailTabSwitch(rail, direction)
                            fadePixelHaptic()
                            tabPager.previewVerticalTabSwitch(
                                currentId = quickTransition.currentId,
                                targetId = quickTransition.targetId,
                                direction = direction,
                                progress = 0.2f
                            )
                            tabPager.commitVerticalTabSwitch(
                                targetId = quickTransition.targetId,
                                direction = direction
                            ) {
                                presenter.commitUrlBarSwipeTab(quickTransition.targetId)
                            }
                        }
                    } else {
                        tabPager.cancelVerticalTabSwitch()
                        if (!fallbackPerformedClick) view.performClick()
                    }
                    urlRailTransition = null
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    stopContinuousRailHaptic()
                    // A remote SurfaceControl input transfer can cancel a stationary host tap.
                    // Leave its short fallback armed. A genuine drag already cancelled it above.
                    if (gestureMoved) cancelInterruptedTapFallback()
                    tabPager.cancelVerticalTabSwitch()
                    urlRailTransition = null
                    rail.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150L)
                        .setInterpolator(expressiveSpatialInterpolator)
                        .start()
                    true
                }

                else -> true
            }
        }
        rail.setOnTouchListener(listener)
        binding.verticalUrlText?.setOnTouchListener(listener)
    }

    private fun animateUrlRailTabSwitch(view: View, direction: Int) {
        val distance = 10.dp.toFloat() * direction
        view.animate().cancel()
        view.translationY = -distance
        view.alpha = 0.72f
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(240L)
            .setInterpolator(expressiveSpatialInterpolator)
            .start()
    }

    private fun continueRailHaptic(progress: Float) {
        if (!userPreferences.hapticsEnabled || !userPreferences.railHapticsEnabled) return
        railHapticLastMovementAt = System.currentTimeMillis()
        if (!railHapticActive) {
            val progressCurve = if (userPreferences.railHapticCurve == 1) {
                val eased = progress.coerceIn(0f, 1f)
                eased * eased * (3f - 2f * eased)
            } else {
                progress.coerceIn(0f, 1f)
            }
            val intensity = userPreferences.railHapticsIntensity.coerceIn(0, 100) / 100f
            val amplitude = ((32 + (progressCurve * 112f)) * intensity)
                .roundToInt().coerceIn(1, 180)
            vibrator?.takeIf { it.hasVibrator() }?.vibrate(
                VibrationEffect.createOneShot(RAIL_HAPTIC_MAX_DURATION_MS, amplitude)
            )
            railHapticActive = true
        }
        mainHandler.removeCallbacks(railHapticStopRunnable)
        mainHandler.postDelayed(railHapticStopRunnable, RAIL_HAPTIC_IDLE_TIMEOUT_MS)
    }

    private fun stopContinuousRailHaptic() {
        mainHandler.removeCallbacks(railHapticStopRunnable)
        if (railHapticActive) {
            vibrator?.cancel()
            railHapticActive = false
        }
    }

    private fun fadePixelHaptic() {
        if (!userPreferences.hapticsEnabled ||
            !userPreferences.railHapticsEnabled ||
            !userPreferences.railCompletionHapticsEnabled
        ) return
        val intensity = userPreferences.railCompletionHapticsIntensity.coerceIn(0, 100) / 100f
        vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0L, 18L, 32L, 14L, 38L, 8L),
                intArrayOf(
                    (145 * intensity).roundToInt(),
                    0,
                    (88 * intensity).roundToInt(),
                    0,
                    (34 * intensity).roundToInt(),
                    0
                ),
                -1
            )
        )
    }

    override fun renderState(viewState: BrowserViewState) {
        renderState(viewState.asPartial())
    }

    fun renderState(viewState: PartialBrowserViewState) {
        viewState.displayUrl?.let { displayUrl ->
            if (!binding.search.hasFocus()) {
                binding.search.setText(buildUrlSpannable(displayUrl, R.attr.autoCompleteTitleColor, R.attr.autoCompleteUrlColor))
            }
            binding.verticalUrlText?.text = buildUrlSpannable(displayUrl, R.attr.colorOnSurface, R.attr.colorOnSurfaceVariant)
        }
        viewState.progress?.let {
            binding.progressView.progress = it
            binding.progressView.isVisible = it in 1..99
        }
        viewState.isRefresh?.let {
            binding.settingsButton?.setImageResource(
                if (it) R.drawable.ic_action_refresh else R.drawable.ic_action_delete
            )
            if (uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser) {
                configureSearchRefreshOrUtilityButton()
            } else {
                binding.searchRefresh.setImageResource(
                    if (it) R.drawable.ic_action_refresh else R.drawable.ic_action_delete
                )
            }
        }
        viewState.isBackEnabled?.let { binding.actionBack.isEnabled = it }
        viewState.isForwardEnabled?.let { binding.actionForward.isEnabled = it }
        viewState.isBookmarked?.let {
            binding.actionAddBookmark.isSelected = it
        }
        viewState.sslState?.let {
            binding.searchSslStatus.setImageDrawable(createSslDrawableForState(it))
            binding.searchSslStatus.updateVisibilityForDrawable()
        }
        viewState.bookmarks?.let(::updateBookmarkList)
        viewState.findInPage?.let { query ->
            val shouldShowFind = query.isNotEmpty() || binding.findBar.isVisible && binding.findQuery.hasFocus()
            binding.findBar.isVisible = shouldShowFind
            if (binding.findQuery.text.toString() != query) {
                binding.findQuery.setText(query)
            }
            if (!shouldShowFind) {
                binding.findQuery.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(binding.findQuery.windowToken, 0)
            }
        }
        val suggestionsAdapter = binding.search.adapter as? SuggestionsAdapter
        suggestionsAdapter?.refreshBookmarks()
    }

    private fun updateBookmarkList(bookmarks: List<Bookmark>) {
        currentBookmarks = bookmarks
        val query = bookmarkQuery.trim().lowercase()
        bookmarksAdapter.submitList(
            if (query.isBlank()) bookmarks else bookmarks.filter {
                it.title.lowercase().contains(query) || it.url.lowercase().contains(query)
            }
        )
    }

    override fun renderTabs(tabs: List<TabListItem>) {
        tabsAdapter.submitList(tabs)
        if (uiConfiguration.tabConfiguration == TabConfiguration.DRAWER_BOTTOM ||
            uiConfiguration.tabConfiguration == TabConfiguration.OasisBrowser) {
            binding.tabCountView.updateTabCount(tabs.count { it is TabListItem.TabItem })
        }
    }

    private fun buildUrlSpannable(
        displayUrl: String,
        @androidx.annotation.AttrRes domainColorAttr: Int,
        @androidx.annotation.AttrRes mutedColorAttr: Int,
    ): CharSequence {
        if (displayUrl.isBlank()) return displayUrl

        val uri = Uri.parse(displayUrl)
        val authority = uri.authority ?: return displayUrl
        val schemeEnd = displayUrl.indexOf("://").takeIf { it != -1 }?.plus(3) ?: 0
        val start = displayUrl.indexOf(authority, schemeEnd)
        if (start == -1) return displayUrl
        val end = start + authority.length

        val spannable = SpannableString(displayUrl)
        val mutedColor = MaterialColors.getColor(this, mutedColorAttr, 0)
        spannable.setSpan(
            ForegroundColorSpan(mutedColor),
            0,
            displayUrl.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val domainColor = MaterialColors.getColor(this, domainColorAttr, 0)
        spannable.setSpan(
            ForegroundColorSpan(domainColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun showSearchEnginePopup() {
        val popup = PopupMenu(this, binding.search)
        val menu = popup.menu

        clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
            ?.takeIf { it.isValidWebUrl() }
            ?.let { url ->
                menu.add(0, Menu.NONE, Menu.NONE, getString(R.string.paste_and_go))
                    .setOnMenuItemClickListener {
                        presenter.onSearch(url)
                        true
                    }
            }

        val engines = searchEngineProvider.provideAllSearchEngines()
        val currentChoice = userPreferences.searchChoice
        engines.forEachIndexed { index, engine ->
            val title = getString(R.string.search_with, getString(engine.titleRes))
            val item = menu.add(0, Menu.NONE, Menu.NONE, title)
            item.isCheckable = true
            item.isChecked = index == currentChoice
            item.setOnMenuItemClickListener {
                userPreferences.searchChoice = index
                val query = binding.search.text.toString()
                if (query.isNotBlank()) {
                    presenter.onSearch(query)
                } else {
                    snackbar(getString(R.string.search_engine_set, getString(engine.titleRes)))
                }
                true
            }
        }
        popup.show()
    }

    private fun String.isValidWebUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

    override fun showAddBookmarkDialog(title: String, url: String, folders: List<String>) {
        oasisbrowserDialogBuilder.showAddBookmarkDialog(this, title, url, folders, presenter::onBookmarkConfirmed)
    }

    override fun showBookmarkOptionsDialog(bookmark: Bookmark.Entry) {
        BrowserDialog.show(
            this, R.string.dialog_bookmark,
            DialogItem(title = R.string.dialog_open_new_tab) {
                presenter.onBookmarkOptionClick(bookmark, BrowserContract.BookmarkOptionEvent.NEW_TAB)
            },
            DialogItem(title = R.string.dialog_open_background_tab) {
                presenter.onBookmarkOptionClick(bookmark, BrowserContract.BookmarkOptionEvent.BACKGROUND_TAB)
            },
            DialogItem(title = R.string.dialog_open_incognito_tab, isConditionMet = !isIncognito()) {
                presenter.onBookmarkOptionClick(bookmark, BrowserContract.BookmarkOptionEvent.INCOGNITO_TAB)
            },
            DialogItem(title = R.string.action_share) {
                presenter.onBookmarkOptionClick(bookmark, BrowserContract.BookmarkOptionEvent.SHARE)
            },
            DialogItem(title = R.string.action_copy) {
                presenter.onBookmarkOptionClick(bookmark, BrowserContract.BookmarkOptionEvent.COPY_LINK)
            },
            DialogItem(title = R.string.dialog_remove_bookmark) {
                presenter.onBookmarkOptionClick(bookmark, BrowserContract.BookmarkOptionEvent.REMOVE)
            },
            DialogItem(title = R.string.action_edit) {
                presenter.onBookmarkOptionClick(bookmark, BrowserContract.BookmarkOptionEvent.EDIT)
            }
        )
    }

    override fun showEditBookmarkDialog(title: String, url: String, folder: String, folders: List<String>) {
        oasisbrowserDialogBuilder.showEditBookmarkDialog(this, title, url, folder, folders, presenter::onBookmarkEditConfirmed)
    }

    override fun showFolderOptionsDialog(folder: Bookmark.Folder) {
        BrowserDialog.show(
            this, R.string.dialog_folder,
            DialogItem(title = R.string.action_rename) {
                presenter.onFolderOptionClick(folder, BrowserContract.FolderOptionEvent.RENAME)
            },
            DialogItem(title = R.string.dialog_remove_folder) {
                presenter.onFolderOptionClick(folder, BrowserContract.FolderOptionEvent.REMOVE)
            }
        )
    }

    override fun showEditFolderDialog(title: String) {
        BrowserDialog.showEditText(
            this, R.string.title_rename_folder, R.string.hint_title, title, R.string.action_ok
        ) { presenter.onBookmarkFolderRenameConfirmed(title, it) }
    }

    override fun showDownloadOptionsDialog(download: DownloadEntry) {
        BrowserDialog.show(
            this, download.title,
            DialogItem(title = R.string.action_donate) {
                openDonationPage()
            },
            DialogItem(title = R.string.dialog_delete_download) {
                presenter.onDownloadOptionClick(download, BrowserContract.DownloadOptionEvent.DELETE)
            },
            DialogItem(title = R.string.dialog_delete_all_downloads) {
                presenter.onDownloadOptionClick(download, BrowserContract.DownloadOptionEvent.DELETE_ALL)
            }
        )
    }

    private fun maybeShowFirstRunDonationDialog() {
        val preferences = getSharedPreferences(DonationPromptPreferences.FILE_NAME, MODE_PRIVATE)
        if (preferences.getBoolean(DonationPromptPreferences.KEY_SHOWN, false) || isIncognito()) {
            return
        }

        preferences.edit { putBoolean(DonationPromptPreferences.KEY_SHOWN, true) }
        mainHandler.post {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.donation_prompt_title)
                .setMessage(R.string.donation_prompt_message)
                .setPositiveButton(R.string.action_donate) { _, _ -> openDonationPage() }
                .setNegativeButton(R.string.action_not_now, null)
                .resizeAndShow()
        }
    }

    private fun openDonationPage() {
        startActivity(Intent(Intent.ACTION_VIEW, KO_FI_URL.toUri()))
    }

    override fun showHistoryOptionsDialog(historyEntry: HistoryEntry) {
        BrowserDialog.show(
            this, R.string.dialog_history_long_press,
            DialogItem(title = R.string.dialog_open_new_tab) {
                presenter.onHistoryOptionClick(historyEntry, BrowserContract.HistoryOptionEvent.NEW_TAB)
            },
            DialogItem(title = R.string.dialog_open_background_tab) {
                presenter.onHistoryOptionClick(historyEntry, BrowserContract.HistoryOptionEvent.BACKGROUND_TAB)
            },
            DialogItem(title = R.string.dialog_open_incognito_tab, isConditionMet = !isIncognito()) {
                presenter.onHistoryOptionClick(historyEntry, BrowserContract.HistoryOptionEvent.INCOGNITO_TAB)
            },
            DialogItem(title = R.string.action_share) {
                presenter.onHistoryOptionClick(historyEntry, BrowserContract.HistoryOptionEvent.SHARE)
            },
            DialogItem(title = R.string.action_copy) {
                presenter.onHistoryOptionClick(historyEntry, BrowserContract.HistoryOptionEvent.COPY_LINK)
            },
            DialogItem(title = R.string.dialog_remove_from_history) {
                presenter.onHistoryOptionClick(historyEntry, BrowserContract.HistoryOptionEvent.REMOVE)
            }
        )
    }

    override fun showFindInPageDialog() {
        binding.findBar.isVisible = true
        binding.findQuery.requestFocus()
        binding.findQuery.setSelection(binding.findQuery.text.length)
        inputMethodManager.showSoftInput(binding.findQuery, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun showFindResult(activeMatch: Int, totalMatches: Int) {
        findCountView?.apply {
            text = if (totalMatches > 0) {
                getString(R.string.find_result_count, activeMatch, totalMatches)
            } else {
                getString(R.string.find_no_matches)
            }
            isVisible = true
        }
    }

    private fun initFindInPageUi() {
        val countView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                marginStart = 8.dp
                marginEnd = 8.dp
            }
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, 0, 8.dp, 0)
            setTextColor(MaterialColors.getColor(this@BrowserActivity, R.attr.colorOnSurfaceVariant, 0))
            textSize = 14f
            isVisible = false
            contentDescription = getString(R.string.find_result_count, 0, 0)
        }
        findCountView = countView
        binding.findBar.addView(countView, 1)
    }

    override fun showReaderView(html: String, title: String) {
        if (readerView == null) {
            readerView = ReaderView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onCloseClick = { presenter.onExitReaderMode() }
                onTtsClick = { presenter.onReaderTts() }
            }
            binding.contentFrame.addView(readerView)
        }
        readerView?.apply {
            loadHtml(html)
            isVisible = true
            bringToFront()
        }
    }

    override fun hideReaderView() {
        readerView?.isVisible = false
    }

    override fun speakPageText(text: String) {
        if (text.isBlank()) {
            snackbar(R.string.text_to_speech_no_text)
            return
        }
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
            pendingSpeechText = null
            snackbar(R.string.text_to_speech_stopped)
            return
        }
        textToSpeech?.let { tts ->
            if (textToSpeechReady) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    @Suppress("DEPRECATION")
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)
                }
            } else {
                pendingSpeechText = text
            }
        } ?: run {
            if (textToSpeechReady) {
                pendingSpeechText = null
                enqueueSpeech(text)
            }
        }
    }

    private fun enqueueSpeech(text: String) {
        val engine = textToSpeech ?: return
        val chunks = text
            .split(Regex("(?<=[.!?])\\s+"))
            .fold(mutableListOf<String>()) { result, sentence ->
                var remaining = sentence.trim()
                while (remaining.length > TTS_CHUNK_LENGTH) {
                    result += remaining.take(TTS_CHUNK_LENGTH)
                    remaining = remaining.drop(TTS_CHUNK_LENGTH).trimStart()
                }
                if (remaining.isNotBlank()) {
                    val current = result.lastOrNull().orEmpty()
                    if (current.length + remaining.length + 1 <= TTS_CHUNK_LENGTH) {
                        if (result.isEmpty()) result += remaining
                        else result[result.lastIndex] = "$current $remaining"
                    } else {
                        result += remaining
                    }
                }
                result
            }
        chunks.forEachIndexed { index, chunk ->
            engine.speak(
                chunk,
                if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                null,
                "OasisBrowser-read-aloud-$index"
            )
        }
        snackbar(R.string.text_to_speech_started)
    }

    override fun showLinkLongPressDialog(longPress: LongPress) {
        BrowserDialog.show(
            this, longPress.targetUrl,
            DialogItem(title = R.string.dialog_open_new_tab) {
                presenter.onLinkLongPressEvent(longPress, BrowserContract.LinkLongPressEvent.NEW_TAB)
            },
            DialogItem(title = R.string.dialog_open_background_tab) {
                presenter.onLinkLongPressEvent(longPress, BrowserContract.LinkLongPressEvent.BACKGROUND_TAB)
            },
            DialogItem(title = R.string.dialog_open_incognito_tab, isConditionMet = !isIncognito()) {
                presenter.onLinkLongPressEvent(longPress, BrowserContract.LinkLongPressEvent.INCOGNITO_TAB)
            },
            DialogItem(title = R.string.action_share) {
                presenter.onLinkLongPressEvent(longPress, BrowserContract.LinkLongPressEvent.SHARE)
            },
            DialogItem(title = R.string.dialog_copy_link) {
                presenter.onLinkLongPressEvent(longPress, BrowserContract.LinkLongPressEvent.COPY_LINK)
            }
        )
    }

    override fun showImageLongPressDialog(longPress: LongPress) {
        BrowserDialog.show(
            this, longPress.targetUrl,
            DialogItem(title = R.string.dialog_open_new_tab) {
                presenter.onImageLongPressEvent(
                    longPress,
                    BrowserContract.ImageLongPressEvent.NEW_TAB
                )
            },
            DialogItem(title = R.string.dialog_open_background_tab) {
                presenter.onImageLongPressEvent(
                    longPress,
                    BrowserContract.ImageLongPressEvent.BACKGROUND_TAB
                )
            },
            DialogItem(
                title = R.string.dialog_open_incognito_tab,
                isConditionMet = !isIncognito()
            ) {
                presenter.onImageLongPressEvent(
                    longPress,
                    BrowserContract.ImageLongPressEvent.INCOGNITO_TAB
                )
            },
            DialogItem(title = R.string.action_share) {
                presenter.onImageLongPressEvent(
                    longPress,
                    BrowserContract.ImageLongPressEvent.SHARE
                )
            },
            DialogItem(title = R.string.dialog_copy_link) {
                presenter.onImageLongPressEvent(
                    longPress,
                    BrowserContract.ImageLongPressEvent.COPY_LINK
                )
            },
            DialogItem(title = R.string.dialog_download_image) {
                presenter.onImageLongPressEvent(
                    longPress,
                    BrowserContract.ImageLongPressEvent.DOWNLOAD
                )
            })
    }

    /**
     * @see BrowserContract.View.showTabGroupDialog
     */
    override fun showTabGroupDialog(tabId: Int, groups: List<TabGroup>) {
        val items = mutableListOf<CharSequence>()
        val actions = mutableListOf<() -> Unit>()

        items += getString(R.string.tab_group_new)
        actions += { showCreateTabGroupDialog(tabId) }

        groups.forEach { group ->
            items += getString(R.string.tab_group_add_to, group.name)
            actions += { presenter.onTabGroupAddTab(tabId, group.id) }
        }

        items += getString(R.string.tab_group_remove)
        actions += { presenter.onTabGroupRemoveTab(tabId) }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.tab_group_dialog_title)
            .setItems(items.toTypedArray<CharSequence>()) { _, which ->
                actions[which].invoke()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showCreateTabGroupDialog(tabId: Int) {
        val input = android.widget.EditText(this)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.tab_group_new)
            .setView(input)
            .setPositiveButton(R.string.action_ok) { _, _ ->
                val name = input.text?.toString()?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.tab_group_default_name)
                presenter.onTabGroupCreate(name, tabId)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    /**
     * @see BrowserContract.View.showCloseBrowserDialog
     */
    override fun showCloseBrowserDialog(id: Int) {
        BrowserDialog.show(
            this,
            getString(
                R.string.dialog_title_tab_management,
                tabPager.estimatedMemoryForTab(id)
            ),
            DialogItem(title = R.string.close_tab) {
                presenter.onCloseBrowserEvent(id, BrowserContract.CloseTabEvent.CLOSE_CURRENT)
            },
            DialogItem(title = R.string.close_other_tabs) {
                presenter.onCloseBrowserEvent(id, BrowserContract.CloseTabEvent.CLOSE_OTHERS)
            },
            DialogItem(title = R.string.close_all_tabs, onClick = {
                presenter.onCloseBrowserEvent(id, BrowserContract.CloseTabEvent.CLOSE_ALL)
            })
        )
    }

    override fun showSslDialog(sslCertificateInfo: SslCertificateInfo) {
        showSslCertificateDialog(sslCertificateInfo)
    }

    fun clearAllHistoryFromHistoryPage() {
        presenter.onClearAllHistoryClick()
    }

    fun clearAllDownloadsFromDownloadsPage() {
        presenter.onClearAllDownloadsClick()
        snackbar(R.string.downloads_history_cleared)
    }

    fun clearAllReadingListFromReadingListPage() {
        presenter.onClearAllReadingListClick()
    }

    fun showDownloadDecoyModePrompt() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.download_decoy_mode_title)
            .setMessage(R.string.download_decoy_mode_message)
            .setPositiveButton(R.string.download_decoy_mode_start) { _, _ ->
                presenter.onDownloadDecoyModeConfirmed()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    fun showHistoryDecoyModePrompt() {
        val options = arrayOf(
            getString(R.string.history_decoy_mode_4_hours),
            getString(R.string.history_decoy_mode_48_hours),
            getString(R.string.history_decoy_mode_all_time)
        )
        var selectedIndex = 0
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.history_decoy_mode_title)
            // AlertDialog uses the single-choice list as its content. Do not
            // also set a message here, otherwise the message panel can take
            // precedence over the list on some Material dialog themes.
            .setSingleChoiceItems(options, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton(R.string.history_decoy_mode_start) { _, _ ->
                val timeframe = when (selectedIndex) {
                    1 -> DecoyTimeframe.FORTY_EIGHT_HOURS
                    2 -> DecoyTimeframe.ALL_TIME
                    else -> DecoyTimeframe.FOUR_HOURS
                }
                presenter.onHistoryDecoyModeConfirmed(timeframe)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showAddressOverlay() {
        val overlay = binding.addressOverlay ?: return
        if (addressOverlayOpen && overlay.isVisible) {
            val generation = ++addressOverlayGeneration
            presenter.onAddressOverlayMoved(true) {
                requestAddressEditorFocus(generation, overlay)
            }
            return
        }
        addressOverlayOpen = true
        val generation = ++addressOverlayGeneration
        val searchContainer = binding.searchContainer
        val editUrl = presenter.currentUrlForEditing()
        overlay.animate().cancel()
        searchContainer.animate().cancel()
        if (!overlay.isVisible) {
            overlay.alpha = 0f
            overlay.translationY = -overlay.height.coerceAtLeast(72.dp).toFloat()
            overlay.scaleX = 0.98f
            overlay.scaleY = 0.98f
            searchContainer.elevation = 0f
            searchContainer.translationZ = 0f
            overlay.isVisible = true
        }
        binding.root.findViewById<ImageView?>(R.id.search_engine_icon)?.let { icon ->
            icon.isVisible = true
            icon.setOnClickListener { showSearchEnginePicker() }
        }
        if (binding.search.text.toString() != editUrl) {
            binding.search.setText(editUrl)
            binding.search.setSelection(editUrl.length)
        }
        overlay.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(ADDRESS_OVERLAY_ENTER_DURATION_MS)
            .setInterpolator(expressiveSpatialInterpolator)
            .start()
        searchContainer.animate()
            .translationZ(18.dp.toFloat())
            .setStartDelay(ADDRESS_OVERLAY_SHADOW_DELAY_MS)
            .setDuration(ADDRESS_OVERLAY_SHADOW_DURATION_MS)
            .setInterpolator(expressiveSpatialInterpolator)
            .withEndAction {
                searchContainer.elevation = 18.dp.toFloat()
            }
            .start()
        // Antares acknowledges that its remote main thread has relinquished input before the
        // local editor becomes the IME client. WebView tabs acknowledge immediately.
        presenter.onAddressOverlayMoved(true) {
            requestAddressEditorFocus(generation, overlay)
        }
    }

    private fun showSearchEnginePicker() {
        val engines = searchEngineProvider.provideAllSearchEngines()
        val items = engines.map { getString(it.titleRes) }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.search_engine)
            .setSingleChoiceItems(items, userPreferences.searchChoice) { dialog, which ->
                userPreferences.searchChoice = which
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun requestAddressEditorFocus(generation: Int, overlay: View, attempt: Int = 0) {
        binding.search.post {
            if (!addressOverlayOpen || generation != addressOverlayGeneration ||
                !overlay.isVisible
            ) {
                return@post
            }
            val search = binding.search
            if (!search.hasFocus() && !search.requestFocus()) {
                scheduleAddressEditorImeRetry(generation, overlay, attempt)
                return@post
            }
            if (!search.isAttachedToWindow || !search.hasWindowFocus()) {
                scheduleAddressEditorImeRetry(generation, overlay, attempt)
                return@post
            }

            // The editor can gain View focus before Android finishes moving the active input
            // connection away from Antares' remote SurfaceControl hierarchy. Request through both
            // supported APIs, then verify the IME inset and retry briefly if system_server has not
            // accepted the new editor yet. Flags are deliberately zero: SHOW_IMPLICIT is deprecated
            // on Android 17 and is weaker than this explicit user-initiated request.
            WindowCompat.getInsetsController(window, search)
                .show(WindowInsetsCompat.Type.ime())
            inputMethodManager.showSoftInput(search, 0)
            val imeVisible = ViewCompat.getRootWindowInsets(search)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            if (!imeVisible) {
                scheduleAddressEditorImeRetry(generation, overlay, attempt)
            }
        }
    }

    private fun scheduleAddressEditorImeRetry(generation: Int, overlay: View, attempt: Int) {
        if (attempt >= ADDRESS_EDITOR_IME_MAX_RETRIES) return
        mainHandler.postDelayed(
            {
                requestAddressEditorFocus(
                    generation = generation,
                    overlay = overlay,
                    attempt = attempt + 1,
                )
            },
            ADDRESS_EDITOR_IME_RETRY_DELAY_MS,
        )
    }

    private fun hideAddressOverlay() {
        val overlay = binding.addressOverlay ?: return
        if (!addressOverlayOpen && !overlay.isVisible) return
        addressOverlayOpen = false
        val generation = ++addressOverlayGeneration
        val searchContainer = binding.searchContainer
        binding.search.clearFocus()
        WindowCompat.getInsetsController(window, binding.search)
            .hide(WindowInsetsCompat.Type.ime())
        overlay.animate().cancel()
        searchContainer.animate().cancel()
        searchContainer.animate()
            .translationZ(0f)
            .setDuration(ADDRESS_OVERLAY_EXIT_DURATION_MS)
            .setInterpolator(expressiveEffectsInterpolator)
            .withEndAction {
                searchContainer.elevation = 0f
            }
            .start()
        overlay.animate()
            .alpha(0f)
            .translationY(-overlay.height.coerceAtLeast(72.dp).toFloat() * 0.35f)
            .scaleX(0.98f)
            .scaleY(0.98f)
            .setDuration(ADDRESS_OVERLAY_EXIT_DURATION_MS)
            .setInterpolator(expressiveEffectsInterpolator)
            .withEndAction {
                finishAddressOverlayHide(generation)
            }
            .start()
        // ViewPropertyAnimator end actions are skipped when an animation is cancelled. Keep the
        // input state correct even if a lifecycle or a second chrome action interrupts the fade.
        mainHandler.postDelayed(
            { finishAddressOverlayHide(generation) },
            ADDRESS_OVERLAY_EXIT_DURATION_MS + ADDRESS_OVERLAY_HIDE_FALLBACK_DELAY_MS,
        )
    }

    private fun finishAddressOverlayHide(generation: Int) {
        if (addressOverlayOpen || generation != addressOverlayGeneration) return
        val overlay = binding.addressOverlay ?: return
        overlay.animate().cancel()
        overlay.isVisible = false
        overlay.translationY = 0f
        overlay.scaleX = 1f
        overlay.scaleY = 1f
        binding.searchContainer.translationZ = 0f
        binding.searchContainer.elevation = 0f
        presenter.onAddressOverlayMoved(false)
    }

    private fun ImageView.updateVisibilityForDrawable() {
        visibility = if (drawable == null) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            downloadPermissionsHelper.onRequestPermissionsResult(this, grantResults)
        }
    }
}

private const val HORIZONTAL_RAIL_ACTION_SIZE_DP = 48
private const val SUPER_COMPACT_RAIL_WIDTH_DP = 30
private const val MIN_OasisBrowser_RAIL_WIDTH_DP = SUPER_COMPACT_RAIL_WIDTH_DP
private const val MAX_OasisBrowser_RAIL_WIDTH_DP = 96
private const val URL_RAIL_DRAG_START_DP = 8
private const val URL_RAIL_SWIPE_THRESHOLD_DP = 34
private const val URL_RAIL_COMMIT_PROGRESS = 0.42f
private const val URL_RAIL_INTERRUPTED_TAP_FALLBACK_MS = 220L
private const val RAIL_HAPTIC_MAX_DURATION_MS = 10_000L
private const val RAIL_HAPTIC_IDLE_TIMEOUT_MS = 140L
private const val SCREENSHOT_ANIMATION_DURATION_MS = 650L
private const val SCREENSHOT_SHRINK_SCALE = 0.70f
private const val BROWSER_MENU_MAX_WIDTH_DP = 258
private const val BROWSER_MENU_SCREEN_MARGIN_DP = 14
private const val KO_FI_URL = "mailto:alzimerahmed84@gmail.com"
private const val ADDRESS_OVERLAY_ENTER_DURATION_MS = 360L
private const val ADDRESS_OVERLAY_EXIT_DURATION_MS = 180L
private const val ADDRESS_OVERLAY_HIDE_FALLBACK_DELAY_MS = 50L
private const val ADDRESS_OVERLAY_SHADOW_DELAY_MS = 90L
private const val ADDRESS_OVERLAY_SHADOW_DURATION_MS = 320L
private const val ADDRESS_EDITOR_IME_RETRY_DELAY_MS = 120L
private const val ADDRESS_EDITOR_IME_MAX_RETRIES = 6
private const val TTS_CHUNK_LENGTH = 3500
