package com.brocoders.taaza_khabar.presentation.newsflip

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.repository.BookmarkRepository
import com.brocoders.taaza_khabar.data.service.TextToSpeechManager
import com.brocoders.taaza_khabar.presentation.components.TTSControls
import com.brocoders.taaza_khabar.presentation.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFlipScreen(
    categoryParam: String,
    onBackClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val localizedStrings by viewModel.localizedStrings.collectAsStateWithLifecycle()
    val bookmarkViewModel: BookmarkViewModel = hiltViewModel()
    
    // Create TTS manager
    val ttsManager = remember { TextToSpeechManager(context.applicationContext) }
    
    // Bookmark states for all articles
    var bookmarkStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    
    // Load bookmark states when articles change
    LaunchedEffect(uiState.articles) {
        if (uiState.articles.isNotEmpty()) {
            val states = mutableMapOf<String, Boolean>()
            uiState.articles.forEach { article ->
                states[article.url] = bookmarkViewModel.isBookmarked(article.url)
            }
            bookmarkStates = states
        }
    }
    
    // Cleanup TTS when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }
    
    // Load news for the category when screen opens
    LaunchedEffect(categoryParam) {
        viewModel.loadNewsForCategory(categoryParam)
    }
    
    // Loading animation state
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val loadingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadingAlpha"
    )
    
    when {
        uiState.isLoading -> {
            LoadingScreen(
                localizedStrings = localizedStrings,
                loadingAlpha = loadingAlpha
            )
        }
        
        uiState.error != null -> {
            ErrorScreen(
                error = uiState.error!!,
                localizedStrings = localizedStrings,
                onRetry = { viewModel.loadNewsForCategory(categoryParam) },
                onBackClick = onBackClick
            )
        }
        
        uiState.articles.isNotEmpty() -> {
            NewsFlipContent(
                articles = uiState.articles,
                localizedStrings = localizedStrings,
                onBackClick = onBackClick,
                onArticleClick = onArticleClick,
                categoryParam = categoryParam,
                ttsManager = ttsManager,
                bookmarkStates = bookmarkStates,
                onBookmarkClick = { article ->
                    bookmarkViewModel.toggleBookmark(article)
                    // Update local state immediately
                    val currentStates = bookmarkStates.toMutableMap()
                    currentStates[article.url] = !(currentStates[article.url] ?: false)
                    bookmarkStates = currentStates
                }
            )
        }
        
        else -> {
            EmptyScreen(
                localizedStrings = localizedStrings,
                onBackClick = onBackClick,
                onRetry = { viewModel.loadNewsForCategory(categoryParam) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsFlipContent(
    articles: List<Article>,
    localizedStrings: Map<String, String>,
    onBackClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    categoryParam: String,
    ttsManager: TextToSpeechManager,
    bookmarkStates: Map<String, Boolean> = emptyMap(),
    onBookmarkClick: (Article) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { articles.size })
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { articles[it].url }
        ) { pageIndex ->
            val article = articles[pageIndex]
            NewsFlipCard(
                article = article,
                localizedStrings = localizedStrings,
                isActive = pageIndex == pagerState.currentPage,
                onArticleClick = onArticleClick,
                ttsManager = ttsManager,
                isBookmarked = bookmarkStates[article.url] ?: false,
                onBookmarkClick = onBookmarkClick
            )
        }
        
        // Top App Bar with share button on top-right
        TopAppBar(
            title = { 
                Column {
                    Text(
                        text = localizedStrings["news_flip"] ?: "News Flip",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${pagerState.currentPage + 1} ${localizedStrings["of"] ?: "of"} ${articles.size}",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = localizedStrings["back"] ?: "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                // Share button at top-right
                IconButton(
                    onClick = {
                        val article = articles[pagerState.currentPage]
                        val shareText = "${article.title}\n\n${article.description ?: ""}\n\n${localizedStrings["read_more_at"] ?: "Read more at"}: ${article.url}"
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, article.title)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, localizedStrings["share"] ?: "Share"))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = localizedStrings["share"] ?: "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.4f)
            )
        )
        
        // Page indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(minOf(articles.size, 5)) { index ->
                val isActive = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (isActive) 8.dp else 6.dp)
                        .background(
                            if (isActive) Color.White else Color.White.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .scale(if (isActive) 1f else 0.8f)
                )
            }
            if (articles.size > 5) {
                Text(
                    text = "...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Swipe hint - show for first few articles
        if (pagerState.currentPage < 2) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "↑ ${localizedStrings["swipe_up"] ?: "Swipe up for next article"} ↑",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun NewsFlipCard(
    article: Article,
    localizedStrings: Map<String, String>,
    isActive: Boolean,
    onArticleClick: (Article) -> Unit,
    ttsManager: TextToSpeechManager,
    isBookmarked: Boolean = false,
    onBookmarkClick: (Article) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var imageState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }
    
    // Animation for card entrance
    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.95f,
        animationSpec = tween(300),
        label = "cardScale"
    )
    
    Card(
        modifier = modifier
            .fillMaxSize()
            .scale(animatedScale)
            .clickable { onArticleClick(article) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            // Image section - 35% of screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = article.urlToImage,
                        contentDescription = article.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onState = { imageState = it }
                    )
                    
                    when (imageState) {
                        is AsyncImagePainter.State.Loading, null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        is AsyncImagePainter.State.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📰",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Color.White
                                )
                            }
                        }
                        else -> Unit
                    }
                    
                    // Source and date overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .align(Alignment.BottomStart)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = article.source.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        
                        article.publishedAt?.let { publishedAt ->
                            val formattedDate = try {
                                val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                                val outputFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                val date = inputFormatter.parse(publishedAt)
                                date?.let { outputFormatter.format(it) } ?: "Recent"
                            } catch (e: Exception) {
                                "Recent"
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    // Bookmark Icon
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 8.dp
                    ) {
                        IconButton(
                            onClick = { onBookmarkClick(article) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
            
            // Content section - 65% of screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                SelectionContainer {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.headlineMedium.lineHeight,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Divider
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                
                // Description
                article.description?.let { description ->
                    SelectionContainer {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            textAlign = TextAlign.Justify
                        )
                    }
                } ?: run {
                    Text(
                        text = "${localizedStrings["full_article_available"] ?: "Full article available"}. ${localizedStrings["tap_to_read_more"] ?: "Tap to read the complete story with detailed information and comprehensive coverage of this news event"}. Stay informed with the latest updates and in-depth analysis from our trusted news sources.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textAlign = TextAlign.Justify,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                
                // TTS Controls
                TTSControls(
                    ttsManager = ttsManager,
                    articleTitle = article.title,
                    articleDescription = article.description,
                    localizedStrings = localizedStrings,
                    isCompact = false,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Read more button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Button(
                        onClick = { onArticleClick(article) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(180f),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizedStrings["read_full_article"] ?: "Read Full Article",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen(
    localizedStrings: Map<String, String>,
    loadingAlpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .alpha(loadingAlpha),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = localizedStrings["loading_news"] ?: "Loading news...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = loadingAlpha),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreen(
    error: String,
    localizedStrings: Map<String, String>,
    onRetry: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Text(
                    text = localizedStrings["error"] ?: "Error",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = localizedStrings["back"] ?: "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = localizedStrings["error_loading_news"] ?: "Error loading news",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizedStrings["try_again"] ?: "Try Again")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyScreen(
    localizedStrings: Map<String, String>,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Text(
                    text = localizedStrings["no_news"] ?: "No News",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = localizedStrings["back"] ?: "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📰",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = localizedStrings["no_articles_found"] ?: "No articles found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizedStrings["refresh"] ?: "Refresh")
            }
        }
    }
} 