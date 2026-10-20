package com.ulsa.deportes.ui.newsSection.newsHome.util

import com.ulsa.deportes.R
import java.util.Locale

/**
 * Selecciona un recurso drawable local de respaldo adecuado según el deporte o contenido de la noticia.
 */
fun getNewsPlaceholderRes(title: String, description: String = ""): Int {
    val text = "$title $description".lowercase(Locale.ROOT)
    return when {
        text.contains("futbol") || text.contains("soccer") || text.contains("gol") || text.contains("liga") || text.contains("partido") || text.contains("linces") -> R.drawable.ic_news_football
        text.contains("basquetbol") || text.contains("baloncesto") || text.contains("basket") || text.contains("nba") || text.contains("gimnasio") -> R.drawable.ic_news_basketball
        text.contains("beisbol") || text.contains("béisbol") || text.contains("jonron") || text.contains("diamante") -> R.drawable.ic_news_baseball
        else -> R.drawable.ic_news_general
    }
}
