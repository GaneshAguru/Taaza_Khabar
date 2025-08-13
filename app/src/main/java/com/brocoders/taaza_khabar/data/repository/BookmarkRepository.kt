package com.brocoders.taaza_khabar.data.repository

import com.brocoders.taaza_khabar.data.database.BookmarkDao
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.model.BookmarkEntity
import com.brocoders.taaza_khabar.data.model.toArticle
import com.brocoders.taaza_khabar.data.model.toBookmarkEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    
    fun getAllBookmarks(): Flow<List<Article>> {
        Log.d("BookmarkRepository", "Getting all bookmarks from database")
        return bookmarkDao.getAllBookmarks().map { bookmarkEntities ->
            Log.d("BookmarkRepository", "Retrieved ${bookmarkEntities.size} bookmark entities from database")
            bookmarkEntities.map { it.toArticle() }
        }
    }
    
    suspend fun isBookmarked(url: String): Boolean {
        val result = bookmarkDao.isBookmarked(url)
        Log.d("BookmarkRepository", "Checking if bookmarked - URL: $url, Result: $result")
        return result
    }
    
    fun isBookmarkedFlow(url: String): Flow<Boolean> {
        return bookmarkDao.isBookmarkedFlow(url)
    }
    
    suspend fun toggleBookmark(article: Article) {
        Log.d("BookmarkRepository", "Toggling bookmark for article: ${article.title}")
        if (bookmarkDao.isBookmarked(article.url)) {
            Log.d("BookmarkRepository", "Article is bookmarked, removing...")
            bookmarkDao.deleteBookmarkByUrl(article.url)
            Log.d("BookmarkRepository", "Bookmark removed")
        } else {
            Log.d("BookmarkRepository", "Article not bookmarked, adding...")
            val bookmarkEntity = article.toBookmarkEntity()
            bookmarkDao.insertBookmark(bookmarkEntity)
            Log.d("BookmarkRepository", "Bookmark added")
        }
    }
    
    suspend fun addBookmark(article: Article) {
        Log.d("BookmarkRepository", "Adding bookmark for: ${article.title}")
        bookmarkDao.insertBookmark(article.toBookmarkEntity())
    }
    
    suspend fun removeBookmark(article: Article) {
        Log.d("BookmarkRepository", "Removing bookmark for: ${article.title}")
        bookmarkDao.deleteBookmarkByUrl(article.url)
    }
    
    suspend fun removeBookmarkByUrl(url: String) {
        Log.d("BookmarkRepository", "Removing bookmark by URL: $url")
        bookmarkDao.deleteBookmarkByUrl(url)
    }
    
    suspend fun getBookmarkCount(): Int {
        val count = bookmarkDao.getBookmarkCount()
        Log.d("BookmarkRepository", "Total bookmarks count: $count")
        return count
    }
} 