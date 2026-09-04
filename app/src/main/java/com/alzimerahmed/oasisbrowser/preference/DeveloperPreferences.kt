package com.alzimerahmed.oasisbrowser.preference

import com.alzimerahmed.oasisbrowser.browser.di.DevPrefs
import com.alzimerahmed.oasisbrowser.preference.delegates.booleanPreference
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferences related to development debugging.
 *
 * Created by anthonycr on 2/19/18.
 */
@Singleton
class DeveloperPreferences @Inject constructor(
    @DevPrefs preferences: SharedPreferences
) {

    var experimentalRailLayoutsEnabled by preferences.booleanPreference(
        EXPERIMENTAL_RAIL_LAYOUTS,
        false
    )

    /**
     * Runs an off-screen WebView beside Antares so debug builds can compare the semantic target
     * under a tap before forwarding it to the experimental engine.
     */
    var antaresCoordinateBridgeEnabled by preferences.booleanPreference(
        ANTARES_COORDINATE_BRIDGE,
        false,
    )

    var checkedForTor by preferences.booleanPreference(INITIAL_CHECK_FOR_TOR, false)

    // var checkedForI2P by preferences.booleanPreference(INITIAL_CHECK_FOR_I2P, false)
}

private const val EXPERIMENTAL_RAIL_LAYOUTS = "experimentalRailLayouts"
private const val ANTARES_COORDINATE_BRIDGE = "antaresCoordinateBridge"
private const val INITIAL_CHECK_FOR_TOR = "checkForTor"
// private const val INITIAL_CHECK_FOR_I2P = "checkForI2P"
