package com.brocoders.taaza_khabar.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.repository.BookmarkRepository
import com.brocoders.taaza_khabar.data.repository.NewsRepository
import com.brocoders.taaza_khabar.data.service.PaymentService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val bookmarkRepository: BookmarkRepository,
    val paymentService: PaymentService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsDetailUiState())
    val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()
    
    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    fun loadArticleByUrl(url: String) {
        Log.d("NewsDetailViewModel", "Loading article by URL: $url")
        val article = newsRepository.getArticleByUrl(url)
        _uiState.value = _uiState.value.copy(
            article = article,
            isLoading = false,
            error = if (article == null) "Article not found" else null
        )
        
        // Check bookmark status
        article?.let {
            Log.d("NewsDetailViewModel", "Article found, checking bookmark status for: ${it.title}")
            checkBookmarkStatus(it.url)
        }
    }

    fun setArticle(article: Article) {
        Log.d("NewsDetailViewModel", "Setting article: ${article.title}")
        _uiState.value = _uiState.value.copy(article = article, isLoading = false, error = null)
        checkBookmarkStatus(article.url)
    }
    
    private fun checkBookmarkStatus(articleUrl: String) {
        viewModelScope.launch {
            try {
                val isBookmarked = bookmarkRepository.isBookmarked(articleUrl)
                _isBookmarked.value = isBookmarked
                Log.d("NewsDetailViewModel", "Bookmark status for $articleUrl: $isBookmarked")
            } catch (e: Exception) {
                Log.e("NewsDetailViewModel", "Error checking bookmark status", e)
            }
        }
    }
    
    fun toggleBookmark() {
        val article = _uiState.value.article ?: return
        
        Log.d("NewsDetailViewModel", "Toggling bookmark for: ${article.title}")
        viewModelScope.launch {
            try {
                bookmarkRepository.toggleBookmark(article)
                val newStatus = bookmarkRepository.isBookmarked(article.url)
                _isBookmarked.value = newStatus
                Log.d("NewsDetailViewModel", "Bookmark toggled. New status: $newStatus")
            } catch (e: Exception) {
                Log.e("NewsDetailViewModel", "Error toggling bookmark", e)
                // Handle error if needed
            }
        }
    }
}

data class NewsDetailUiState(
    val article: Article? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)