package com.example.projektworldwisdom.mockApi

import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Image
import com.example.projektworldwisdom.model.Quote

object MockApi {
    private val images = listOf(
        "https://example.com/image1.jpg",
        "https://example.com/image2.jpg",
        "https://example.com/image3.jpg",
        "https://example.com/image4.jpg",
        "https://example.com/image5.jpg"
    )

    fun getAuthorImage(authorName: String): String? {
        return getAllQuotes().firstOrNull { it.authorName.equals(authorName, ignoreCase = true) }?.authorImageUrl
    }

    fun getRandomInspirationalImage(): String {
        val randomIndex = (0 until images.size).random()
        return images[randomIndex]
    }

    fun getAllQuotes(): List<Quote> {
        return listOf(
            Quote(content = "Das Leben ist wie ein Fahrrad. Man muss sich vorwärts bewegen, um das Gleichgewicht nicht zu verlieren.", authorName = "Albert Einstein", keywords = listOf("Leben", "Fahrrad")),
            Quote(content = "Der einzige Weg, großartige Arbeit zu leisten, ist zu lieben, was man tut.", authorName = "Steve Jobs", keywords = listOf("Arbeit", "Leidenschaft")),
            Quote(content = "Die beste Zeit für einen Neuanfang ist jetzt.", authorName = "Unbekannt", keywords = listOf("Neuanfang", "Jetzt")),
            Quote(content = "Wer kämpft, kann verlieren. Wer nicht kämpft, hat schon verloren.", authorName = "Bertolt Brecht", keywords = listOf("Kampf", "Verlust")),
            Quote(content = "Zufriedenheit ist der wahre Reichtum.", authorName = "Platon", keywords = listOf("Zufriedenheit", "Reichtum")),
            Quote(content = "Das Glück ist das einzige, was wir anderen geben können, ohne es selbst zu haben.", authorName = "Ricarda Huch", keywords = listOf("Glück", "Teilen")),
            Quote(content = "Die Zukunft gehört denen, die an die Schönheit ihrer Träume glauben.", authorName = "Eleanor Roosevelt", keywords = listOf("Zukunft", "Träume"))
        )
    }

    fun getAllAuthors(): List<Author> {
        return listOf(
            Author(id = 1, name = "Albert Einstein", tag = "Wissenschaftler", link = "https://de.wikipedia.org/wiki/Albert_Einstein", imageUrl = "image_url_einstein"),
            Author(id = 2, name = "Steve Jobs", tag = "Unternehmer", link = "https://de.wikipedia.org/wiki/Steve_Jobs", imageUrl = "image_url_jobs"),
            Author(id = 3, name = "Bertolt Brecht", tag = "Dramatiker", link = "https://de.wikipedia.org/wiki/Bertolt_Brecht", imageUrl = "image_url_brecht"),
            // Füge weitere Autoren hinzu
        )
    }

    fun getQuoteOfTheDay(): List<Quote> {
        return listOf(
            Quote(content = "Das Leben ist wie ein Fahrrad. Man muss sich vorwärts bewegen, um das Gleichgewicht nicht zu verlieren.", authorName = "Albert Einstein", isQuoteOfTheDay = true),
            Quote(content = "Der beste Weg, die Zukunft vorauszusagen, ist, sie zu gestalten.", authorName = "Peter Drucker", isQuoteOfTheDay = true)

        )

    }

    fun getRandomQuote(): Quote {
        val allQuotes = getAllQuotes()
        val randomIndex = (0 until allQuotes.size).random()
        return allQuotes[randomIndex]
    }
    // Liefert eine Liste von Zitaten eines bestimmten Autors
    fun getQuotesByAuthor(authorName: String): List<Quote> {
        return getAllQuotes().filter { it.authorName.equals(authorName, ignoreCase = true) }
    }

    fun filterQuotesByKeywords(keywords: List<String>): List<Quote> {
        return getAllQuotes().filter { quote ->
            keywords.any { keyword ->
                quote.content?.contains(keyword, ignoreCase = true) == true
            }
        }
    }
    // Generiert ein Zitatbild basierend auf dem angegebenen Schlüsselwort
    fun getImageByKeyword(keyword: String): Image? {
        return when (keyword.lowercase()) {
            "inspiration" -> Image("https://example.com/images/inspiration.jpg")
            "motivation" -> Image("https://example.com/images/motivation.jpg")
            "happiness" -> Image("https://example.com/images/happiness.jpg")
            "success" -> Image("https://example.com/images/success.jpg")
            "love" -> Image("https://example.com/images/love.jpg")
            // Füge hier weitere Keywords und die entsprechenden Bild-URLs hinzu
            else -> null // Wenn kein passendes Bild gefunden wird
        }
    }


}