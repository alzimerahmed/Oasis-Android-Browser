package com.alzimerahmed.oasisbrowser.browser.ui

import com.alzimerahmed.oasisbrowser.preference.IntEnum

/** Placement of the OasisBrowser rail. */
enum class OasisBrowserRailPosition(override val value: Int) : IntEnum {
    RIGHT(0),
    LEFT(1),
    TOP(2),
    BOTTOM(3);

    /** Internal layout selector. Top and bottom use the horizontal implementation. */
    val isExperimental: Boolean
        get() = this == TOP || this == BOTTOM
}
