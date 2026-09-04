package com.alzimerahmed.oasisbrowser.browser.tab.bundle

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.browser.di.DiskScheduler
import com.alzimerahmed.oasisbrowser.browser.tab.BookmarkPageInitializer
import com.alzimerahmed.oasisbrowser.browser.tab.DownloadPageInitializer
import com.alzimerahmed.oasisbrowser.browser.tab.FreezableBundleInitializer
import com.alzimerahmed.oasisbrowser.browser.tab.HistoryPageInitializer
import com.alzimerahmed.oasisbrowser.browser.tab.HomePageInitializer
import com.alzimerahmed.oasisbrowser.browser.tab.TabInitializer
import com.alzimerahmed.oasisbrowser.browser.tab.TabModel
import com.alzimerahmed.oasisbrowser.utils.FileUtils
import com.alzimerahmed.oasisbrowser.utils.isBookmarkUrl
import com.alzimerahmed.oasisbrowser.utils.isDownloadsUrl
import com.alzimerahmed.oasisbrowser.utils.isHistoryUrl
import com.alzimerahmed.oasisbrowser.utils.isSpecialUrl
import com.alzimerahmed.oasisbrowser.utils.isStartPageUrl
import android.app.Application
import android.os.Bundle
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Inject

/**
 * A bundle store that serializes each tab state to disk and supports its retrieval.
 */
class DefaultBundleStore @Inject constructor(
    private val application: Application,
    private val bookmarkPageInitializer: BookmarkPageInitializer,
    private val homePageInitializer: HomePageInitializer,
    private val downloadPageInitializer: DownloadPageInitializer,
    private val historyPageInitializer: HistoryPageInitializer,
    @DiskScheduler private val diskScheduler: Scheduler
) : BundleStore {

    override fun save(tabs: List<TabModel>) {
        val outState = Bundle(ClassLoader.getSystemClassLoader())

        tabs.withIndex().forEach { (index, tab) ->
            if (!tab.url.isSpecialUrl()) {
                outState.putBundle(BUNDLE_KEY + index, tab.freeze())
                outState.putString(TAB_TITLE_KEY + index, tab.title)
                outState.putInt(TAB_ID_KEY + index, tab.id)
            } else {
                outState.putBundle(BUNDLE_KEY + index, Bundle().apply {
                    putString(URL_KEY, tab.url)
                })
            }
        }

        FileUtils.writeBundleToStorage(application, outState, BUNDLE_STORAGE)
            .subscribeOn(diskScheduler)
            .subscribe(
                {},
                { e -> android.util.Log.e("DefaultBundleStore", "save failed", e) }
            )
    }

    override fun retrieve(): List<TabInitializer> =
        FileUtils.readBundleFromStorage(application, BUNDLE_STORAGE)?.let { bundle ->
            bundle.keySet()
                .filter { it.startsWith(BUNDLE_KEY) }
                .mapNotNull { bundleKey ->
                    bundle.getBundle(bundleKey)?.let {
                        Triple(
                            it,
                            bundle.getString(TAB_TITLE_KEY + bundleKey.extractNumberFromEnd()),
                            bundle.getInt(TAB_ID_KEY + bundleKey.extractNumberFromEnd(), -1)
                        )
                    }
                }
        }?.map { (bundle, title, id) ->
            return@map bundle.getString(URL_KEY)?.let { url ->
                when {
                    url.isBookmarkUrl() -> bookmarkPageInitializer
                    url.isDownloadsUrl() -> downloadPageInitializer
                    url.isStartPageUrl() -> homePageInitializer
                    url.isHistoryUrl() -> historyPageInitializer
                    else -> homePageInitializer
                }
            } ?: FreezableBundleInitializer(
                bundle = bundle,
                initialTitle = title ?: application.getString(R.string.tab_frozen),
                id = id
            )
        } ?: emptyList()

    override fun deleteAll() {
        FileUtils.deleteBundleInStorage(application, BUNDLE_STORAGE)
    }

    private fun String.extractNumberFromEnd(): String {
        val underScore = lastIndexOf('_')
        return if (underScore in indices) {
            substring(underScore + 1)
        } else {
            ""
        }
    }

    companion object {
        private const val BUNDLE_KEY = "WEBVIEW_"
        private const val TAB_TITLE_KEY = "TITLE_"
        private const val TAB_ID_KEY = "ID_"
        private const val URL_KEY = "URL_KEY"
        private const val BUNDLE_STORAGE = "SAVED_TABS.parcel"
    }
}
