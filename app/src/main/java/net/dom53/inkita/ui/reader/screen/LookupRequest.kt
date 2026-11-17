package net.dom53.inkita.ui.reader.screen

/** A word the reader selected, and the language of the book it came from (if the book says). */
internal data class LookupRequest(
    val text: String,
    val language: String?,
)
