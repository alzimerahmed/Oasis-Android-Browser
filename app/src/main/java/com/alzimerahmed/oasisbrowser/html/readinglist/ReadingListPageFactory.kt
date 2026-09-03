package com.alzimerahmed.oasisbrowser.html.readinglist

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.browser.theme.ThemeProvider
import com.alzimerahmed.oasisbrowser.constant.FILE
import com.alzimerahmed.oasisbrowser.database.readinglist.ReadingListRepository
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
import android.app.Application
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

/**
 * Factory for the reading list page.
 */
class ReadingListPageFactory @Inject constructor(
    private val listPageReader: ListPageReader,
    private val application: Application,
    private val readingListRepository: ReadingListRepository,
    private val themeProvider: ThemeProvider
) : HtmlPageFactory {

    private val title = application.getString(R.string.action_reading_list)

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

    override fun buildPage(): Single<String> = readingListRepository
        .getAllReadingListEntries()
        .map { list ->
            val html = listPageReader.provideHtml().replace(
                "<!--ACTION_BAR-->",
                """
                <div class="page_actions visible">
                    <header class="page_header"><div class="page_icon" aria-hidden="true">✓</div><h1>${title}</h1></header>
                    <button class="history_clear_button" type="button">${application.getString(R.string.reading_list_clear_all)}</button>
                </div>
                <script>
                    (function () {
                        var button = document.querySelector('.history_clear_button');
                        if (!button) return;
                        button.addEventListener('click', function (event) {
                            event.preventDefault();
                            window.location.href = 'OasisBrowser://clear-reading-list';
                            return false;
                        });
                    })();
                </script>
                """.trimIndent()
            )
            parse(html) andBuild {
                title { title }
                style { content ->
                    content.replace("--body-bg: {COLOR}", "--body-bg: #$backgroundColor;")
                        .replace("--divider-color: {COLOR}", "--divider-color: #$dividerColor;")
                        .replace("--title-color: {COLOR}", "--title-color: #$textColor;")
                        .replace("--subtitle-color: {COLOR}", "--subtitle-color: #$subtitleColor;")
                }
                body {
                    val repeatedElement = findId("repeated").removeElement()
                    id("content") {
                        list.forEach {
                            appendChild(repeatedElement.clone {
                                tag("a") { attr("href", it.url) }
                                id("title") { text(it.title) }
                                id("url") { text(it.url) }
                            })
                        }
                    }
                }
            }
        }
        .map { content -> Pair(createReadingListPage(), content) }
        .doOnSuccess { (page, content) ->
            FileWriter(page, false).use { it.write(content) }
        }
        .map { (page, _) -> "$FILE$page" }

    /**
     * Use this observable to immediately delete the reading list page. This will clear the cached
     * reading list page that was stored on file.
     *
     * @return a completable that deletes the reading list page when subscribed to.
     */
    fun deleteReadingListPage(): Completable = Completable.fromAction {
        with(createReadingListPage()) {
            if (exists()) {
                delete()
            }
        }
    }

    private fun createReadingListPage(): File {
        val generatedHtml = File(application.filesDir, "generated-html")
        generatedHtml.mkdirs()
        return File(generatedHtml, FILENAME)
    }

    companion object {
        const val FILENAME = "reading-list.html"
    }

}
