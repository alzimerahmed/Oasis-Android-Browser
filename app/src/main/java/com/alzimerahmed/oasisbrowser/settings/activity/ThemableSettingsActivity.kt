package com.alzimerahmed.oasisbrowser.settings.activity

import com.alzimerahmed.oasisbrowser.AppTheme
import com.alzimerahmed.oasisbrowser.AccentPalette
import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.browser.di.injector
import com.alzimerahmed.oasisbrowser.preference.UserPreferences
import com.alzimerahmed.oasisbrowser.i18n.TranslationOverrides
import com.alzimerahmed.oasisbrowser.utils.ThemeUtils
import com.alzimerahmed.oasisbrowser.utils.CustomFontManager
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import javax.inject.Inject

abstract class ThemableSettingsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(TranslationOverrides.wrap(newBase))
    }

    private var themeId: AppTheme = AppTheme.LIGHT
    private var appliedSystemAccent: Int? = null

    @Inject internal lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        themeId = userPreferences.useTheme
        val effectiveTheme = themeId.effective(this)

        // set the theme
        when (effectiveTheme) {
            AppTheme.LIGHT -> {
                setTheme(R.style.Theme_SettingsTheme)
                window.setBackgroundDrawable(ThemeUtils.getPrimaryColor(this).toDrawable())
            }

            AppTheme.DARK -> {
                setTheme(R.style.Theme_SettingsTheme_Dark)
                window.setBackgroundDrawable(ThemeUtils.getPrimaryColorDark(this).toDrawable())
            }

            AppTheme.BLACK -> {
                setTheme(R.style.Theme_SettingsTheme_Black)
                window.setBackgroundDrawable(ThemeUtils.getPrimaryColorDark(this).toDrawable())
            }

            AppTheme.SYSTEM -> error("System theme must be resolved before applying it")
        }
        theme.applyStyle(
            AccentPalette.overlayFor(
                effectiveTheme,
                userPreferences.accentPalette,
                userPreferences.matchSystemAccent
            ),
            true
        )
        appliedSystemAccent = AccentPalette.systemAccentFingerprint(this)
        super.onCreate(savedInstanceState)

        resetPreferences()
        CustomFontManager.applyToViewTree(window.decorView, userPreferences.customFontPath)
    }

    private fun resetPreferences() {
        if (userPreferences.useBlackStatusBar) {
            window.statusBarColor = Color.BLACK
        } else {
            window.statusBarColor = ThemeUtils.getStatusBarColor(this)
        }
    }

    override fun onResume() {
        super.onResume()
        val systemAccent = AccentPalette.systemAccentFingerprint(this)
        if (userPreferences.matchSystemAccent && systemAccent != appliedSystemAccent) {
            recreate()
            return
        }
        resetPreferences()
        CustomFontManager.applyToViewTree(window.decorView, userPreferences.customFontPath)
        if (userPreferences.useTheme != themeId) {
            recreate()
        }
    }

}
