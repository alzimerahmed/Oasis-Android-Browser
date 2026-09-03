package com.alzimerahmed.oasisbrowser.search.engine

import com.alzimerahmed.oasisbrowser.R

/**
 * The DuckDuckGo Lite search engine.
 *
 * See https://duckduckgo.com/assets/logo_homepage.normal.v101.png for the icon.
 */
class DuckLiteSearch : BaseSearchEngine(
    "file:///android_asset/duckduckgo.png",
    "https://duckduckgo.com/lite/?t=OasisBrowser&q=",
    R.string.search_engine_duckduckgo_lite
)
