package com.alzimerahmed.oasisbrowser.database.collection

/**
 * A named collection of web pages and the notes attached to them.
 *
 * @param id Unique collection identifier; 0 when creating a new collection.
 * @param name Display name chosen by the user.
 * @param position Manual ordering value.
 */
data class Collection(
    val id: Long = 0,
    val name: String,
    val position: Int = 0,
)
