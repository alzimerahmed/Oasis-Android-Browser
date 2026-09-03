package com.alzimerahmed.oasisbrowser.database.readinglist

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

/**
 * The contract for saving, retrieving, and managing reading-list articles.
 */
interface ReadingListRepository {

    /**
     * Adds a reading-list entry, replacing any existing entry with the same URL.
     *
     * @param entry The entry to persist.
     * @return An observable that emits true when the entry is persisted.
     */
    fun addReadingListEntry(entry: ReadingListEntry): Single<Boolean>

    /**
     * Determines whether the given [url] is already in the reading list.
     */
    fun isReadingListEntry(url: String): Single<Boolean>

    /**
     * Returns the reading-list entry with the given [url], if one exists.
     */
    fun findReadingListEntry(url: String): Maybe<ReadingListEntry>

    /**
     * Returns all reading-list entries ordered by most recently added first.
     */
    fun getAllReadingListEntries(): Single<List<ReadingListEntry>>

    /**
     * Updates the offline HTML snapshot for the entry at [url].
     */
    fun updateHtmlSnapshot(url: String, htmlSnapshot: String): Completable

    /**
     * Updates the reading progress for the entry at [url].
     *
     * @param url The URL of the entry.
     * @param progress The 0–100 progress value.
     */
    fun updateReadingProgress(url: String, progress: Int): Completable

    /**
     * Deletes the reading-list entry with the given [url].
     *
     * @return An observable that emits true when an entry was deleted.
     */
    fun deleteReadingListEntry(url: String): Single<Boolean>

    /**
     * Deletes every reading-list entry.
     */
    fun deleteAllReadingListEntries(): Completable
}
