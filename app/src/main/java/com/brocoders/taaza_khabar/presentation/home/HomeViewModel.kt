package com.brocoders.taaza_khabar.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.model.Language
import com.brocoders.taaza_khabar.data.model.LanguageData
import com.brocoders.taaza_khabar.data.model.LocalizedStrings
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

    private val _selectedLanguage = MutableStateFlow(LanguageData.getDefaultLanguage())
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    private val _localizedStrings = MutableStateFlow(LocalizedStrings.getStrings("en"))
    val localizedStrings: StateFlow<Map<String, String>> = _localizedStrings.asStateFlow()
    
    private var currentCategoryParam: String = "general" // Track current category

    init {
        loadCategories()
        // Load default news
        loadNewsByCategory(currentCategoryParam)
    }

    private fun loadCategories() {
        _uiState.value = _uiState.value.copy(
            categories = NewsCategoryData.categories,
            isLoading = false
        )
    }

    fun onCategoryClick(category: NewsCategory) {
        currentCategoryParam = category.apiParam
        loadNewsByCategory(category.apiParam)
    }

    fun onLanguageSelected(language: Language) {
        _selectedLanguage.value = language
        _localizedStrings.value = LocalizedStrings.getStrings(language.code)
        
        // Save language preference
        // TODO: Add SharedPreferences to save language preference
        
        // Reload current data with new language
        loadNewsByCategory(currentCategoryParam)
        
        // Also reload search results if there are any
        val currentSearchQuery = _searchQuery.value
        if (currentSearchQuery.isNotBlank()) {
            // TODO: Implement search reload with new language
        }
    }
    
    // Public method to load news for flip screen
    fun loadNewsForCategory(categoryParam: String) {
        currentCategoryParam = categoryParam
        loadNewsByCategory(categoryParam)
    }

    private fun loadNewsByCategory(category: String) {
        viewModelScope.launch {
            newsRepository.getTopHeadlines(category, _selectedLanguage.value.code).collect { resource ->
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
                newsRepository.searchNews(query, _selectedLanguage.value.code).collect { resource ->
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    

}

data class HomeUiState(
    val categories: List<NewsCategory> = emptyList(),
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)