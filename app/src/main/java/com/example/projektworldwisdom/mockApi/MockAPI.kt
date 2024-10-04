package com.example.projektworldwisdom.mockApi

import android.util.Log
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote

//object MockApi {
//
//
//    fun getAllQuotes(): List<Quote> {
//        return listOf(
//            Quote(content = "Das Leben ist wie ein Fahrrad. Man muss sich vorwärts bewegen, um das Gleichgewicht nicht zu verlieren.", authorName = "Albert Einstein", keywords = listOf("Leben", "Fahrrad")),
//            Quote(content = "Der einzige Weg, großartige Arbeit zu leisten, ist zu lieben, was man tut.", authorName = "Steve Jobs", keywords = listOf("Arbeit", "Leidenschaft")),
//            Quote(content = "Innovation unterscheidet zwischen einem Anführer und einem Folger.", authorName = "Steve Jobs", keywords = listOf("Innovation", "Führung")),
//            Quote(content = "Die beste Zeit für einen Neuanfang ist jetzt.", authorName = "Mahatma Gandhi", keywords = listOf("Neuanfang", "Jetzt")),
//            Quote(content = "Wer kämpft, kann verlieren. Wer nicht kämpft, hat schon verloren.", authorName = "Bertolt Brecht", keywords = listOf("Kampf", "Verlust")),
//            Quote(content = "Zufriedenheit ist der wahre Reichtum.", authorName = "Platon", keywords = listOf("Zufriedenheit", "Reichtum")),
//            Quote(content = "Das Glück ist das einzige, was wir anderen geben können, ohne es selbst zu haben.", authorName = "Ricarda Huch", keywords = listOf("Glück", "Teilen")),
//            Quote(content = "Die Zukunft gehört denen, die an die Schönheit ihrer Träume glauben.", authorName = "Eleanor Roosevelt", keywords = listOf("Zukunft", "Träume")),
//            Quote(content = "Die größte Ehre, die man einem Menschen antun kann, ist die, dass man zu ihm Vertrauen hat.", authorName = "Matthias Claudius", keywords = listOf("Ehre", "Vertrauen")),
//            Quote(content = "In der Mitte von Schwierigkeiten liegen Möglichkeiten.", authorName = "Albert Einstein", keywords = listOf("Schwierigkeiten", "Möglichkeiten")),
//            Quote(content = "Das, was du liebst, lass frei. Kommt es zurück, gehört es dir – für immer.", authorName = "Konfuzius", keywords = listOf("Liebe", "Freiheit")),
//            Quote(content = "Der Weg ist das Ziel.", authorName = "Konfuzius", keywords = listOf("Weg", "Ziel")),
//            Quote(content = "Was du nicht willst, das man dir tu, das füg auch keinem anderen zu.", authorName = "Konfuzius", keywords = listOf("Respekt", "Empathie")),
//            Quote(content = "Man kann nicht immer weiter gehen, manchmal muss man innehalten, um zu wissen, wo man steht.", authorName = "Friedrich Nietzsche", keywords = listOf("Reflexion", "Selbstbewusstsein")),
//            Quote(content = "Der erste Schritt zur Veränderung ist das Bewusstsein. Der zweite ist die Annahme.", authorName = "Nathaniel Branden", keywords = listOf("Veränderung", "Bewusstsein")),
//            Quote(content = "Jeder Tag ist eine Chance, das zu tun, was du möchtest.", authorName = "Mark Twain", keywords = listOf("Chance", "Möglichkeiten")),
//            Quote(content = "Das Leben ist ein Abenteuer. Wage es.", authorName = "Helen Keller", keywords = listOf("Abenteuer", "Mut")),
//            Quote(content = "Die einzige Grenze für unseren Realisierungsgrad der morgen ist unsere Zweifel an heute.", authorName = "Franklin D. Roosevelt", keywords = listOf("Zukunft", "Glaube")),
//            Quote(content = "Wenn du einen Traum hast, dann beschütze ihn. Die Leute, die nichts erreichen wollen, werden dir sagen, dass du es nicht kannst.", authorName = "Will Smith", keywords = listOf("Traum", "Glaube")),
//            Quote(content = "Dein Leben wird nicht besser, wenn du wartest, dass es besser wird. Du musst es selbst in die Hand nehmen.", authorName = "Tony Robbins", keywords = listOf("Veränderung", "Aktiv werden")),
//            Quote(content = "Die Gesellschaft ist der Spiegel unserer Taten.", authorName = "Mahatma Gandhi", keywords = listOf("Gesellschaft")),
//            Quote(content = "Erfolg ist nicht der Schlüssel zum Glück. Glück ist der Schlüssel zum Erfolg.", authorName = "Albert Schweitzer", keywords = listOf("Erfolg", "Glück")),
//            Quote(content = "Weisheit ist nicht das Produkt der Schulbildung, sondern der lebenslangen Versuche, sie zu erwerben.", authorName = "Albert Einstein", keywords = listOf("Weisheit")),
//            Quote(content = "Dankbarkeit ist der Weg zur Freude.", authorName = "Marcus Tullius Cicero", keywords = listOf("Dankbarkeit", "Freude")),
//            Quote(content = "Die Stärke einer Gesellschaft misst sich an der Stärke ihrer Schwächsten.", authorName = "Nelson Mandela", keywords = listOf("Gesellschaft", "Stärke")),
//            Quote(content = "Erfolg bedeutet, sich selbst treu zu bleiben, während man sich verändert.", authorName = "Johann Wolfgang von Goethe", keywords = listOf("Erfolg", "Veränderung")),
//            Quote(content = "Weisheit ist das Verständnis von Dingen, die uns helfen, das Leben besser zu leben.", authorName = "Buddha", keywords = listOf("Weisheit")),
//            Quote(content = "Dankbarkeit ist nicht nur die größte aller Tugenden, sondern auch die Mutter aller anderen.", authorName = "Cicero", keywords = listOf("Dankbarkeit", "Tugend")),
//            Quote(content = "Die Gesellschaft wird nicht durch die Anzahl der Reichen gemessen, sondern durch das Wohl der Schwachen.", authorName = "Martin Luther King Jr.", keywords = listOf("Gesellschaft", "Wohl")),
//            Quote(content = "Erfolg ist das Ergebnis harter Arbeit, Ausdauer und der Fähigkeit, Rückschläge zu überwinden.", authorName = "Henry Ford", keywords = listOf("Erfolg", "Ausdauer")),
//            Quote(content = "Weisheit beginnt mit Staunen.", authorName = "Sokrates", keywords = listOf("Weisheit", "Staunen"))
//
//        )
//    }

