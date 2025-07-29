package com.brocoders.taaza_khabar.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class NewsCategory(
    val id: String,
    val name: String,
    val apiParam: String,
    @Transient val icon: ImageVector = Icons.Default.Category,
    val description: String
)

object NewsCategoryData {
    val categories = listOf(
        NewsCategory(
            id = "business",
            name = "Business",
            apiParam = "business",
            icon = Icons.Default.Business,
            description = "Latest business news and market updates"
        ),
        NewsCategory(
            id = "sports",
            name = "Sports",
            apiParam = "sports",
            icon = Icons.Default.SportsBaseball,
            description = "Sports news, scores, and highlights"
        ),
        NewsCategory(
            id = "technology",
            name = "Technology",
            apiParam = "technology",
            icon = Icons.Default.Computer,
            description = "Tech news, gadgets, and innovations"
        ),
        NewsCategory(
            id = "health",
            name = "Health",
            apiParam = "health",
            icon = Icons.Default.HealthAndSafety,
            description = "Health, wellness, and medical news"
        ),
        NewsCategory(
            id = "science",
            name = "Science",
            apiParam = "science",
            icon = Icons.Default.Science,
            description = "Scientific discoveries and research"
        ),
        NewsCategory(
            id = "entertainment",
            name = "Entertainment",
            apiParam = "entertainment",
            icon = Icons.Default.Movie,
            description = "Entertainment, movies, and celebrity news"
        ),
        NewsCategory(
            id = "general",
            name = "Current Affairs",
            apiParam = "general",
            icon = Icons.Default.Public,
            description = "General news and current affairs"
        ),
        NewsCategory(
            id = "politics",
            name = "Geo Politics",
            apiParam = "general", // Using general as politics is not available in NewsAPI
            icon = Icons.Default.AccountBalance,
            description = "Political news and geopolitical updates"
        )
    )
}