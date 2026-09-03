package com.alzimerahmed.oasisbrowser.browser.tab

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alzimerahmed.oasisbrowser.browser.homepage.NativeHomepageView
import com.alzimerahmed.oasisbrowser.constant.SCHEME_HOMEPAGE
import com.alzimerahmed.oasisbrowser.preference.UserPreferences
import com.google.android.material.color.MaterialColors
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

/** Owns the mutually exclusive native and engine surfaces for one browser tab. */
class TabContentHost private constructor(
    activity: Activity,
    private val tab: TabModel,
    private val engineView: Lazy<View>,
    private val homepageFactory: NativeHomepageView.Factory,
    private val userPreferences: UserPreferences,
) : FrameLayout(activity) {
    private val disposables = CompositeDisposable()
    private var homepageView: NativeHomepageView? = null
    private var engineAttachedListener: ((View) -> Unit)? = null
    private var customViewActive = false

    private inner class ScrollableHost(context: Context) : FrameLayout(context) {
        var scrollableChild: View? = null
        override fun canScrollVertically(direction: Int): Boolean {
            return scrollableChild?.canScrollVertically(direction) ?: false
        }
    }

    private val contentWrapper = ScrollableHost(activity).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
    }

    private val swipeRefreshLayout = SwipeRefreshLayout(activity).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        setColorSchemeColors(
            MaterialColors.getColor(activity, com.alzimerahmed.oasisbrowser.R.attr.colorAccent, 0),
            MaterialColors.getColor(activity, com.alzimerahmed.oasisbrowser.R.attr.colorPrimary, 0),
            MaterialColors.getColor(activity, com.alzimerahmed.oasisbrowser.R.attr.colorTertiary, 0),
        )
        setProgressBackgroundColorSchemeColor(
            MaterialColors.getColor(activity, com.alzimerahmed.oasisbrowser.R.attr.colorSurfaceContainerHighest, 0),
        )
        setOnRefreshListener { tab.reload() }
        addView(contentWrapper)
    }

    init {
        addView(swipeRefreshLayout)
        updateSwipeState()
        // Render immediately so a newly selected tab cannot expose an empty host for one frame.
        render(tab.contentKind)
        disposables += tab.contentKindChanges()
            .skip(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::render)
        disposables += tab.urlChanges()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateSwipeState() }
        disposables += tab.loadingProgress()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { progress ->
                if (progress >= 100) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        disposables += tab.showCustomViewRequests()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                customViewActive = true
                updateSwipeState()
            }
        disposables += tab.hideCustomViewRequests()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                customViewActive = false
                updateSwipeState()
            }
    }

    fun setEngineAttachedListener(listener: (View) -> Unit) {
        engineAttachedListener = listener
        currentEngineView()?.let(listener)
    }

    fun currentEngineView(): View? = engineView.takeIf(Lazy<View>::isInitialized)?.value

    fun refreshHomepage() = homepageView?.refresh()

    fun dispose() {
        swipeRefreshLayout.isRefreshing = false
        disposables.dispose()
        homepageView?.dispose()
        removeAllViews()
        homepageView = null
    }

    private fun render(kind: TabContentKind) {
        when (kind) {
            TabContentKind.NATIVE_HOMEPAGE -> showHomepage()
            TabContentKind.ENGINE -> showEngine()
        }
    }

    private fun showHomepage() {
        tab.setContentVisible(false)
        currentEngineView()?.let(::removeIfAttached)
        contentWrapper.scrollableChild = null
        val homepage = homepageView ?: homepageFactory.create(context, tab::loadUrl).also {
            homepageView = it
        }
        attach(homepage)
        homepage.refresh()
        updateSwipeState()
    }

    private fun showEngine() {
        homepageView?.let(::removeIfAttached)
        val engine = engineView.value
        contentWrapper.scrollableChild = engine
        attach(engine)
        tab.setContentVisible(true)
        engineAttachedListener?.invoke(engine)
        updateSwipeState()
    }

    private fun updateSwipeState() {
        if (userPreferences.reducedMotionEnabled || customViewActive) {
            swipeRefreshLayout.isEnabled = false
            return
        }
        val refreshable = tab.contentKind == TabContentKind.ENGINE && isRefreshableUrl(tab.url)
        swipeRefreshLayout.isEnabled = refreshable
    }

    private fun isRefreshableUrl(url: String): Boolean =
        url.isNotBlank() &&
            !url.startsWith(SCHEME_HOMEPAGE) &&
            !url.startsWith("file:") &&
            !url.startsWith("about:") &&
            !url.startsWith("javascript:")

    private fun attach(view: View) {
        if (view.parent === contentWrapper) return
        (view.parent as? ViewGroup)?.removeView(view)
        contentWrapper.addView(
            view,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
        )
    }

    private fun removeIfAttached(view: View) {
        if (view.parent === contentWrapper) contentWrapper.removeView(view)
    }

    class Factory @Inject constructor(
        private val activity: Activity,
        private val homepageFactory: NativeHomepageView.Factory,
        private val userPreferences: UserPreferences,
    ) {
        fun create(tab: TabModel, engineView: Lazy<View>): TabContentHost =
            TabContentHost(activity, tab, engineView, homepageFactory, userPreferences)
    }
}
