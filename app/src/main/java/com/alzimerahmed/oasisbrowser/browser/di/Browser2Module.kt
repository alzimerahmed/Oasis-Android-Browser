package com.alzimerahmed.oasisbrowser.browser.di

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.adblock.AdBlocker
import com.alzimerahmed.oasisbrowser.adblock.PreferenceAwareAdBlocker
import com.alzimerahmed.oasisbrowser.browser.BrowserContract
import com.alzimerahmed.oasisbrowser.browser.data.CookieAdministrator
import com.alzimerahmed.oasisbrowser.browser.data.DefaultCookieAdministrator
import com.alzimerahmed.oasisbrowser.browser.history.DefaultHistoryRecord
import com.alzimerahmed.oasisbrowser.browser.history.HistoryRecord
import com.alzimerahmed.oasisbrowser.browser.history.NoOpHistoryRecord
import com.alzimerahmed.oasisbrowser.browser.image.IconFreeze
import com.alzimerahmed.oasisbrowser.browser.notification.DefaultTabCountNotifier
import com.alzimerahmed.oasisbrowser.browser.notification.IncognitoTabCountNotifier
import com.alzimerahmed.oasisbrowser.browser.notification.TabCountNotifier
import com.alzimerahmed.oasisbrowser.browser.search.IntentExtractor
import com.alzimerahmed.oasisbrowser.browser.engine.OnboardingStarterTabs
import com.alzimerahmed.oasisbrowser.browser.tab.DefaultUserAgent
import com.alzimerahmed.oasisbrowser.browser.tab.bundle.BundleStore
import com.alzimerahmed.oasisbrowser.browser.tab.bundle.DefaultBundleStore
import com.alzimerahmed.oasisbrowser.browser.tab.bundle.IncognitoBundleStore
import com.alzimerahmed.oasisbrowser.browser.ui.BookmarkConfiguration
import com.alzimerahmed.oasisbrowser.browser.ui.TabConfiguration
import com.alzimerahmed.oasisbrowser.browser.ui.UiConfiguration
import com.alzimerahmed.oasisbrowser.extensions.drawable
import com.alzimerahmed.oasisbrowser.utils.IntentUtils
import com.alzimerahmed.oasisbrowser.utils.NavigationSecurity
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.webkit.WebSettings
import androidx.core.graphics.drawable.toBitmap
import dagger.Module
import dagger.Provides

/**
 * Constructs dependencies for the browser scope.
 */
@Module
class Browser2Module {

    @Provides
    fun providesAdBlocker(
        preferenceAwareAdBlocker: PreferenceAwareAdBlocker,
    ): AdBlocker = preferenceAwareAdBlocker

    @Provides
    @InitialUrls
    fun providesInitialUrls(
        @InitialIntent initialIntent: Intent?,
        intentExtractor: IntentExtractor,
    ): List<String> {
        val starterUrls = initialIntent
            ?.getStringArrayListExtra(OnboardingStarterTabs.EXTRA_URLS)
            ?.mapNotNull(NavigationSecurity::sanitizeUserInput)
            ?.filter(NavigationSecurity::isAllowedFromExternalIntent)
            .orEmpty()
        if (starterUrls.isNotEmpty()) return starterUrls
        return listOfNotNull(
            (intentExtractor.extractUrlFromIntent(initialIntent) as? BrowserContract.Action.LoadUrl)?.url,
        )
    }

    // TODO: auto inject intent utils
    @Provides
    fun providesIntentUtils(activity: Activity): IntentUtils = IntentUtils(activity)

    @Provides
    fun providesUiConfiguration(): UiConfiguration = UiConfiguration(
        tabConfiguration = TabConfiguration.OasisBrowser,
        bookmarkConfiguration = BookmarkConfiguration.RIGHT
    )

    @DefaultUserAgent
    @Provides
    fun providesDefaultUserAgent(application: Application): String =
        WebSettings.getDefaultUserAgent(application)


    @Provides
    fun providesHistoryRecord(
        @IncognitoMode incognitoMode: Boolean,
        defaultHistoryRecord: DefaultHistoryRecord
    ): HistoryRecord = if (incognitoMode) {
        NoOpHistoryRecord
    } else {
        defaultHistoryRecord
    }

    @Provides
    fun providesCookieAdministrator(
        @IncognitoMode incognitoMode: Boolean,
        defaultCookieAdministrator: DefaultCookieAdministrator,
        incognitoCookieAdministrator: DefaultCookieAdministrator
    ): CookieAdministrator = if (incognitoMode) {
        incognitoCookieAdministrator
    } else {
        defaultCookieAdministrator
    }

    @Provides
    fun providesTabCountNotifier(
        @IncognitoMode incognitoMode: Boolean,
        incognitoTabCountNotifier: IncognitoTabCountNotifier
    ): TabCountNotifier = if (incognitoMode) {
        incognitoTabCountNotifier
    } else {
        DefaultTabCountNotifier
    }

    @Provides
    fun providesBundleStore(
        @IncognitoMode incognitoMode: Boolean,
        defaultBundleStore: DefaultBundleStore
    ): BundleStore = if (incognitoMode) {
        IncognitoBundleStore
    } else {
        defaultBundleStore
    }

    @IconFreeze
    @Provides
    fun providesFrozenIcon(activity: Activity): Bitmap =
        activity.drawable(R.drawable.ic_frozen).toBitmap()

}
