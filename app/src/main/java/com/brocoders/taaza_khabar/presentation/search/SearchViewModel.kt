package com.brocoders.taaza_khabar.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.repository.BookmarkRepository
import com.brocoders.taaza_khabar.data.repository.NewsRepository
import com.brocoders.taaza_khabar.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _bookmarkStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val bookmarkStates: StateFlow<Map<String, Boolean>> = _bookmarkStates.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchNews(language: String = "en") {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) return

        Log.d("SearchViewModel", "Searching for: $query")
        viewModelScope.launch {
            newsRepository.searchNews(query, language)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true,
                                error = null
                            )
                        }
                        is Resource.Success -> {
                            Log.d("SearchViewModel", "Search completed, found ${resource.data?.size ?: 0} articles")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                articles = resource.data ?: emptyList(),
                                error = null
                            )
                            
                            // Load bookmark states for the articles
                            loadBookmarkStates(resource.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            Log.e("SearchViewModel", "Search error: ${resource.message}")
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
        Log.d("SearchViewModel", "Loading bookmark states for ${articles.size} articles")
        viewModelScope.launch {
            val states = mutableMapOf<String, Boolean>()
            articles.forEach { article ->
                states[article.url] = bookmarkRepository.isBookmarked(article.url)
            }
            _bookmarkStates.value = states
            Log.d("SearchViewModel", "Loaded bookmark states: ${states.entries.count { it.value }} bookmarked out of ${states.size}")
        }
    }

    fun toggleBookmark(article: Article) {
        Log.d("SearchViewModel", "Toggling bookmark for search result: ${article.title}")
        viewModelScope.launch {
            try {
                bookmarkRepository.toggleBookmark(article)
                
                // Update local state immediately for better UX
                val currentStates = _bookmarkStates.value.toMutableMap()
                val isCurrentlyBookmarked = currentStates[article.url] ?: false
                currentStates[article.url] = !isCurrentlyBookmarked
                _bookmarkStates.value = currentStates
                
                Log.d("SearchViewModel", "Bookmark toggled. New state: ${!isCurrentlyBookmarked}")
                
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error toggling bookmark", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update bookmark"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.value = SearchUiState()
        _bookmarkStates.value = emptyMap()
        Log.d("SearchViewModel", "Search cleared")
    }
}

data class SearchUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val error: String? = null
) 