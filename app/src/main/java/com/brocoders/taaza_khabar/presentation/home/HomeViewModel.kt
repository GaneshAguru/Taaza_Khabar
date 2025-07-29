package com.brocoders.taaza_khabar.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.model.NewsCategory
import com.brocoders.taaza_khabar.data.model.NewsCategoryData
import com.brocoders.taaza_khabar.data.repository.NewsRepository
import com.brocoders.taaza_khabar.util.Resource

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        _uiState.value = _uiState.value.copy(
            categories = NewsCategoryData.categories,
            isLoading = false
        )
    }

    fun onCategoryClick(category: NewsCategory) {
        loadNewsForCategory(category.apiParam)
    }

    private fun loadNewsForCategory(category: String) {
        viewModelScope.launch {
            newsRepository.getTopHeadlines(category).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            articles = resource.data ?: emptyList(),
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun searchNews() {
        val query = _searchQuery.value.trim()
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                newsRepository.searchNews(query).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                articles = resource.data ?: emptyList(),
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HomeUiState(
    val categories: List<NewsCategory> = emptyList(),
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)