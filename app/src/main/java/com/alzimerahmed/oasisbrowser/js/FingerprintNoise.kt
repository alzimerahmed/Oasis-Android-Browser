package com.alzimerahmed.oasisbrowser.js

import com.anthonycr.mezzanine.FileStream

/**
 * Injects canvas/WebGL noise to reduce fingerprinting surface.
 */
@FileStream("src/main/js/FingerprintNoise.js")
interface FingerprintNoise {

    fun provideJs(): String

}
