package com.brocoders.taaza_khabar.presentation.newsflip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    suspend fun isBookmarked(articleUrl: String): Boolean {
        val result = bookmarkRepository.isBookmarked(articleUrl)
        Log.d("NewsFlipBookmarkViewModel", "Checking bookmark status for $articleUrl: $result")
        return result
    }

    fun toggleBookmark(article: Article) {
        Log.d("NewsFlipBookmarkViewModel", "Toggling bookmark for NewsFlip article: ${article.title}")
        viewModelScope.launch {
            try {
                bookmarkRepository.toggleBookmark(article)
                Log.d("NewsFlipBookmarkViewModel", "Bookmark toggled successfully")
            } catch (e: Exception) {
                Log.e("NewsFlipBookmarkViewModel", "Error toggling bookmark", e)
            }
        }
    }
} 