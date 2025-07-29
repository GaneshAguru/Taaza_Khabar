package com.brocoders.taaza_khabar.presentation.detail

import androidx.lifecycle.ViewModel
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsDetailUiState())
    val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()

    fun loadArticleByUrl(url: String) {
        val article = newsRepository.getArticleByUrl(url)
        _uiState.value = _uiState.value.copy(
            article = article,
            isLoading = false,
            error = if (article == null) "Article not found" else null
        )
    }

    fun setArticle(article: Article) {
        _uiState.value = _uiState.value.copy(article = article, isLoading = false, error = null)
    }
}

data class NewsDetailUiState(
    val article: Article? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)