package com.example.projektworldwisdom.model

data class Quote(
    val id: String = "",
    val author: String = "",
    val quote: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),

    /**
     * UI-State: wird NICHT aus der API „wahr“ sein müssen.
     * Wir setzen es über Favoriten-Storage (DataStore) und `copy(isFavorite = ...)`.
     */
    val isFavorite: Boolean = false,

    // Optional / für spätere Erweiterungen (dürfen in der Mock-API fehlen)
    val description: String = "",
    val source: String = "",
    val authorImageURLs: List<String> = emptyList()
) {
    /**
     * Stabiler Key für Favoriten.
     * Primär: API-ID + authorSlug + quote.hashCode() (robust gegen doppelte IDs in Mock-Daten).
     * Fallback: authorSlug + quote.hashCode() (deterministisch).
     */
    val favoriteKey: String
        get() = if (id.isNotBlank()) "${id}_${authorSlug}_${quote.hashCode()}" else "${authorSlug}_${quote.hashCode()}"

    val authorSlug: String
        get() = author
            .trim()
            .lowercase()
            .replace("ö", "oe")
            .replace("ä", "ae")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("[^a-z0-9-]"), "")
            .replace(Regex("-{2,}"), "-")
            .trim('-')
}