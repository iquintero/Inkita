package com.iquintero.kavern

import com.iquintero.kavern.core.dictionary.WiktionaryClient
import com.iquintero.kavern.core.dictionary.WiktionaryParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WiktionaryParserTest {
    private val json =
        """
        {
          "en": [
            {"partOfSpeech": "Symbol", "language": "Translingual",
             "definitions": [{"definition": "<i>ISO code</i> for <b>Riantana</b>"}]},
            {"partOfSpeech": "Verb", "language": "English",
             "definitions": [
               {"definition": "<span>simple past of <a href=\"/wiki/run\">run</a></span>",
                "examples": ["He <b>ran</b> home.", "<i>She ran away.</i>", "A third example"]},
               {"definition": "   "}
             ]}
          ],
          "cs": [
            {"partOfSpeech": "Noun", "language": "Czech",
             "definitions": [{"definition": "book &amp; volume"}]}
          ]
        }
        """.trimIndent()

    @Test
    fun picksTheBookLanguageAndStripsHtml() {
        val entries = WiktionaryParser.parse(json, listOf("cs-CZ"))
        assertEquals(1, entries.size)
        assertEquals("Czech", entries[0].language)
        assertEquals("book & volume", entries[0].definitions[0].text)
    }

    @Test
    fun fallsBackToTheNextPreferredLanguageThenTheFirstKey() {
        assertEquals("Czech", WiktionaryParser.parse(json, listOf("de", "cs")).first().language)
        assertEquals("English", WiktionaryParser.parse(json, listOf("de")).first().language)
    }

    @Test
    fun putsTranslingualLastDropsBlankDefinitionsAndLimitsExamples() {
        val entries = WiktionaryParser.parse(json, listOf("en"))
        assertEquals(listOf("English", "Translingual"), entries.map { it.language })
        val definitions = entries[0].definitions
        assertEquals(1, definitions.size)
        assertEquals("simple past of run", definitions[0].text)
        assertEquals(listOf("He ran home.", "She ran away."), definitions[0].examples)
    }

    @Test
    fun invalidOrEmptyJsonGivesNoEntries() {
        assertTrue(WiktionaryParser.parse("not json", listOf("en")).isEmpty())
        assertTrue(WiktionaryParser.parse("{}", listOf("en")).isEmpty())
    }

    @Test
    fun normalizesLanguageTags() {
        assertEquals("cs", WiktionaryParser.normalizeLanguage("cs-CZ"))
        assertEquals("en", WiktionaryParser.normalizeLanguage("en_US"))
        assertEquals("cs", WiktionaryParser.normalizeLanguage("ces"))
        assertEquals("en", WiktionaryParser.normalizeLanguage("eng"))
        assertEquals(null, WiktionaryParser.normalizeLanguage(""))
        assertEquals(null, WiktionaryParser.normalizeLanguage(null))
    }

    @Test
    fun cleansSelectionsDraggedAlongWithPunctuation() {
        assertEquals("word", WiktionaryClient.cleanWord("  “word,” "))
        assertEquals("ice cream", WiktionaryClient.cleanWord("\n ice  cream. "))
        assertEquals("", WiktionaryClient.cleanWord("…!?"))
    }
}
