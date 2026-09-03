package com.alzimerahmed.oasisbrowser.search

import android.app.Application
import android.content.Context
import com.alzimerahmed.oasisbrowser.device.ScreenSize
import com.alzimerahmed.oasisbrowser.log.Logger
import com.alzimerahmed.oasisbrowser.preference.UserPreferences
import com.alzimerahmed.oasisbrowser.search.engine.DuckSearch
import com.alzimerahmed.oasisbrowser.search.suggestions.RequestFactory
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SearchDefaultsTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `fresh installation uses DuckDuckGo for searches and suggestions`() {
        val preferences = context.getSharedPreferences("search_defaults_test", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()

        val userPreferences = UserPreferences(preferences, ScreenSize(context))
        val provider = SearchEngineProvider(
            userPreferences = userPreferences,
            okHttpClient = Single.just(mock(OkHttpClient::class.java)),
            requestFactory = mock(RequestFactory::class.java),
            application = context.applicationContext as Application,
            logger = mock(Logger::class.java)
        )

        assertThat(userPreferences.searchChoice)
            .isEqualTo(SearchEngineProvider.DEFAULT_SEARCH_ENGINE_INDEX)
        assertThat(userPreferences.searchUrl).isEqualTo(DuckSearch().queryUrl)
        assertThat(userPreferences.searchSuggestionChoice).isEqualTo(Suggestions.DUCK.index)
        assertThat(provider.provideSearchEngine()).isInstanceOf(DuckSearch::class.java)
    }

    @Test
    fun `invalid suggestion preference falls back to DuckDuckGo`() {
        assertThat(Suggestions.from(Int.MAX_VALUE)).isEqualTo(Suggestions.DUCK)
    }
}
