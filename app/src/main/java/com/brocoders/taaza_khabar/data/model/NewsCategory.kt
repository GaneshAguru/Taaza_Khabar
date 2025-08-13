package com.brocoders.taaza_khabar.data.model

import androidx.annotation.DrawableRes
import com.brocoders.taaza_khabar.R

data class NewsCategory(
    val id: String,
    val name: String,
    val apiParam: String,
    @DrawableRes val iconRes: Int,
    val description: String
)

object NewsCategoryData {
    val categories = listOf(
        NewsCategory(
            id = "business",
            name = "Business",
            apiParam = "business",
            iconRes = R.drawable.business,
            description = "Latest business news and market updates"
        ),
        NewsCategory(
            id = "sports",
            name = "Sports",
            apiParam = "sports",
            iconRes = R.drawable.sports,
            description = "Sports news, scores, and highlights"
        ),
        NewsCategory(
            id = "technology",
            name = "Technology",
            apiParam = "technology",
            iconRes = R.drawable.technology,
            description = "Tech news, gadgets, and innovations"
        ),
        NewsCategory(
            id = "health",
            name = "Health",
            apiParam = "health",
            iconRes = R.drawable.health,
            description = "Health, wellness, and medical news"
        ),
        NewsCategory(
            id = "science",
            name = "Science",
            apiParam = "science",
            iconRes = R.drawable.science,
            description = "Scientific discoveries and research"
        ),
        NewsCategory(
            id = "entertainment",
            name = "Entertainment",
            apiParam = "entertainment",
            iconRes = R.drawable.entertainment,
            description = "Entertainment, movies, and celebrity news"
        ),
        NewsCategory(
            id = "general",
            name = "Current Affairs",
            apiParam = "general",
            iconRes = R.drawable.currentaffairs,
            description = "General news and current affairs"
        ),
        NewsCategory(
            id = "politics",
            name = "Geo Politics",
            apiParam = "general", // Using general as politics is not available in NewsAPI
            iconRes = R.drawable.geopolitics,
            description = "Political news and geopolitical updates"
        )
    )
}