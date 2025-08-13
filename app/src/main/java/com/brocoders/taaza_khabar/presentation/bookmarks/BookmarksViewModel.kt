package com.brocoders.taaza_khabar.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    private val _bookmarkStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val bookmarkStates: StateFlow<Map<String, Boolean>> = _bookmarkStates.asStateFlow()

    init {
        Log.d("BookmarksViewModel", "BookmarksViewModel initialized")
        loadBookmarks()
    }

    fun loadBookmarks() {
        Log.d("BookmarksViewModel", "Loading bookmarks...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                bookmarkRepository.getAllBookmarks()
                    .catch { exception ->
                        Log.e("BookmarksViewModel", "Error loading bookmarks", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                    .collect { bookmarks ->
                        Log.d("BookmarksViewModel", "Loaded ${bookmarks.size} bookmarks")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            bookmarks = bookmarks,
                            error = null
                        )
                        
                        // Update bookmark states
                        _bookmarkStates.value = bookmarks.associate { it.url to true }
                        Log.d("BookmarksViewModel", "Updated bookmark states: ${_bookmarkStates.value.keys}")
                    }
            } catch (e: Exception) {
                Log.e("BookmarksViewModel", "Exception loading bookmarks", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun toggleBookmark(article: Article) {
        Log.d("BookmarksViewModel", "Toggling bookmark for: ${article.title}")
        viewModelScope.launch {
            try {
                bookmarkRepository.toggleBookmark(article)
                
                // Update local state immediately for better UX
                val currentStates = _bookmarkStates.value.toMutableMap()
                val isCurrentlyBookmarked = currentStates[article.url] ?: false
                currentStates[article.url] = !isCurrentlyBookmarked
                _bookmarkStates.value = currentStates
                
                // If removing bookmark, also remove from the list
                if (isCurrentlyBookmarked) {
                    val currentBookmarks = _uiState.value.bookmarks.toMutableList()
                    currentBookmarks.removeAll { it.url == article.url }
                    _uiState.value = _uiState.value.copy(bookmarks = currentBookmarks)
                    Log.d("BookmarksViewModel", "Removed bookmark, remaining: ${currentBookmarks.size}")
                }
                
            } catch (e: Exception) {
                Log.e("BookmarksViewModel", "Error toggling bookmark", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update bookmark"
                )
            }
        }
    }

    fun clearAllBookmarks() {
        Log.d("BookmarksViewModel", "Clearing all bookmarks")
        viewModelScope.launch {
            try {
                val bookmarksToRemove = _uiState.value.bookmarks
                bookmarksToRemove.forEach { article ->
                    bookmarkRepository.removeBookmark(article)
                }
                
                _uiState.value = _uiState.value.copy(bookmarks = emptyList())
                _bookmarkStates.value = emptyMap()
                Log.d("BookmarksViewModel", "All bookmarks cleared")
                
            } catch (e: Exception) {
                Log.e("BookmarksViewModel", "Error clearing bookmarks", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to clear bookmarks"
                )
            }
        }
    }

    fun isBookmarked(articleUrl: String): Flow<Boolean> {
        return bookmarkRepository.isBookmarkedFlow(articleUrl)
    }
}

data class BookmarksUiState(
    val isLoading: Boolean = false,
    val bookmarks: List<Article> = emptyList(),
    val error: String? = null
) 