//    fun getAllAuthors(): List<Author> {
//        return listOf(
//            Author(id = 1, name = "Albert Einstein", tag = "Wissenschaftler", link = "https://de.wikipedia.org/wiki/Albert_Einstein", imageUrl = "image_url_einstein"),
//            Author(id = 2, name = "Steve Jobs", tag = "Unternehmer", link = "https://de.wikipedia.org/wiki/Steve_Jobs", imageUrl = "image_url_jobs"),
//            Author(id = 3, name = "Bertolt Brecht", tag = "Dramatiker", link = "https://de.wikipedia.org/wiki/Bertolt_Brecht", imageUrl = "image_url_brecht"),
//            Author(id = 4, name = "Platon", tag = "Philosoph", link = "https://de.wikipedia.org/wiki/Platon", imageUrl = "image_url_platon"),
//            Author(id = 5, name = "Mahatma Gandhi", tag = "Führer", link = "https://de.wikipedia.org/wiki/Mahatma_Gandhi", imageUrl = "image_url_gandhi"),
//            Author(id = 6, name = "Ricarda Huch", tag = "Schriftstellerin", link = "https://de.wikipedia.org/wiki/Ricarda_Huch", imageUrl = "image_url_huch"),
//            Author(id = 7, name = "Matthias Claudius", tag = "Dichter", link = "https://de.wikipedia.org/wiki/Matthias_Claudius", imageUrl = "image_url_claudius"),
//            Author(id = 8, name = "Konfuzius", tag = "Philosoph", link = "https://de.wikipedia.org/wiki/Konfuzius", imageUrl = "image_url_konfuzius"),
//            Author(id = 9, name = "Friedrich Nietzsche", tag = "Philosoph", link = "https://de.wikipedia.org/wiki/Friedrich_Nietzsche", imageUrl = "image_url_nietzsche"),
//            Author(id = 10, name = "Nathaniel Branden", tag = "Psychologe", link = "https://de.wikipedia.org/wiki/Nathaniel_Brand", imageUrl = "image_url_branden"),
//            Author(id = 11, name = "Mark Twain", tag = "Schriftsteller", link = "https://de.wikipedia.org/wiki/Mark_Twain", imageUrl = "image_url_twain"),
//            Author(id = 12, name = "Helen Keller", tag = "Aktivistin", link = "https://de.wikipedia.org/wiki/Helen_Keller", imageUrl = "image_url_keller"),
//            Author(id = 13, name = "Franklin D. Roosevelt", tag = "Politiker", link = "https://de.wikipedia.org/wiki/Franklin_D._Roosevelt", imageUrl = "image_url_roosevelt"),
//            Author(id = 14, name = "Will Smith", tag = "Schauspieler", link = "https://de.wikipedia.org/wiki/Will_Smith", imageUrl = "image_url_smith"),
//            Author(id = 15, name = "Tony Robbins", tag = "Motivationssprecher", link = "https://de.wikipedia.org/wiki/Tony_Robbins", imageUrl = "image_url_robbins"),
//            Author(id = 16, name = "Albert Schweitzer", tag = "Arzt und Philosoph", link = "https://de.wikipedia.org/wiki/Albert_Schweitzer", imageUrl = "image_url_schweitzer"),
//            Author(id = 17, name = "Marcus Tullius Cicero", tag = "Politiker und Philosoph", link = "https://de.wikipedia.org/wiki/Cicero", imageUrl = "image_url_cicero"),
//            Author(id = 18, name = "Nelson Mandela", tag = "Politiker", link = "https://de.wikipedia.org/wiki/Nelson_Mandela", imageUrl = "image_url_mandela"),
//            Author(id = 19, name = "Johann Wolfgang von Goethe", tag = "Schriftsteller", link = "https://de.wikipedia.org/wiki/Johann_Wolfgang_von_Goethe", imageUrl = "image_url_goethe"),
//            Author(id = 20, name = "Buddha", tag = "Philosoph", link = "https://de.wikipedia.org/wiki/Buddha", imageUrl = "image_url_buddha"),
//            Author(id = 21, name = "Martin Luther King Jr.", tag = "Bürgerrechtler", link = "https://de.wikipedia.org/wiki/Martin_Luther_King", imageUrl = "image_url_king"),
//            Author(id = 22, name = "Henry Ford", tag = "Unternehmer", link = "https://de.wikipedia.org/wiki/Henry_Ford", imageUrl = "image_url_ford"),
//            Author(id = 23, name = "Sokrates", tag = "Philosoph", link = "https://de.wikipedia.org/wiki/Sokrates", imageUrl = "image_url_sokrates")
//        )
//

//}