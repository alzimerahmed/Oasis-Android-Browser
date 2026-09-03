package com.alzimerahmed.oasisbrowser.browser.homepage

import com.alzimerahmed.oasisbrowser.database.Bookmark

data class HomepageUiState(
    val bookmarks: List<Bookmark.Entry>,
    val bookmarksVisible: Boolean,
    val bookmarkColumns: Int,
    val wallpaper: Wallpaper,
    val wallpaperOpacity: Float,
    val wallpaperPositionX: Float,
    val wallpaperPositionY: Float,
    val dateTimeVisible: Boolean,
    val timePattern: String,
    val datePattern: String,
    val dateTimeOpacity: Float,
    val mottoVisible: Boolean,
    val motto: String,
    val mottoSizeSp: Float,
    val mottoOpacity: Float,
) {
    sealed interface Wallpaper {
        data class Bundled(val assetName: String) : Wallpaper
        data class Custom(val path: String) : Wallpaper
        data object Black : Wallpaper
    }
}
