package com.brocoders.taaza_khabar.data.repository


import com.brocoders.taaza_khabar.data.api.NewsApiService
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val newsApiService: NewsApiService
) {
    
    // Cache for articles to avoid passing complex objects through navigation
    private val articlesCache = mutableMapOf<String, Article>()
    
    fun cacheArticle(article: Article) {
        articlesCache[article.url] = article
    }
    
    fun getArticleByUrl(url: String): Article? {
        return articlesCache[url]
    }

    fun getTopHeadlines(category: String, language: String = "en"): Flow<Resource<List<Article>>> = flow {
        try {
            emit(Resource.Loading())
            val response = newsApiService.getTopHeadlines(
                category = category,
                language = language,
                apiKey = NewsApiService.API_KEY
            )

            if (response.isSuccessful) {
                response.body()?.let { newsResponse ->
                    // Cache articles for navigation
                    newsResponse.articles.forEach { article ->
                        cacheArticle(article)
                    }
                    emit(Resource.Success(newsResponse.articles))
                } ?: emit(Resource.Error("No data available"))
            } else {
                emit(Resource.Error("Failed to fetch news: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    fun searchNews(query: String, language: String = "en"): Flow<Resource<List<Article>>> = flow {
        try {
            emit(Resource.Loading())
            val response = newsApiService.searchNews(
                query = query,
                language = language,
                apiKey = NewsApiService.API_KEY
            )

            if (response.isSuccessful) {
                response.body()?.let { newsResponse ->
                    // Cache articles for navigation
                    newsResponse.articles.forEach { article ->
                        cacheArticle(article)
                    }
                    emit(Resource.Success(newsResponse.articles))
                } ?: emit(Resource.Error("No data available"))
            } else {
                emit(Resource.Error("Failed to search news: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }
}