package com.alzimerahmed.oasisbrowser.database.collection

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.database.databaseDelegate
import com.alzimerahmed.oasisbrowser.extensions.useMap
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Disk-backed collections and notes storage. See [CollectionRepository] for method documentation.
 */
@Singleton
class CollectionDatabase @Inject constructor(
    private val application: Application,
) : SQLiteOpenHelper(application, DATABASE_NAME, null, DATABASE_VERSION), CollectionRepository {

    private val database: SQLiteDatabase by databaseDelegate()

    override fun onCreate(db: SQLiteDatabase) {
        val createCollectionsTable =
            "CREATE TABLE ${DatabaseUtils.sqlEscapeString(TABLE_COLLECTIONS)}(" +
                "${DatabaseUtils.sqlEscapeString(KEY_ID)} INTEGER PRIMARY KEY," +
                "${DatabaseUtils.sqlEscapeString(KEY_NAME)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_POSITION)} INTEGER" +
                ')'
        val createItemsTable =
            "CREATE TABLE ${DatabaseUtils.sqlEscapeString(TABLE_ITEMS)}(" +
                "${DatabaseUtils.sqlEscapeString(KEY_ID)} INTEGER PRIMARY KEY," +
                "${DatabaseUtils.sqlEscapeString(KEY_COLLECTION_ID)} INTEGER," +
                "${DatabaseUtils.sqlEscapeString(KEY_URL)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_TITLE)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_NOTE)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_POSITION)} INTEGER," +
                "FOREIGN KEY(${KEY_COLLECTION_ID}) REFERENCES ${TABLE_COLLECTIONS}(${KEY_ID}) ON DELETE CASCADE" +
                ')'
        db.execSQL(createCollectionsTable)
        db.execSQL(createItemsTable)

        val default = ContentValues(3).apply {
            put(KEY_ID, 1)
            put(KEY_NAME, application.getString(R.string.collection_default_name))
            put(KEY_POSITION, 0)
        }
        db.insert(TABLE_COLLECTIONS, null, default)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseUtils.sqlEscapeString(TABLE_ITEMS)}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseUtils.sqlEscapeString(TABLE_COLLECTIONS)}")
        onCreate(db)
    }

    override fun addCollection(collection: Collection): Single<Long> = Single.fromCallable {
        database.insert(
            TABLE_COLLECTIONS,
            null,
            collection.toContentValues()
        )
    }

    override fun getAllCollections(): Single<List<Collection>> = Single.fromCallable {
        database.query(
            TABLE_COLLECTIONS,
            null,
            null,
            null,
            null,
            null,
            "$KEY_POSITION ASC, $KEY_NAME COLLATE NOCASE ASC"
        ).useMap { it.bindToCollection() }
    }

    override fun findCollection(id: Long): Maybe<Collection> = Maybe.fromCallable {
        database.query(
            TABLE_COLLECTIONS,
            null,
            "$KEY_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            "1"
        ).useMap { it.bindToCollection() }.firstOrNull()
    }

    override fun deleteCollection(id: Long): Single<Boolean> = Single.fromCallable {
        database.delete(TABLE_COLLECTIONS, "$KEY_ID = ?", arrayOf(id.toString())) > 0
    }

    override fun addCollectionItem(item: CollectionItem): Single<Boolean> = Single.fromCallable {
        database.insertWithOnConflict(
            TABLE_ITEMS,
            null,
            item.toContentValues(),
            SQLiteDatabase.CONFLICT_REPLACE
        ) != -1L
    }

    override fun getCollectionItems(collectionId: Long): Single<List<CollectionItem>> =
        Single.fromCallable {
            database.query(
                TABLE_ITEMS,
                null,
                "$KEY_COLLECTION_ID = ?",
                arrayOf(collectionId.toString()),
                null,
                null,
                "$KEY_POSITION ASC, $KEY_TITLE COLLATE NOCASE ASC"
            ).useMap { it.bindToCollectionItem() }
        }

    override fun updateItemNote(itemId: Long, note: String?): Completable = Completable.fromAction {
        val values = ContentValues().apply { put(KEY_NOTE, note) }
        database.update(TABLE_ITEMS, values, "$KEY_ID = ?", arrayOf(itemId.toString()))
    }

    override fun deleteCollectionItem(itemId: Long): Single<Boolean> = Single.fromCallable {
        database.delete(TABLE_ITEMS, "$KEY_ID = ?", arrayOf(itemId.toString())) > 0
    }

    private fun Collection.toContentValues() = ContentValues(3).apply {
        if (id != 0L) put(KEY_ID, id)
        put(KEY_NAME, name)
        put(KEY_POSITION, position)
    }

    private fun CollectionItem.toContentValues() = ContentValues(6).apply {
        if (id != 0L) put(KEY_ID, id)
        put(KEY_COLLECTION_ID, collectionId)
        put(KEY_URL, url)
        put(KEY_TITLE, title)
        put(KEY_NOTE, note)
        put(KEY_POSITION, position)
    }

    private fun Cursor.bindToCollection() = Collection(
        id = getLong(getColumnIndexOrThrow(KEY_ID)),
        name = getString(getColumnIndexOrThrow(KEY_NAME)),
        position = getInt(getColumnIndexOrThrow(KEY_POSITION)),
    )

    private fun Cursor.bindToCollectionItem() = CollectionItem(
        id = getLong(getColumnIndexOrThrow(KEY_ID)),
        collectionId = getLong(getColumnIndexOrThrow(KEY_COLLECTION_ID)),
        url = getString(getColumnIndexOrThrow(KEY_URL)),
        title = getString(getColumnIndexOrThrow(KEY_TITLE)),
        note = getString(getColumnIndexOrThrow(KEY_NOTE)),
        position = getInt(getColumnIndexOrThrow(KEY_POSITION)),
    )

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "collectionManager"

        private const val TABLE_COLLECTIONS = "collections"
        private const val TABLE_ITEMS = "collection_items"

        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_POSITION = "position"
        private const val KEY_COLLECTION_ID = "collection_id"
        private const val KEY_URL = "url"
        private const val KEY_TITLE = "title"
        private const val KEY_NOTE = "note"
    }
}
