package com.alzimerahmed.oasisbrowser.utils

import android.net.Uri

/** Strips common tracking parameters and empty fragments from URLs. */
object UrlCleaner {

    private val TRACKING_PARAMS = setOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "utm_id", "utm_name", "utm_cid", "utm_reader", "utm_social", "utm_brand",
        "fbclid", "gclid", "dclid", "msclkid", "twclid", "igshid", "mc_cid",
        "mc_eid", "yclid", "_hsenc", "_hsmi", "vero_id", "wickedid", "ttclid",
        "s_kwcid", "li_fat_id", "mibextid", "si", "ref_src", "ref_url", "ref",
        "referrer", "spm", "scm", "share_source", "share_medium", "share_app_id"
    )

    /**
     * Returns [url] with known tracking query parameters and empty fragments removed.
     * Non-http(s) URLs are returned unchanged.
     */
    fun clean(url: String): String {
        if (url.isBlank()) return url
        val uri = Uri.parse(url)
        val scheme = uri.scheme?.lowercase() ?: return url
        if (scheme != "http" && scheme != "https") return url

        val kept = uri.queryParameterNames
            .filter { it.lowercase() !in TRACKING_PARAMS }
        val builder = uri.buildUpon()
            .query(
                if (kept.isEmpty()) {
                    null
                } else {
                    kept.joinToString("&") { key ->
                        Uri.encode(key) + "=" + Uri.encode(uri.getQueryParameter(key).orEmpty())
                    }
                }
            )
        if (uri.fragment.isNullOrEmpty()) builder.fragment(null)
        return builder.build().toString()
    }
}
