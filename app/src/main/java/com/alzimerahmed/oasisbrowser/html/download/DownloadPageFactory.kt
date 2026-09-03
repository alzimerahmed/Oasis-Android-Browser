package com.alzimerahmed.oasisbrowser.html.download

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.browser.theme.ThemeProvider
import com.alzimerahmed.oasisbrowser.constant.FILE
import com.alzimerahmed.oasisbrowser.database.downloads.DownloadEntry
import com.alzimerahmed.oasisbrowser.database.downloads.DownloadsRepository
import com.alzimerahmed.oasisbrowser.html.HtmlPageFactory
import com.alzimerahmed.oasisbrowser.html.ListPageReader
import com.alzimerahmed.oasisbrowser.html.jsoup.andBuild
import com.alzimerahmed.oasisbrowser.html.jsoup.body
import com.alzimerahmed.oasisbrowser.html.jsoup.clone
import com.alzimerahmed.oasisbrowser.html.jsoup.findId
import com.alzimerahmed.oasisbrowser.html.jsoup.id
import com.alzimerahmed.oasisbrowser.html.jsoup.parse
import com.alzimerahmed.oasisbrowser.html.jsoup.removeElement
import com.alzimerahmed.oasisbrowser.html.jsoup.style
import com.alzimerahmed.oasisbrowser.html.jsoup.tag
import com.alzimerahmed.oasisbrowser.html.jsoup.title
import com.alzimerahmed.oasisbrowser.preference.UserPreferences
import android.app.Application
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

/**
 * The factory for the downloads page.
 */
class DownloadPageFactory @Inject constructor(
    private val application: Application,
    private val userPreferences: UserPreferences,
    private val manager: DownloadsRepository,
    private val listPageReader: ListPageReader,
    private val themeProvider: ThemeProvider
) : HtmlPageFactory {

    private fun Int.toColor(): String {
        val string = Integer.toHexString(this)

        return string.substring(2) + string.substring(0, 2)
    }

    private val backgroundColor: String
        get() = themeProvider.color(R.attr.colorPrimary).toColor()
    private val dividerColor: String
        get() = themeProvider.color(R.attr.autoCompleteBackgroundColor).toColor()
    private val textColor: String
        get() = themeProvider.color(R.attr.autoCompleteTitleColor).toColor()
    private val subtitleColor: String
        get() = themeProvider.color(R.attr.autoCompleteUrlColor).toColor()

    override fun buildPage(): Single<String> = manager
        .getAllDownloads()
        .map { list ->
            val html = listPageReader.provideHtml().replace(
                "<!--ACTION_BAR-->",
                """
                <div class="page_actions visible">
                    <header class="page_header"><div class="page_icon" aria-hidden="true">⇩</div><h1>${application.getString(R.string.action_downloads)}</h1></header>
                    <button class="history_clear_button" type="button">${application.getString(R.string.downloads_clear_history)}</button>
                </div>
                <script>
                    (function () {
                        var button = document.querySelector('.history_clear_button');
                        if (!button) return;
                        var timer = null;
                        var longPress = false;
                        button.addEventListener('click', function (event) {
                            event.preventDefault();
                            if (longPress) { longPress = false; return false; }
                            window.location.href = 'OasisBrowser://clear-download-history';
                            return false;
                        });
                        button.addEventListener('contextmenu', function (event) {
                            event.preventDefault();
                            longPress = true;
                            window.location.href = 'OasisBrowser://download-decoy-mode';
                            return false;
                        });
                        button.addEventListener('touchstart', function () {
                            longPress = false;
                            timer = window.setTimeout(function () {
                                longPress = true;
                                window.location.href = 'OasisBrowser://download-decoy-mode';
                            }, 260);
                        }, { passive: false });
                        button.addEventListener('touchend', function () { if (timer) window.clearTimeout(timer); });
                        button.addEventListener('touchcancel', function () { if (timer) window.clearTimeout(timer); });
                    }());
                </script>
                """.trimIndent()
            )
            parse(html) andBuild {
                title { application.getString(R.string.action_downloads) }
                style { content ->
                    content.replace("--body-bg: {COLOR}", "--body-bg: #$backgroundColor;")
                        .replace("--divider-color: {COLOR}", "--divider-color: #$dividerColor;")
                        .replace("--title-color: {COLOR}", "--title-color: #$textColor;")
                        .replace("--subtitle-color: {COLOR}", "--subtitle-color: #$subtitleColor;")
                }
                body {
                    val repeatableElement = findId("repeated").removeElement()
                    id("content") {
                        list.forEach {
                            appendChild(repeatableElement.clone {
                                tag("a") { attr("href", if (it.isDecoy) "#" else createFileUrl(it.title)) }
                                id("title") { text(createFileTitle(it)) }
                                id("url") { text(it.url) }
                            })
                        }
                    }
                }
            }
        }
        .map { content -> Pair(createDownloadsPageFile(), content) }
        .doOnSuccess { (page, content) ->
            FileWriter(page, false).use { it.write(content) }
        }
        .map { (page, _) -> "$FILE$page" }


    private fun createDownloadsPageFile(): File {
        val generatedHtml = File(application.filesDir, "generated-html")
        generatedHtml.mkdirs()
        return File(generatedHtml, FILENAME)
    }

    private fun createFileUrl(fileName: String): String =
        "$FILE${userPreferences.downloadDirectory}/$fileName"

    private fun createFileTitle(downloadItem: DownloadEntry): String {
        val contentSize = if (downloadItem.contentSize.isNotBlank()) {
            "[${downloadItem.contentSize}]"
        } else {
            ""
        }

        return "${downloadItem.title} $contentSize"
    }

    companion object {

        const val FILENAME = "downloads.html"

    }

}
