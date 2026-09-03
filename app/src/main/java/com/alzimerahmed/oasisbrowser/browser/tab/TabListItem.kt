package com.alzimerahmed.oasisbrowser.browser.tab

/**
 * A sealed class representing items that can appear in the tab list, including
 * both individual tabs and group headers.
 */
sealed class TabListItem {

    /**
     * A tab item in the list.
     *
     * @param tab The view state for the tab.
     * @param groupId The ID of the group this tab belongs to, or null if ungrouped.
     */
    data class TabItem(
        val tab: TabViewState,
        val groupId: Int?,
    ) : TabListItem()

    /**
     * A group header item in the list.
     *
     * @param group The tab group.
     * @param tabCount The number of tabs in this group.
     */
    data class GroupHeader(
        val group: TabGroup,
        val tabCount: Int,
    ) : TabListItem()
}
