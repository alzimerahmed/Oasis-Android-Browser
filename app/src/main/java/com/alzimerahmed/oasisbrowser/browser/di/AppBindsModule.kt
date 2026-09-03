package com.alzimerahmed.oasisbrowser.browser.di

import com.alzimerahmed.oasisbrowser.adblock.allowlist.AllowListModel
import com.alzimerahmed.oasisbrowser.adblock.allowlist.SessionAllowListModel
import com.alzimerahmed.oasisbrowser.adblock.source.AssetsHostsDataSource
import com.alzimerahmed.oasisbrowser.adblock.source.HostsDataSource
import com.alzimerahmed.oasisbrowser.adblock.source.HostsDataSourceProvider
import com.alzimerahmed.oasisbrowser.adblock.source.PreferencesHostsDataSourceProvider
import com.alzimerahmed.oasisbrowser.database.adblock.HostsDatabase
import com.alzimerahmed.oasisbrowser.database.adblock.HostsRepository
import com.alzimerahmed.oasisbrowser.database.allowlist.AdBlockAllowListDatabase
import com.alzimerahmed.oasisbrowser.database.allowlist.AdBlockAllowListRepository
import com.alzimerahmed.oasisbrowser.database.bookmark.BookmarkDatabase
import com.alzimerahmed.oasisbrowser.database.bookmark.BookmarkRepository
import com.alzimerahmed.oasisbrowser.database.downloads.DownloadsDatabase
import com.alzimerahmed.oasisbrowser.database.downloads.DownloadsRepository
import com.alzimerahmed.oasisbrowser.database.history.HistoryDatabase
import com.alzimerahmed.oasisbrowser.database.history.HistoryRepository
import com.alzimerahmed.oasisbrowser.database.readinglist.ReadingListDatabase
import com.alzimerahmed.oasisbrowser.database.readinglist.ReadingListRepository
import com.alzimerahmed.oasisbrowser.database.vault.VaultDatabase
import com.alzimerahmed.oasisbrowser.database.vault.VaultRepository
import com.alzimerahmed.oasisbrowser.ssl.SessionSslWarningPreferences
import com.alzimerahmed.oasisbrowser.ssl.SslWarningPreferences
import dagger.Binds
import dagger.Module

/**
 * Dependency injection module used to bind implementations to interfaces.
 */
@Module
interface AppBindsModule {

    @Binds
    fun bindsBookmarkModel(bookmarkDatabase: BookmarkDatabase): BookmarkRepository

    @Binds
    fun bindsDownloadsModel(downloadsDatabase: DownloadsDatabase): DownloadsRepository

    @Binds
    fun bindsHistoryModel(historyDatabase: HistoryDatabase): HistoryRepository

    @Binds
    fun bindsReadingListRepository(readingListDatabase: ReadingListDatabase): ReadingListRepository

    @Binds
    fun bindsVaultRepository(vaultDatabase: VaultDatabase): VaultRepository

    @Binds
    fun bindsAdBlockAllowListModel(adBlockAllowListDatabase: AdBlockAllowListDatabase): AdBlockAllowListRepository

    @Binds
    fun bindsAllowListModel(sessionAllowListModel: SessionAllowListModel): AllowListModel

    @Binds
    fun bindsSslWarningPreferences(sessionSslWarningPreferences: SessionSslWarningPreferences): SslWarningPreferences

    @Binds
    fun bindsHostsDataSource(assetsHostsDataSource: AssetsHostsDataSource): HostsDataSource

    @Binds
    fun bindsHostsRepository(hostsDatabase: HostsDatabase): HostsRepository

    @Binds
    fun bindsHostsDataSourceProvider(preferencesHostsDataSourceProvider: PreferencesHostsDataSourceProvider): HostsDataSourceProvider
}
