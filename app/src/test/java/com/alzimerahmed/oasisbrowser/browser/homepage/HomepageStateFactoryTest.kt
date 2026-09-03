package com.alzimerahmed.oasisbrowser.browser.homepage

import android.app.Application
import com.alzimerahmed.oasisbrowser.AppTheme
import com.alzimerahmed.oasisbrowser.database.Bookmark
import com.alzimerahmed.oasisbrowser.database.bookmark.BookmarkRepository
import com.alzimerahmed.oasisbrowser.database.bookmark.BookmarkSortOrder
import com.alzimerahmed.oasisbrowser.preference.UserPreferences
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class HomepageStateFactoryTest {
    private val application = mock(Application::class.java)
    private val repository = mock(BookmarkRepository::class.java)
    private val preferences = mock(UserPreferences::class.java)

    @Test
    fun `state limits shortcuts and clamps visual settings`() {
        val bookmarks = (1..12).map { index ->
            Bookmark.Entry(
                url = "https://example.com/$index",
                title = "Bookmark $index",
                position = index,
                folder = Bookmark.Folder.Root,
            )
        }
        `when`(repository.getAllBookmarksSorted(BookmarkSortOrder.MANUAL))
            .thenReturn(Single.just(bookmarks))
        `when`(preferences.useTheme).thenReturn(AppTheme.DARK)
        `when`(preferences.homepageBookmarksEnabled).thenReturn(true)
        `when`(preferences.homepageBookmarkColumns).thenReturn(20)
        `when`(preferences.homepageWallpaperOpacity).thenReturn(160)
        `when`(preferences.homepageWallpaperPositionX).thenReturn(-10)
        `when`(preferences.homepageWallpaperPositionY).thenReturn(140)
        `when`(preferences.homepageMottoSize).thenReturn(80)
        `when`(preferences.homepageMottoOpacity).thenReturn(-5)
        `when`(preferences.homepageMotto).thenReturn("Ex se sola veritas fluit")
        `when`(preferences.homepageTimeFormat).thenReturn("not[a valid pattern")
        `when`(preferences.homepageDateFormat).thenReturn("")

        val state = HomepageStateFactory(application, repository, preferences)
            .create()
            .blockingGet()

        assertThat(state.bookmarks).hasSize(8)
        assertThat(state.bookmarkColumns).isEqualTo(4)
        assertThat(state.wallpaperOpacity).isEqualTo(1f)
        assertThat(state.wallpaperPositionX).isZero()
        assertThat(state.wallpaperPositionY).isEqualTo(1f)
        assertThat(state.mottoSizeSp).isEqualTo(32f)
        assertThat(state.mottoOpacity).isZero()
        assertThat(state.timePattern).isEqualTo("HH:mm")
        assertThat(state.datePattern).isEqualTo("EEEE, d MMMM yyyy")
        assertThat(state.wallpaper)
            .isEqualTo(HomepageUiState.Wallpaper.Bundled("homepage_wallpaper_dark.jpg"))
    }
}
