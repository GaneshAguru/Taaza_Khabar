package com.brocoders.taaza_khabar.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.model.NewsCategory
import com.brocoders.taaza_khabar.data.model.NewsCategoryData
import com.brocoders.taaza_khabar.presentation.bookmarks.BookmarksScreen
import com.brocoders.taaza_khabar.presentation.detail.NewsDetailScreen
import com.brocoders.taaza_khabar.presentation.home.HomeScreen
import com.brocoders.taaza_khabar.presentation.newsflip.NewsFlipScreen
import com.brocoders.taaza_khabar.presentation.newslist.NewsListScreen
import com.brocoders.taaza_khabar.presentation.search.SearchScreen
import com.brocoders.taaza_khabar.presentation.webview.WebViewScreen

import com.google.gson.Gson

@Composable
fun NewsNavigation(
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = NewsDestinations.HOME_ROUTE,
        modifier = modifier
    ) {
        composable(NewsDestinations.HOME_ROUTE) {
            HomeScreen(
                onCategoryClick = { category ->
                    val encodedCategoryParam = java.net.URLEncoder.encode(category.apiParam, "UTF-8")
                    navController.navigate("${NewsDestinations.NEWS_FLIP_ROUTE}/$encodedCategoryParam")
                },
                onSearchClick = {
                    navController.navigate(NewsDestinations.SEARCH_ROUTE)
                },
                onBookmarksClick = {
                    navController.navigate(NewsDestinations.BOOKMARKS_ROUTE)
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        }

        composable("${NewsDestinations.NEWS_LIST_ROUTE}/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            val category = NewsCategoryData.categories.find { it.id == categoryId }
                ?: NewsCategoryData.categories.first()

            NewsListScreen(
                category = category,
                onBackClick = {
                    navController.popBackStack()
                },
                onArticleClick = { article ->
                    // URL encode the article URL to handle special characters
                    val encodedUrl = java.net.URLEncoder.encode(article.url, "UTF-8")
                    navController.navigate("${NewsDestinations.NEWS_DETAIL_ROUTE}/$encodedUrl")
                }
            )
        }

        composable(NewsDestinations.SEARCH_ROUTE) {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onArticleClick = { article ->
                    val encodedUrl = java.net.URLEncoder.encode(article.url, "UTF-8")
                    navController.navigate("${NewsDestinations.NEWS_DETAIL_ROUTE}/$encodedUrl")
                }
            )
        }

        composable(NewsDestinations.BOOKMARKS_ROUTE) {
            BookmarksScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onArticleClick = { article ->
                    val encodedUrl = java.net.URLEncoder.encode(article.url, "UTF-8")
                    navController.navigate("${NewsDestinations.NEWS_DETAIL_ROUTE}/$encodedUrl")
                }
            )
        }

        composable("${NewsDestinations.NEWS_FLIP_ROUTE}/{categoryParam}") { backStackEntry ->
            val encodedCategoryParam = backStackEntry.arguments?.getString("categoryParam")
            val categoryParam = encodedCategoryParam?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "general"

            NewsFlipScreen(
                categoryParam = categoryParam,
                onBackClick = {
                    navController.popBackStack()
                },
                onArticleClick = { article ->
                    // Navigate to article detail screen
                    val encodedUrl = java.net.URLEncoder.encode(article.url, "UTF-8")
                    navController.navigate("${NewsDestinations.NEWS_DETAIL_ROUTE}/$encodedUrl")
                }
            )
        }

        composable("${NewsDestinations.NEWS_DETAIL_ROUTE}/{encodedUrl}") { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl")
            val articleUrl = encodedUrl?.let { java.net.URLDecoder.decode(it, "UTF-8") }

            NewsDetailScreen(
                articleUrl = articleUrl,
                onBackClick = {
                    navController.popBackStack()
                },
                onOpenWebView = { url, title ->
                    val encodedWebUrl = java.net.URLEncoder.encode(url, "UTF-8")
                    val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
                    navController.navigate("${NewsDestinations.WEBVIEW_ROUTE}/$encodedWebUrl/$encodedTitle")
                }
            )
        }

        composable("${NewsDestinations.WEBVIEW_ROUTE}/{encodedUrl}/{encodedTitle}") { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl")
            val encodedTitle = backStackEntry.arguments?.getString("encodedTitle")
            val url = encodedUrl?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: ""
            val title = encodedTitle?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "Article"

            WebViewScreen(
                url = url,
                title = title,
                onBackClick = {
                    navController.popBackStack()
                },
                onShareClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, title)
                        putExtra(Intent.EXTRA_TEXT, "Check out this article: $url")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
                }
            )
        }
    }
}

object NewsDestinations {
    const val HOME_ROUTE = "home"
    const val NEWS_LIST_ROUTE = "news_list"
    const val NEWS_DETAIL_ROUTE = "news_detail"
    const val SEARCH_ROUTE = "search"
    const val BOOKMARKS_ROUTE = "bookmarks"
    const val WEBVIEW_ROUTE = "webview"
    const val NEWS_FLIP_ROUTE = "news_flip"
}