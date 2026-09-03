package com.alzimerahmed.oasisbrowser.database.bookmark

import com.alzimerahmed.oasisbrowser.preference.IntEnum

enum class BookmarkSortOrder(override val value: Int) : IntEnum {
    MANUAL(0),
    TITLE_ASC(1),
    TITLE_DESC(2),
    URL_ASC(3),
    URL_DESC(4)
}
