package com.brocoders.taaza_khabar.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val url: String,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val author: String?,
    val publishedAt: String,
    val sourceName: String,
    val sourceId: String?,
    val content: String?,
    val bookmarkedAt: Long = System.currentTimeMillis()
)

// Extension function to convert Article to BookmarkEntity
fun Article.toBookmarkEntity(): BookmarkEntity {
    return BookmarkEntity(
        url = this.url,
        title = this.title,
        description = this.description,
        urlToImage = this.urlToImage,
        author = this.author,
        publishedAt = this.publishedAt,
        sourceName = this.source.name,
        sourceId = this.source.id,
        content = this.content
    )
}

// Extension function to convert BookmarkEntity to Article
fun BookmarkEntity.toArticle(): Article {
    return Article(
        source = Source(id = this.sourceId, name = this.sourceName),
        author = this.author,
        title = this.title,
        description = this.description,
        url = this.url,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content
    )
} 