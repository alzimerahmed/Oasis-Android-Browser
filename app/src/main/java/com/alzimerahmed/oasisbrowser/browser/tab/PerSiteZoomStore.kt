package com.alzimerahmed.oasisbrowser.browser.tab

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import javax.inject.Inject

/** Persists a per-host text zoom level so each site remembers its preferred zoom. */
class PerSiteZoomStore @Inject constructor(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("per_site_zoom", Context.MODE_PRIVATE)

    /** Returns the stored text zoom for the host of [url], or null when none is stored. */
    fun zoomFor(url: String): Int? {
        val host = hostOf(url) ?: return null
        if (!preferences.contains(host)) return null
        return preferences.getInt(host, DEFAULT_TEXT_ZOOM)
    }

    /** Stores [textZoom] for the host of [url]. */
    fun save(url: String, textZoom: Int) {
        val host = hostOf(url) ?: return
        preferences.edit().putInt(host, textZoom).apply()
    }

    private fun hostOf(url: String): String? =
        Uri.parse(url).host?.lowercase()?.takeIf { it.isNotBlank() }

    private companion object {
        private const val DEFAULT_TEXT_ZOOM = 100
    }
}
