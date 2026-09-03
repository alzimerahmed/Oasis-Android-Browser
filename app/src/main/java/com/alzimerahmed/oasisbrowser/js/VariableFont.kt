package com.alzimerahmed.oasisbrowser.js

import com.anthonycr.mezzanine.FileStream

/**
 * Replaces page fonts with the bundled variable Google Sans Flex font.
 */
@FileStream("src/main/js/VariableFont.js")
interface VariableFont {

    fun provideJs(): String

}
