package com.alzimerahmed.oasisbrowser.browser.tab

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents a group of tabs in the browser.
 *
 * @param id The unique identifier for the group.
 * @param name The display name of the group.
 * @param isCollapsed True if the group's tabs are hidden in the tab list, false otherwise.
 */
data class TabGroup(
    val id: Int,
    val name: String,
    val isCollapsed: Boolean = false,
)

/**
 * Manages tab groups in memory. Tracks which tabs belong to which groups and the
 * collapsed/expanded state of each group.
 */
@Singleton
class TabGroupManager @Inject constructor() {

    private val idGenerator = AtomicInteger(0)

    private val groups = mutableMapOf<Int, TabGroup>()
    private val tabToGroup = mutableMapOf<Int, Int>()

    /**
     * Create a new tab group with the provided [name].
     *
     * @return the newly created [TabGroup].
     */
    fun createGroup(name: String): TabGroup {
        val id = idGenerator.incrementAndGet()
        val group = TabGroup(id = id, name = name)
        groups[id] = group
        return group
    }

    /**
     * Rename the group with the provided [groupId] to [newName].
     */
    fun renameGroup(groupId: Int, newName: String) {
        groups[groupId]?.let { groups[groupId] = it.copy(name = newName) }
    }

    /**
     * Delete the group with the provided [groupId]. Tabs in the group are ungrouped
     * but not closed.
     */
    fun deleteGroup(groupId: Int) {
        groups.remove(groupId)
        tabToGroup.entries.removeAll { it.value == groupId }
    }

    /**
     * Add the tab with [tabId] to the group with [groupId].
     */
    fun addTabToGroup(tabId: Int, groupId: Int) {
        if (groups.containsKey(groupId)) {
            tabToGroup[tabId] = groupId
        }
    }

    /**
     * Remove the tab with [tabId] from its group (if any).
     */
    fun removeTabFromGroup(tabId: Int) {
        tabToGroup.remove(tabId)
    }

    /**
     * Toggle the collapsed state of the group with [groupId].
     */
    fun toggleGroupCollapse(groupId: Int) {
        groups[groupId]?.let { groups[groupId] = it.copy(isCollapsed = !it.isCollapsed) }
    }

    /**
     * Get the group ID for the tab with [tabId], or null if the tab is not in a group.
     */
    fun getGroupIdForTab(tabId: Int): Int? = tabToGroup[tabId]

    /**
     * Get the [TabGroup] for the given [groupId], or null if it doesn't exist.
     */
    fun getGroup(groupId: Int): TabGroup? = groups[groupId]

    /**
     * Get all groups.
     */
    fun getAllGroups(): List<TabGroup> = groups.values.toList().sortedBy { it.id }

    /**
     * Get all tab IDs in the group with [groupId].
     */
    fun getTabIdsInGroup(groupId: Int): List<Int> =
        tabToGroup.entries.filter { it.value == groupId }.map { it.key }

    /**
     * Remove a tab from any group it belongs to. Called when a tab is closed.
     */
    fun onTabClosed(tabId: Int) {
        tabToGroup.remove(tabId)
    }

    /**
     * Clear all groups and tab-to-group mappings.
     */
    fun clearAll() {
        groups.clear()
        tabToGroup.clear()
        idGenerator.set(0)
    }
}
