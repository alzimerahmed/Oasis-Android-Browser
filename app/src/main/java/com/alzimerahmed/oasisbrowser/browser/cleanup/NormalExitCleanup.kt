package com.alzimerahmed.oasisbrowser.browser.cleanup

import com.alzimerahmed.oasisbrowser.browser.di.DatabaseScheduler
import com.alzimerahmed.oasisbrowser.database.history.HistoryDatabase
import com.alzimerahmed.oasisbrowser.log.Logger
import com.alzimerahmed.oasisbrowser.preference.UserPreferences
import com.alzimerahmed.oasisbrowser.utils.WebUtils
import android.app.Activity
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Inject

/**
 * Exit cleanup that should run whenever the main browser process is exiting.
 */
class NormalExitCleanup @Inject constructor(
    private val userPreferences: UserPreferences,
    private val logger: Logger,
    private val historyDatabase: HistoryDatabase,
    @DatabaseScheduler private val databaseScheduler: Scheduler,
    private val activity: Activity
) : ExitCleanup {
    override fun cleanUp() {
        if (userPreferences.clearCacheExit) {
            WebUtils.clearCache(activity)
            logger.log(TAG, "Cache Cleared")
        }
        if (userPreferences.clearHistoryExitEnabled) {
            WebUtils.clearHistory(activity, historyDatabase, databaseScheduler)
            logger.log(TAG, "History Cleared")
        }
        if (userPreferences.clearCookiesExitEnabled) {
            WebUtils.clearCookies()
            logger.log(TAG, "Cookies Cleared")
        }
        if (userPreferences.clearWebStorageExitEnabled) {
            WebUtils.clearWebStorage()
            logger.log(TAG, "WebStorage Cleared")
        }
    }

    companion object {
        const val TAG = "NormalExitCleanup"
    }
}
