package com.alzimerahmed.oasisbrowser.settings.fragment

import com.alzimerahmed.oasisbrowser.R
import android.os.Bundle

/**
 * The root settings list.
 */
class RootSettingsFragment : AbstractSettingsFragment() {
    override fun providePreferencesXmlResource(): Int = R.xml.preference_root

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
    }
}
