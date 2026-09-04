package com.alzimerahmed.oasisbrowser.migration

import com.alzimerahmed.oasisbrowser.BuildConfig
import com.alzimerahmed.oasisbrowser.preference.UserPreferences
import javax.inject.Inject

/**
 * Handle cleanup that should run on upgrade.
 */
class Cleanup @Inject constructor(
    private val actions: List<@JvmSuppressWildcards Action>,
    private val userPreferences: UserPreferences
) {

    suspend fun cleanup() {
        val currentVersionCode = BuildConfig.VERSION_CODE.toLong()
        val lastCleanupVersionCode = userPreferences.lastCleanupVersionCode
        if (lastCleanupVersionCode == currentVersionCode) {
            return
        }

        actions
            .filter { it.versionCode > lastCleanupVersionCode }
            .forEach { it.execute() }

        userPreferences.lastCleanupVersionCode = currentVersionCode
    }

    interface Action {
        val versionCode: Int
        suspend fun execute()
    }
}
