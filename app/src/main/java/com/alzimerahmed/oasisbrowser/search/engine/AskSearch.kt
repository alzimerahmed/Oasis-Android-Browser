package com.alzimerahmed.oasisbrowser.search.engine

import com.alzimerahmed.oasisbrowser.R

/**
 * The Ask search engine.
 */
class AskSearch : BaseSearchEngine(
    "file:///android_asset/ask.png",
    "https://www.ask.com/web?qsrc=0&o=0&l=dir&qo=OasisBrowserBrowser&q=",
    R.string.search_engine_ask
)
