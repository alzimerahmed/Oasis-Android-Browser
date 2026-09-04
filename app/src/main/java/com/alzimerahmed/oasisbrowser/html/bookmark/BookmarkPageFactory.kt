package com.alzimerahmed.oasisbrowser.html.bookmark

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.browser.di.DatabaseScheduler
import com.alzimerahmed.oasisbrowser.browser.di.DiskScheduler
import com.alzimerahmed.oasisbrowser.browser.theme.ThemeProvider
import com.alzimerahmed.oasisbrowser.constant.FILE
import com.alzimerahmed.oasisbrowser.database.Bookmark
import com.alzimerahmed.oasisbrowser.database.bookmark.BookmarkRepository
import com.alzimerahmed.oasisbrowser.extensions.safeUse
import com.alzimerahmed.oasisbrowser.favicon.FaviconModel
import com.alzimerahmed.oasisbrowser.favicon.toValidUri
import com.alzimerahmed.oasisbrowser.html.HtmlPageFactory
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
import com.alzimerahmed.oasisbrowser.utils.ThemeUtils
import com.alzimerahmed.oasisbrowser.preference.UserPreferences
import android.app.Application
import android.graphics.Bitmap
import androidx.core.net.toUri
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import javax.inject.Inject

/**
 * Created by anthonycr on 9/23/18.
 */
class BookmarkPageFactory @Inject constructor(
    private val application: Application,
    private val bookmarkModel: BookmarkRepository,
    private val faviconModel: FaviconModel,
    @DatabaseScheduler private val databaseScheduler: Scheduler,
    @DiskScheduler private val diskScheduler: Scheduler,
    private val bookmarkPageReader: BookmarkPageReader,
    private val themeProvider: ThemeProvider,
    private val userPreferences: UserPreferences
) : HtmlPageFactory {

    private val title = application.getString(R.string.action_bookmarks)
    private val folderIconFile by lazy {
        File(FaviconModel.faviconCacheFolder(application), FOLDER_ICON)
    }
    private val defaultIconFile by lazy {
        File(FaviconModel.faviconCacheFolder(application), DEFAULT_ICON)
    }

    private fun Int.toColor(): String {
        val string = Integer.toHexString(this)

        return string.substring(2) + string.substring(0, 2)
    }

    private val backgroundColor: String
        get() = themeProvider.color(R.attr.colorPrimary).toColor()
    private val cardColor: String
        get() = themeProvider.color(R.attr.autoCompleteBackgroundColor).toColor()
    private val textColor: String
        get() = themeProvider.color(R.attr.autoCompleteTitleColor).toColor()

    override fun buildPage(): Single<String> = bookmarkModel
        .getAllBookmarksSorted(userPreferences.bookmarkSortOrder)
        .flattenAsObservable { it }
        .groupBy<Bookmark.Folder, Bookmark>(Bookmark.Entry::folder) { it }
        .flatMapSingle { bookmarksInFolder ->
            val folder = bookmarksInFolder.key
            return@flatMapSingle bookmarksInFolder
                .toList()
                .concatWith(
                    if (folder == Bookmark.Folder.Root) {
                        bookmarkModel.getFoldersSorted()
                            .map { it.filterIsInstance<Bookmark.Folder.Entry>() }
                    } else {
                        Single.just(emptyList())
                    }
                )
                .toList()
                .map { bookmarksAndFolders ->
                    Pair(folder, bookmarksAndFolders.flatten().map { it.asViewModel() })
                }
        }
        .map { (folder, viewModels) -> Pair(folder, construct(viewModels)) }
        .subscribeOn(databaseScheduler)
        .observeOn(diskScheduler)
        .doOnNext { (folder, content) ->
            FileWriter(createBookmarkPage(folder), false).use {
                it.write(content)
            }
        }
        .ignoreElements()
        .toSingle {
            cacheIcon(
                ThemeUtils.createThemedBitmap(
                    application,
                    R.drawable.ic_folder,
                    themeProvider.color(R.attr.autoCompleteTitleColor)
                ),
                folderIconFile
            )
            cacheIcon(faviconModel.createDefaultBitmapForTitle(null), defaultIconFile)

            "$FILE${createBookmarkPage(null)}"
        }

    private fun cacheIcon(icon: Bitmap, file: File) = FileOutputStream(file).safeUse {
        icon.compress(Bitmap.CompressFormat.PNG, 100, it)
        icon.recycle()
    }

    private fun construct(list: List<BookmarkViewModel>): String {
        return parse(bookmarkPageReader.provideHtml()) andBuild {
            title { title }
            style { content ->
                content.replace("--body-bg: {COLOR}", "--body-bg: #$backgroundColor;")
                    .replace("--box-bg: {COLOR}", "--box-bg: #$cardColor;")
                    .replace("--box-txt: {COLOR}", "--box-txt: #$textColor;")
            }
            body {
                id("bookmark-title") { appendText(title) }
                val repeatableElement = findId("repeated").removeElement()
                id("content") {
                    list.forEach {
                        appendChild(repeatableElement.clone {
                            tag("a") { attr("href", it.url) }
                            attr("data-search", "${it.title} ${it.url}")
                            tag("img") { attr("src", it.iconUrl) }
                            id("title") { appendText(it.title) }
                        })
                    }
                }
            }
        }
    }

    private fun Bookmark.asViewModel(): BookmarkViewModel = when (this) {
        is Bookmark.Folder -> createViewModelForFolder(this)
        is Bookmark.Entry -> createViewModelForBookmark(this)
    }

    private fun createViewModelForFolder(folder: Bookmark.Folder): BookmarkViewModel {
        val folderPage = createBookmarkPage(folder)
        val url = "$FILE$folderPage"

        return BookmarkViewModel(
            title = folder.title,
            url = url,
            iconUrl = folderIconFile.toString()
        )
    }

    private fun createViewModelForBookmark(entry: Bookmark.Entry): BookmarkViewModel {
        if (!userPreferences.bookmarkFaviconsEnabled) {
            return BookmarkViewModel(entry.title, entry.url, defaultIconFile.toString())
        }
        val bookmarkUri = entry.url.toUri().toValidUri()

        val iconUrl = if (bookmarkUri != null) {
            val faviconFile = FaviconModel.getFaviconCacheFile(application, bookmarkUri)
            if (!faviconFile.exists()) {
                val defaultFavicon = faviconModel.createDefaultBitmapForTitle(entry.title)
                faviconModel.cacheFaviconForUrl(defaultFavicon, entry.url)
                    .subscribeOn(diskScheduler)
                    .subscribe(
                        {},
                        { e -> android.util.Log.e("BookmarkPageFactory", "cacheFavicon failed", e) }
                    )
            }

            faviconFile
        } else {
            defaultIconFile
        }

        return BookmarkViewModel(
            title = entry.title,
            url = entry.url,
            iconUrl = iconUrl.toString()
        )
    }

    /**
     * Create the bookmark page file.
     */
    fun createBookmarkPage(folder: Bookmark.Folder?): File {
        val prefix = if (folder?.title?.isNotBlank() == true) {
            "${folder.title}-"
        } else {
            ""
        }
        val generatedHtml = File(application.filesDir, "generated-html")
        generatedHtml.mkdirs()
        return File(generatedHtml, prefix + FILENAME)
    }

    companion object {

        const val FILENAME = "bookmarks.html"

        private const val FOLDER_ICON = "folder.png"
        private const val DEFAULT_ICON = "default.png"

    }
}
