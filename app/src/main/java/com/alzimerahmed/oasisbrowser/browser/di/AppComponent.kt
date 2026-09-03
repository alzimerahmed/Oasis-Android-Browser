package com.alzimerahmed.oasisbrowser.browser.di

import com.alzimerahmed.oasisbrowser.BrowserApp
import com.alzimerahmed.oasisbrowser.ThemableBrowserActivity
import com.alzimerahmed.oasisbrowser.adblock.BloomFilterAdBlocker
import com.alzimerahmed.oasisbrowser.adblock.NoOpAdBlocker
import com.alzimerahmed.oasisbrowser.browser.search.SearchBoxModel
import com.alzimerahmed.oasisbrowser.device.BuildInfo
import com.alzimerahmed.oasisbrowser.dialog.OasisBrowserDialogBuilder
import com.alzimerahmed.oasisbrowser.search.SuggestionsAdapter
import com.alzimerahmed.oasisbrowser.settings.activity.ThemableSettingsActivity
import com.alzimerahmed.oasisbrowser.settings.activity.UserScriptEditorActivity
import com.alzimerahmed.oasisbrowser.settings.fragment.HapticsSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.AccessibilitySettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.AdBlockSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.AdvancedSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.AudioSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.BookmarkSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.DebugSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.DisplaySettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.GeneralSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.AboutSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.PrivacySettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.RootSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.SitePermissionsSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.UserScriptsSettingsFragment
import com.alzimerahmed.oasisbrowser.settings.fragment.VirusTotalSettingsFragment
import com.alzimerahmed.oasisbrowser.qr.QrScannerActivity
import com.alzimerahmed.oasisbrowser.vault.VaultActivity
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, AppBindsModule::class, Submodules::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun buildInfo(buildInfo: BuildInfo): Builder

        fun build(): AppComponent
    }

    fun inject(fragment: BookmarkSettingsFragment)

    fun inject(builder: OasisBrowserDialogBuilder)

    fun inject(activity: ThemableBrowserActivity)

    fun inject(advancedSettingsFragment: AdvancedSettingsFragment)

    fun inject(audioSettingsFragment: AudioSettingsFragment)

    fun inject(app: BrowserApp)

    fun inject(activity: ThemableSettingsActivity)

    fun inject(activity: UserScriptEditorActivity)

    fun inject(activity: QrScannerActivity)

    fun inject(activity: VaultActivity)

    fun inject(fragment: HapticsSettingsFragment)

    fun inject(fragment: PrivacySettingsFragment)

    fun inject(fragment: SitePermissionsSettingsFragment)

    fun inject(fragment: DebugSettingsFragment)

    fun inject(suggestionsAdapter: SuggestionsAdapter)

    fun inject(searchBoxModel: SearchBoxModel)

    fun inject(activity: RootSettingsFragment)

    fun inject(generalSettingsFragment: GeneralSettingsFragment)

    fun inject(aboutSettingsFragment: AboutSettingsFragment)

    fun inject(displaySettingsFragment: DisplaySettingsFragment)

    fun inject(adBlockSettingsFragment: AdBlockSettingsFragment)

    fun inject(accessibilitySettingsFragment: AccessibilitySettingsFragment)

    fun inject(userScriptsSettingsFragment: UserScriptsSettingsFragment)

    fun inject(virusTotalSettingsFragment: VirusTotalSettingsFragment)

    fun provideBloomFilterAdBlocker(): BloomFilterAdBlocker

    fun provideNoOpAdBlocker(): NoOpAdBlocker

    fun browser2ComponentBuilder(): Browser2Component.Builder

}

@Module(subcomponents = [Browser2Component::class])
internal class Submodules
