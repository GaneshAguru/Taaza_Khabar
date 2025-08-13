package com.brocoders.taaza_khabar.presentation.newslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.model.NewsCategory
import com.brocoders.taaza_khabar.data.repository.BookmarkRepository
import com.brocoders.taaza_khabar.data.repository.NewsRepository
import com.brocoders.taaza_khabar.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class NewsListViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsListUiState())
    val uiState: StateFlow<NewsListUiState> = _uiState.asStateFlow()

    private val _bookmarkStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val bookmarkStates: StateFlow<Map<String, Boolean>> = _bookmarkStates.asStateFlow()

    fun loadNews(category: NewsCategory, language: String = "en") {
        Log.d("NewsListViewModel", "Loading news for category: ${category.name}")
        viewModelScope.launch {
            newsRepository.getTopHeadlines(category.apiParam, language)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true,
                                error = null
                            )
                        }
                        is Resource.Success -> {
                            Log.d("NewsListViewModel", "Loaded ${resource.data?.size ?: 0} articles for ${category.name}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                articles = resource.data ?: emptyList(),
                                error = null
                            )
                            
                            // Load bookmark states for the articles
                            loadBookmarkStates(resource.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            Log.e("NewsListViewModel", "Error loading news: ${resource.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                    }
                }
        }
    }

    private fun loadBookmarkStates(articles: List<Article>) {
        Log.d("NewsListViewModel", "Loading bookmark states for ${articles.size} articles")
        viewModelScope.launch {
            val states = mutableMapOf<String, Boolean>()
            articles.forEach { article ->
                states[article.url] = bookmarkRepository.isBookmarked(article.url)
            }
            _bookmarkStates.value = states
            Log.d("NewsListViewModel", "Loaded bookmark states: ${states.entries.count { it.value }} bookmarked out of ${states.size}")
        }
    }

    fun toggleBookmark(article: Article) {
        Log.d("NewsListViewModel", "Toggling bookmark for: ${article.title}")
        viewModelScope.launch {
            try {
                bookmarkRepository.toggleBookmark(article)
                
                // Update local state immediately for better UX
                val currentStates = _bookmarkStates.value.toMutableMap()
                val isCurrentlyBookmarked = currentStates[article.url] ?: false
                currentStates[article.url] = !isCurrentlyBookmarked
                _bookmarkStates.value = currentStates
                
                Log.d("NewsListViewModel", "Bookmark toggled. New state: ${!isCurrentlyBookmarked}")
                
            } catch (e: Exception) {
                Log.e("NewsListViewModel", "Error toggling bookmark", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update bookmark"
                )
            }
        }
    }

    fun isBookmarked(articleUrl: String): Flow<Boolean> {
        return bookmarkRepository.isBookmarkedFlow(articleUrl)
    }
}

data class NewsListUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val error: String? = null
) 