package com.brocoders.taaza_khabar.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.model.NewsCategory
import com.brocoders.taaza_khabar.data.model.NewsCategoryData
import com.brocoders.taaza_khabar.presentation.detail.NewsDetailScreen
import com.brocoders.taaza_khabar.presentation.home.HomeScreen
import com.brocoders.taaza_khabar.presentation.newslist.NewsListScreen

import com.google.gson.Gson

@Composable
fun NewsNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NewsDestinations.HOME_ROUTE,
        modifier = modifier
    ) {
        composable(NewsDestinations.HOME_ROUTE) {
            HomeScreen(
                onCategoryClick = { category ->
                    navController.navigate("${NewsDestinations.NEWS_LIST_ROUTE}/${category.id}")
                },
                onSearchClick = {
                    // TODO: Implement search functionality
                }
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

        composable("${NewsDestinations.NEWS_DETAIL_ROUTE}/{encodedUrl}") { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl")
            val articleUrl = encodedUrl?.let { java.net.URLDecoder.decode(it, "UTF-8") }

            NewsDetailScreen(
                articleUrl = articleUrl,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

object NewsDestinations {
    const val HOME_ROUTE = "home"
    const val NEWS_LIST_ROUTE = "news_list"
    const val NEWS_DETAIL_ROUTE = "news_detail"
}