package com.alzimerahmed.oasisbrowser.database.collection

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

/**
 * The contract for saving, retrieving, and managing user collections and the
 * pages/notes they contain.
 */
interface CollectionRepository {

    /** Creates or replaces a collection. */
    fun addCollection(collection: Collection): Single<Long>

    /** Returns all collections sorted by position, then name. */
    fun getAllCollections(): Single<List<Collection>>

    /** Finds a collection by its [id]. */
    fun findCollection(id: Long): Maybe<Collection>

    /** Deletes a collection and all items inside it. */
    fun deleteCollection(id: Long): Single<Boolean>

    /** Adds an item to a collection, replacing an existing item with the same URL. */
    fun addCollectionItem(item: CollectionItem): Single<Boolean>

    /** Returns all items in [collectionId] sorted by position. */
    fun getCollectionItems(collectionId: Long): Single<List<CollectionItem>>

    /** Updates the note for the item with [itemId]. */
    fun updateItemNote(itemId: Long, note: String?): Completable

    /** Removes a single item from a collection. */
    fun deleteCollectionItem(itemId: Long): Single<Boolean>
}
