package com.brocoders.taaza_khabar.presentation.detail

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.brocoders.taaza_khabar.data.model.Article
import com.brocoders.taaza_khabar.data.repository.BookmarkRepository
import com.brocoders.taaza_khabar.data.service.TextToSpeechManager
import com.brocoders.taaza_khabar.presentation.components.SubscriptionDialog
import com.brocoders.taaza_khabar.presentation.components.TTSControls
import com.brocoders.taaza_khabar.presentation.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    articleUrl: String?,
    onBackClick: () -> Unit,
    onOpenWebView: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: NewsDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var isSubscribed by remember { mutableStateOf(false) }
    
    // Bookmark functionality
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    
    // Create TTS manager
    val ttsManager = remember { TextToSpeechManager(context.applicationContext) }
    
    // Cleanup TTS when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }
    
    // Get localized strings from HomeViewModel (assuming we need language context)
    val homeViewModel: HomeViewModel = hiltViewModel()
    val localizedStrings by homeViewModel.localizedStrings.collectAsStateWithLifecycle()

    LaunchedEffect(articleUrl) {
        articleUrl?.let { url ->
            viewModel.loadArticleByUrl(url)
        }
        // Check subscription status
        isSubscribed = viewModel.paymentService.isSubscriptionActive()
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = localizedStrings["news_detail"] ?: "News Detail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = localizedStrings["back"] ?: "Back"
                    )
                }
            },
            actions = {
                uiState.article?.let { article ->
                    // Bookmark button
                    IconButton(
                        onClick = {
                            viewModel.toggleBookmark()
                        }
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Share button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, article.title)
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${article.title}\n\n${article.description}\n\nRead more: ${article.url}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share News"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = localizedStrings["share"] ?: "Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Content based on state
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            uiState.article != null -> {
                ArticleContent(
                    article = uiState.article!!,
                    isSubscribed = isSubscribed,
                    localizedStrings = localizedStrings,
                    ttsManager = ttsManager,
                    onSubscribeClick = { showSubscriptionDialog = true },
                    onReadFullArticle = { article ->
                        if (isSubscribed) {
                            onOpenWebView(article.url, article.title)
                        } else {
                            showSubscriptionDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                )
            }
        }
        
        // Subscription Dialog
        if (showSubscriptionDialog) {
            SubscriptionDialog(
                onDismiss = { showSubscriptionDialog = false },
                onSubscriptionSuccess = {
                    isSubscribed = true
                    showSubscriptionDialog = false
                },
                localizedStrings = localizedStrings
            )
        }
    }
}

@Composable
private fun ArticleContent(
    article: Article,
    isSubscribed: Boolean,
    localizedStrings: Map<String, String>,
    ttsManager: TextToSpeechManager,
    onSubscribeClick: () -> Unit,
    onReadFullArticle: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(modifier = modifier) {
            // Article Image
            article.urlToImage?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Article Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Article Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Article Meta Information
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source
                Text(
                    text = article.source.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                // Published Date
                Text(
                    text = formatDate(article.publishedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Author
            article.author?.let { author ->
                Text(
                    text = "By $author",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Article Description
            article.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Article Content
            article.content?.let { content ->
                Text(
                    text = content.replace("[+\\d+ chars]".toRegex(), ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // TTS Controls for Accessibility
            TTSControls(
                ttsManager = ttsManager,
                articleTitle = article.title,
                articleDescription = article.description,
                articleContent = article.content?.replace("[+\\d+ chars]".toRegex(), ""),
                localizedStrings = localizedStrings,
                isCompact = false,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Subscription Banner (if not subscribed)
            if (!isSubscribed) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = localizedStrings["get_premium_access"] ?: "Get Premium Access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = localizedStrings["subscribe_description"] ?: "Subscribe to read full articles in WebView with no ads",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onSubscribeClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(localizedStrings["subscribe_now"] ?: "Subscribe Now")
                        }
                    }
                }
            }

            // Read Full Article Button
            if (isSubscribed) {
                Button(
                    onClick = { onReadFullArticle(article) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = localizedStrings["premium_article"] ?: "🌟 Read Full Article (Premium)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onSubscribeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizedStrings["subscribe_for_full_article"] ?: "Subscribe for Full Article",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom Spacing
            Spacer(modifier = Modifier.height(24.dp))
        }
    }


private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}