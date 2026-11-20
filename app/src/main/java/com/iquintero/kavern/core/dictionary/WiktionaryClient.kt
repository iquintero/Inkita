package com.iquintero.kavern.core.dictionary

import com.iquintero.kavern.BuildConfig
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DictionaryDefinition(
    val text: String,
    val examples: List<String>,
)

data class DictionaryEntry(
    val partOfSpeech: String,
    val language: String,
    val definitions: List<DictionaryDefinition>,
)

sealed interface DefinitionResult {
    data class Found(
        val word: String,
        val entries: List<DictionaryEntry>,
    ) : DefinitionResult

    data class NotFound(
        val word: String,
    ) : DefinitionResult

    data class Failed(
        val word: String,
    ) : DefinitionResult
}

/** Turns Wiktionary's REST `page/definition` JSON into plain-text entries. No Android dependencies. */
object WiktionaryParser {
    private const val MAX_EXAMPLES = 2
    private const val TRANSLINGUAL = "Translingual"

    private val rootAdapter by lazy {
        Moshi
            .Builder()
            .build()
            .adapter<Map<String, Any?>>(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))
    }

    /**
     * The response maps a language code to the entries for that language. Pick the first of
     * [preferredLanguages] that is present, otherwise the only/first language in the response.
     */
    fun parse(
        json: String,
        preferredLanguages: List<String>,
    ): List<DictionaryEntry> {
        val root =
            try {
                rootAdapter.fromJson(json)
            } catch (_: IOException) {
                null
            } catch (_: JsonDataException) {
                null
            }
        if (root.isNullOrEmpty()) return emptyList()
        val key =
            preferredLanguages
                .mapNotNull { normalizeLanguage(it) }
                .firstOrNull { it in root }
                ?: root.keys.first()
        return (root[key] as? List<*>)
            .orEmpty()
            .mapNotNull { it as? Map<*, *> }
            .mapNotNull { entry ->
                val definitions =
                    (entry["definitions"] as? List<*>)
                        .orEmpty()
                        .mapNotNull { it as? Map<*, *> }
                        .mapNotNull { definition ->
                            val text = htmlToText(definition["definition"] as? String ?: "")
                            if (text.isBlank()) {
                                null
                            } else {
                                val examples =
                                    (definition["examples"] as? List<*>)
                                        .orEmpty()
                                        .mapNotNull { it as? String }
                                        .map { htmlToText(it) }
                                        .filter { it.isNotBlank() }
                                        .take(MAX_EXAMPLES)
                                DictionaryDefinition(text, examples)
                            }
                        }
                if (definitions.isEmpty()) {
                    null
                } else {
                    DictionaryEntry(
                        partOfSpeech = entry["partOfSpeech"] as? String ?: "",
                        language = entry["language"] as? String ?: "",
                        definitions = definitions,
                    )
                }
            }
            // Translingual entries (symbols, ISO codes) are rarely what a reader wants first.
            .sortedBy { it.language == TRANSLINGUAL }
    }

    internal fun htmlToText(html: String): String = Jsoup.parse(html).text().trim()

    /** "cs-CZ" -> "cs"; ISO 639-2 codes such as "ces" or "eng" map to their two-letter code. */
    fun normalizeLanguage(tag: String?): String? {
        val primary =
            tag
                ?.trim()
                ?.lowercase(Locale.ROOT)
                ?.split('-', '_')
                ?.firstOrNull()
                ?.takeIf { it.isNotEmpty() }
                ?: return null
        return if (primary.length == 3) iso3ToIso2[primary] ?: primary else primary
    }

    private val iso3ToIso2: Map<String, String> by lazy {
        Locale
            .getISOLanguages()
            .mapNotNull { two ->
                runCatching { Locale.forLanguageTag(two).isO3Language to two }.getOrNull()
            }.toMap()
    }
}

/**
 * Looks words up on English Wiktionary, which describes words of many languages in English.
 * The selected word is sent to wiktionary.org; nothing else about the reader or the book is.
 */
class WiktionaryClient(
    private val client: OkHttpClient =
        OkHttpClient
            .Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build(),
) {
    suspend fun define(
        word: String,
        preferredLanguages: List<String>,
    ): DefinitionResult =
        withContext(Dispatchers.IO) {
            val cleaned = cleanWord(word)
            if (cleaned.isEmpty()) return@withContext DefinitionResult.NotFound(word)
            var failed = false
            for (candidate in listOf(cleaned, cleaned.lowercase()).distinct()) {
                when (val outcome = fetch(candidate, preferredLanguages)) {
                    is Fetch.Entries -> return@withContext DefinitionResult.Found(candidate, outcome.entries)
                    Fetch.Missing -> Unit
                    Fetch.Error -> failed = true
                }
            }
            if (failed) DefinitionResult.Failed(cleaned) else DefinitionResult.NotFound(cleaned)
        }

    private sealed interface Fetch {
        data class Entries(
            val entries: List<DictionaryEntry>,
        ) : Fetch

        data object Missing : Fetch

        data object Error : Fetch
    }

    private fun fetch(
        word: String,
        preferredLanguages: List<String>,
    ): Fetch {
        val url =
            HttpUrl
                .Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegments("api/rest_v1/page/definition")
                .addPathSegment(word)
                .build()
        val request =
            Request
                .Builder()
                .url(url)
                .header("User-Agent", "Kavern/${BuildConfig.VERSION_NAME} (Android e-reader dictionary lookup)")
                .header("Accept", "application/json")
                .build()
        return try {
            client.newCall(request).execute().use { response -> toFetch(response, preferredLanguages) }
        } catch (_: IOException) {
            Fetch.Error
        }
    }

    private fun toFetch(
        response: Response,
        preferredLanguages: List<String>,
    ): Fetch =
        when {
            response.code == 404 -> Fetch.Missing
            !response.isSuccessful -> Fetch.Error
            else ->
                WiktionaryParser
                    .parse(response.body?.string().orEmpty(), preferredLanguages)
                    .let { if (it.isEmpty()) Fetch.Missing else Fetch.Entries(it) }
        }

    companion object {
        private const val HOST = "en.wiktionary.org"
        private const val MAX_WORD_LENGTH = 100

        // Books often use non-breaking spaces, which plain \\s does not match.
        private val WHITESPACE = Regex("[\\s\\p{Z}]+")
        private val EDGE_PUNCTUATION = Regex("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$")

        /** Trims the punctuation and whitespace a selection often drags along ("word," or "“word”"). */
        fun cleanWord(selection: String): String =
            selection
                .replace(WHITESPACE, " ")
                .trim()
                .replace(EDGE_PUNCTUATION, "")
                .take(MAX_WORD_LENGTH)

        fun pageUrl(word: String): String =
            HttpUrl
                .Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment("wiki")
                .addPathSegment(word)
                .build()
                .toString()
    }
}
