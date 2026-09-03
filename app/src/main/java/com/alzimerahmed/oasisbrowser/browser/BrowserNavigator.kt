package com.alzimerahmed.oasisbrowser.browser

import com.alzimerahmed.oasisbrowser.IncognitoBrowserActivity
import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.browser.cleanup.ExitCleanup
import com.alzimerahmed.oasisbrowser.browser.di.IncognitoMode
import com.alzimerahmed.oasisbrowser.browser.download.DownloadPermissionsHelper
import com.alzimerahmed.oasisbrowser.browser.download.PendingDownload
import com.alzimerahmed.oasisbrowser.extensions.copyToClipboard
import com.alzimerahmed.oasisbrowser.extensions.snackbar
import com.alzimerahmed.oasisbrowser.log.Logger
import com.alzimerahmed.oasisbrowser.qr.QrShowActivity
import com.alzimerahmed.oasisbrowser.settings.activity.SettingsActivity
import com.alzimerahmed.oasisbrowser.settings.activity.SettingsNavigation
import com.alzimerahmed.oasisbrowser.utils.IntentUtils
import com.alzimerahmed.oasisbrowser.utils.UrlCleaner
import com.alzimerahmed.oasisbrowser.utils.Utils
import android.app.ActivityManager
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject

/**
 * The navigator implementation.
 */
class BrowserNavigator @Inject constructor(
    private val activity: FragmentActivity,
    private val clipboardManager: ClipboardManager,
    private val logger: Logger,
    private val downloadPermissionsHelper: DownloadPermissionsHelper,
    private val exitCleanup: ExitCleanup,
    @IncognitoMode private val incognitoMode: Boolean,
    private val activityManager: ActivityManager,
) : BrowserContract.Navigator {

    override fun openSettings() {
        activity.startActivity(
            Intent(activity, SettingsActivity::class.java).apply {
                putExtra(SettingsNavigation.EXTRA_INCOGNITO, incognitoMode)
            }
        )
    }

    override fun sharePage(url: String, title: String?) {
        IntentUtils(activity).shareUrl(url, title)
    }

    override fun copyPageLink(url: String) {
        clipboardManager.copyToClipboard(UrlCleaner.clean(url))
        activity.snackbar(R.string.message_link_copied)
    }

    override fun closeBrowser() {
        exitCleanup.cleanUp()
        if (incognitoMode) {
            activityManager.appTasks
                .first { it.taskInfo.topActivity?.className == IncognitoBrowserActivity::class.java.name }
                .finishAndRemoveTask()
        } else {
            activity.finish()
        }
    }

    override fun addToHomeScreen(url: String, title: String, favicon: Bitmap?) {
        Utils.createShortcut(activity, url, title, favicon)
        logger.log(TAG, "Creating shortcut")
    }

    override fun download(pendingDownload: PendingDownload) {
        downloadPermissionsHelper.download(
            activity = activity,
            url = pendingDownload.url,
            userAgent = pendingDownload.userAgent,
            contentDisposition = pendingDownload.contentDisposition,
            mimeType = pendingDownload.mimeType,
            contentLength = pendingDownload.contentLength,
            origin = pendingDownload.origin,
            blobData = pendingDownload.blobData
        )
    }

    override fun backgroundBrowser() {
        if (incognitoMode) {
            exitCleanup.cleanUp()
            activityManager.appTasks
                .first { it.taskInfo.topActivity?.className == IncognitoBrowserActivity::class.java.name }
                .finishAndRemoveTask()
        } else {
            activity.moveTaskToBack(true)
        }
    }

    override fun launchIncognito(url: String?) {
        IncognitoBrowserActivity.launch(activity, url)
    }

    override fun showQrCode(url: String) {
        val intent = Intent(activity, QrShowActivity::class.java).apply {
            putExtra(QrShowActivity.EXTRA_URL, url)
        }
        activity.startActivity(intent)
    }

    companion object {
        private const val TAG = "BrowserNavigator"
    }

}
