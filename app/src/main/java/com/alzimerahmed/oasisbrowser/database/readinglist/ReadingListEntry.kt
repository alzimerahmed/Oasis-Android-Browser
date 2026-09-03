package com.alzimerahmed.oasisbrowser.database.readinglist

/**
 * A data type that represents an article saved for later reading.
 *
 * @param url The URL of the original article.
 * @param title The title of the article.
 * @param addedAt The time the entry was added, in milliseconds since the epoch.
 * @param htmlSnapshot Optional offline HTML snapshot of the article content.
 * @param readingProgress Scroll/read progress from 0 to 100.
 */
data class ReadingListEntry(
    val url: String,
    val title: String,
    val addedAt: Long = System.currentTimeMillis(),
    val htmlSnapshot: String? = null,
    val readingProgress: Int = 0,
)
