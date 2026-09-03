package com.alzimerahmed.oasisbrowser.search.engine

import com.alzimerahmed.oasisbrowser.R

/**
 * A custom search engine.
 */
class CustomSearch(queryUrl: String) : BaseSearchEngine(
    "file:///android_asset/OasisBrowser.png",
    queryUrl,
    R.string.search_engine_custom
)
