package com.alzimerahmed.oasisbrowser.database.collection

/**
 * A single saved page inside a [Collection].
 *
 * @param id Unique item identifier; 0 when creating.
 * @param collectionId The [Collection.id] this item belongs to.
 * @param url The page URL.
 * @param title The page title.
 * @param note Optional user note for this item.
 * @param position Manual ordering value inside the collection.
 */
data class CollectionItem(
    val id: Long = 0,
    val collectionId: Long,
    val url: String,
    val title: String,
    val note: String? = null,
    val position: Int = 0,
)
