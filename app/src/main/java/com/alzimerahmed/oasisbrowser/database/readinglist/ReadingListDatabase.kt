package com.alzimerahmed.oasisbrowser.database.readinglist

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.alzimerahmed.oasisbrowser.database.databaseDelegate
import com.alzimerahmed.oasisbrowser.extensions.firstOrNullMap
import com.alzimerahmed.oasisbrowser.extensions.useMap
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Disk-backed reading list storage. See [ReadingListRepository] for method documentation.
 */
@Singleton
class ReadingListDatabase @Inject constructor(
    application: Application,
) : SQLiteOpenHelper(application, DATABASE_NAME, null, DATABASE_VERSION), ReadingListRepository {

    private val database: SQLiteDatabase by databaseDelegate()

    override fun onCreate(db: SQLiteDatabase) {
        val createReadingListTable =
            "CREATE TABLE ${DatabaseUtils.sqlEscapeString(TABLE_READING_LIST)}(" +
                "${DatabaseUtils.sqlEscapeString(KEY_ID)} INTEGER PRIMARY KEY," +
                "${DatabaseUtils.sqlEscapeString(KEY_URL)} TEXT UNIQUE," +
                "${DatabaseUtils.sqlEscapeString(KEY_TITLE)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_ADDED_AT)} INTEGER," +
                "${DatabaseUtils.sqlEscapeString(KEY_HTML_SNAPSHOT)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_READING_PROGRESS)} INTEGER NOT NULL DEFAULT 0" +
                ')'
        db.execSQL(createReadingListTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseUtils.sqlEscapeString(TABLE_READING_LIST)}")
        onCreate(db)
    }

    override fun addReadingListEntry(entry: ReadingListEntry): Single<Boolean> =
        Single.fromCallable {
            val existing = getReadingListEntry(entry.url)
            val values = entry.copy(addedAt = existing?.addedAt ?: entry.addedAt).toContentValues()
            val result = database.insertWithOnConflict(
                TABLE_READING_LIST,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            result != -1L
        }

    override fun isReadingListEntry(url: String): Single<Boolean> = Single.fromCallable {
        database.query(
            TABLE_READING_LIST,
            arrayOf(KEY_ID),
            "$KEY_URL = ?",
            arrayOf(url),
            null,
            null,
            null,
            "1"
        ).use { it.moveToFirst() }
    }

    override fun findReadingListEntry(url: String): Maybe<ReadingListEntry> = Maybe.fromCallable {
        database.query(
            TABLE_READING_LIST,
            null,
            "$KEY_URL = ?",
            arrayOf(url),
            null,
            null,
            null,
            "1"
        ).firstOrNullMap { it.bindToReadingListEntry() }
    }

    override fun getAllReadingListEntries(): Single<List<ReadingListEntry>> = Single.fromCallable {
        database.query(
            TABLE_READING_LIST,
            null,
            null,
            null,
            null,
            null,
            "$KEY_ADDED_AT DESC"
        ).useMap { it.bindToReadingListEntry() }
    }

    override fun updateHtmlSnapshot(url: String, htmlSnapshot: String): Completable =
        Completable.fromAction {
            val values = ContentValues().apply { put(KEY_HTML_SNAPSHOT, htmlSnapshot) }
            database.update(TABLE_READING_LIST, values, "$KEY_URL = ?", arrayOf(url))
        }

    override fun updateReadingProgress(url: String, progress: Int): Completable =
        Completable.fromAction {
            val clampedProgress = progress.coerceIn(0, 100)
            val values = ContentValues().apply { put(KEY_READING_PROGRESS, clampedProgress) }
            database.update(TABLE_READING_LIST, values, "$KEY_URL = ?", arrayOf(url))
        }

    override fun deleteReadingListEntry(url: String): Single<Boolean> = Single.fromCallable {
        database.delete(TABLE_READING_LIST, "$KEY_URL = ?", arrayOf(url)) > 0
    }

    override fun deleteAllReadingListEntries(): Completable = Completable.fromAction {
        database.delete(TABLE_READING_LIST, null, null)
    }

    private fun getReadingListEntry(url: String): ReadingListEntry? =
        database.query(
            TABLE_READING_LIST,
            null,
            "$KEY_URL = ?",
            arrayOf(url),
            null,
            null,
            null,
            "1"
        ).firstOrNullMap { it.bindToReadingListEntry() }

    private fun ReadingListEntry.toContentValues() = ContentValues(5).apply {
        put(KEY_URL, url)
        put(KEY_TITLE, title)
        put(KEY_ADDED_AT, addedAt)
        put(KEY_HTML_SNAPSHOT, htmlSnapshot)
        put(KEY_READING_PROGRESS, readingProgress)
    }

    private fun Cursor.bindToReadingListEntry() = ReadingListEntry(
        url = getString(getColumnIndexOrThrow(KEY_URL)),
        title = getString(getColumnIndexOrThrow(KEY_TITLE)),
        addedAt = getLong(getColumnIndexOrThrow(KEY_ADDED_AT)),
        htmlSnapshot = getString(getColumnIndexOrThrow(KEY_HTML_SNAPSHOT)),
        readingProgress = getInt(getColumnIndexOrThrow(KEY_READING_PROGRESS)),
    )

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "readingListManager"

        private const val TABLE_READING_LIST = "reading_list"
        private const val KEY_ID = "id"
        private const val KEY_URL = "url"
        private const val KEY_TITLE = "title"
        private const val KEY_ADDED_AT = "added_at"
        private const val KEY_HTML_SNAPSHOT = "html_snapshot"
        private const val KEY_READING_PROGRESS = "reading_progress"
    }
}
