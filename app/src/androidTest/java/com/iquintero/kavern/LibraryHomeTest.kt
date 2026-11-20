package com.iquintero.kavern

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.request.CachePolicy
import com.iquintero.kavern.core.cache.CacheManagerImpl
import com.iquintero.kavern.core.cache.LibraryV2CacheKeys
import com.iquintero.kavern.core.storage.AppConfig
import com.iquintero.kavern.core.storage.AppPreferences
import com.iquintero.kavern.data.api.dto.SeriesDto
import com.iquintero.kavern.data.local.db.KavernDatabase
import com.iquintero.kavern.data.local.db.entity.CachedSeriesListRefEntity
import com.iquintero.kavern.data.mapper.toDomain
import com.iquintero.kavern.data.repository.CollectionsRepositoryImpl
import com.iquintero.kavern.data.repository.PersonRepositoryImpl
import com.iquintero.kavern.data.repository.ReadingListRepositoryImpl
import com.iquintero.kavern.data.repository.SeriesRepositoryImpl
import com.iquintero.kavern.domain.model.Library
import com.iquintero.kavern.domain.repository.LibraryRepository
import com.iquintero.kavern.ui.library.LibraryV2Screen
import com.iquintero.kavern.ui.theme.KavernTheme
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class LibraryHomeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun emptyCacheShowsReadableErrorAndRetryRecovers() {
        val healthy = AtomicBoolean()
        withHome(response = { path ->
            when {
                !healthy.get() -> htmlResponse()
                path == "/api/Series/recently-added-v2" -> jsonResponse("""[{"id":2,"name":"Recovered series"}]""")
                else -> jsonResponse("[]")
            }
        }) {
            open()
            val error = label(R.string.library_home_load_failed)
            waitForText(error)
            compose.onNodeWithText("JsonReader.setLenient", substring = true).assertDoesNotExist()
            healthy.set(true)
            retry()
            waitForText("Recovered series")
            compose.onNodeWithText(error).assertDoesNotExist()
        }
    }

    @Test
    fun populatedCacheKeepsFailedSectionUntilServerReturnsSuccessfulEmptyList() {
        val healthy = AtomicBoolean()
        withHome(response = { path ->
            when (path) {
                "/api/Series/on-deck" -> if (healthy.get()) jsonResponse("[]") else htmlResponse()
                "/api/Series/recently-added-v2" -> jsonResponse("""[{"id":2,"name":"Fresh series"}]""")
                else -> jsonResponse("[]")
            }
        }) {
            seedOnDeck()
            open()
            val error = label(R.string.library_home_load_failed)
            waitForText(error)
            waitForText("Cached series")
            waitForText("Fresh series")
            assertEquals(listOf(1), cachedOnDeckIds())
            assertEquals(1L, cachedOnDeckUpdatedAt())

            healthy.set(true)
            retry()
            compose.waitUntil(timeoutMillis = 8000) {
                compose.onAllNodesWithText("Cached series").fetchSemanticsNodes().isEmpty()
            }
            compose.onNodeWithText("Cached series").assertDoesNotExist()
            compose.onNodeWithText(error).assertDoesNotExist()
            assertEquals(emptyList<Int>(), cachedOnDeckIds())
        }
    }

    private fun label(id: Int): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun retry() {
        compose.onNodeWithText(label(R.string.download_action_retry)).performScrollTo().performClick()
    }

    private fun waitForText(text: String) {
        compose.waitUntil(timeoutMillis = 8000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(text).performScrollTo().assertIsDisplayed()
    }

    private fun withHome(
        response: (String) -> MockResponse,
        block: HomeFixture.() -> Unit,
    ) {
        MockWebServer().use { server ->
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse = response(request.requestUrl?.encodedPath.orEmpty())
                }
            val fixture = HomeFixture(server)
            try {
                fixture.block()
            } finally {
                fixture.close()
            }
        }
    }

    private inner class HomeFixture(
        server: MockWebServer,
    ) {
        private val context = InstrumentationRegistry.getInstrumentation().targetContext
        private val preferences = AppPreferences(context)
        private val database = Room.inMemoryDatabaseBuilder(context, KavernDatabase::class.java).build()
        private val dao = database.libraryV2Dao()
        private val cache = CacheManagerImpl(preferences, libraryV2Dao = dao, thumbnailsDir = null)
        private val config = AppConfig(server.url("/").toString(), "test-key", "", 1)
        private val loader = ImageLoader.Builder(context).networkCachePolicy(CachePolicy.DISABLED).build()
        private val libraries =
            object : LibraryRepository {
                override suspend fun getLibraries(): List<Library> = emptyList()

                override suspend fun hasLibraryAccess(libraryId: Int): Boolean = false
            }

        fun seedOnDeck() =
            runBlocking {
                cache.cacheLibraryV2SeriesList(LibraryV2CacheKeys.HOME_ON_DECK, "", listOf(SeriesDto(1, "Cached series").toDomain()))
                dao.upsertSeriesRefs(listOf(CachedSeriesListRefEntity(LibraryV2CacheKeys.HOME_ON_DECK, "", 1, 0, 1L)))
                assertEquals(listOf(1), cachedOnDeckIds())
            }

        fun cachedOnDeckIds(): List<Int> =
            runBlocking {
                cache.getCachedLibraryV2SeriesList(LibraryV2CacheKeys.HOME_ON_DECK, "").map { it.id }
            }

        fun cachedOnDeckUpdatedAt(): Long? =
            runBlocking {
                cache.getLibraryV2SeriesListUpdatedAt(LibraryV2CacheKeys.HOME_ON_DECK, "")
            }

        @Suppress("DEPRECATION")
        fun open() {
            compose.setContent {
                CompositionLocalProvider(LocalImageLoader provides loader) {
                    KavernTheme(darkTheme = true, dynamicColor = false) {
                        LibraryV2Screen(
                            libraryRepository = libraries,
                            seriesRepository = SeriesRepositoryImpl(context, flowOf(config), cache),
                            collectionsRepository = CollectionsRepositoryImpl(context, preferences),
                            readingListRepository = ReadingListRepositoryImpl(context, preferences),
                            personRepository = PersonRepositoryImpl(context, preferences),
                            cacheManager = cache,
                            appPreferences = preferences,
                            onOpenSeries = {},
                        )
                    }
                }
            }
        }

        fun close() {
            loader.shutdown()
            database.close()
        }
    }

    private fun htmlResponse(): MockResponse =
        MockResponse()
            .addHeader("Content-Type", "text/html")
            .setBody("<html><body>Proxy sign-in</body></html>")

    private fun jsonResponse(body: String): MockResponse = MockResponse().addHeader("Content-Type", "application/json").setBody(body)
}
