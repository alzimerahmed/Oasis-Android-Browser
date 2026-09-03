package com.alzimerahmed.oasisbrowser.js

import com.anthonycr.mezzanine.FileStream

/**
 * Attaches double-tap fullscreen and swipe gesture handlers to HTML5 video elements.
 */
@FileStream("src/main/js/VideoGestures.js")
interface VideoGestures {

    fun provideJs(): String

}
