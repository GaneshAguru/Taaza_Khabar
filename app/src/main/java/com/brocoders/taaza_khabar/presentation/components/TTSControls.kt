package com.brocoders.taaza_khabar.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brocoders.taaza_khabar.data.service.TTSState
import com.brocoders.taaza_khabar.data.service.TextToSpeechManager

@Composable
fun TTSControls(
    ttsManager: TextToSpeechManager,
    articleTitle: String,
    articleDescription: String?,
    articleContent: String? = null,
    localizedStrings: Map<String, String> = mapOf(),
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val ttsState by ttsManager.ttsState.collectAsStateWithLifecycle()
    val isInitializing by ttsManager.isInitializing.collectAsStateWithLifecycle()
    val progress by ttsManager.progress.collectAsStateWithLifecycle()

    // Animation for speaking indicator
    val infiniteTransition = rememberInfiniteTransition(label = "speaking")
    val speakingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakingAlpha"
    )

    if (isCompact) {
        // Compact version for smaller spaces
        CompactTTSControls(
            ttsManager = ttsManager,
            ttsState = ttsState,
            isInitializing = isInitializing,
            speakingAlpha = speakingAlpha,
            articleTitle = articleTitle,
            articleDescription = articleDescription,
            articleContent = articleContent,
            localizedStrings = localizedStrings,
            modifier = modifier
        )
    } else {
        // Full version with progress indicator
        FullTTSControls(
            ttsManager = ttsManager,
            ttsState = ttsState,
            isInitializing = isInitializing,
            progress = progress,
            speakingAlpha = speakingAlpha,
            articleTitle = articleTitle,
            articleDescription = articleDescription,
            articleContent = articleContent,
            localizedStrings = localizedStrings,
            modifier = modifier
        )
    }
}

@Composable
private fun CompactTTSControls(
    ttsManager: TextToSpeechManager,
    ttsState: TTSState,
    isInitializing: Boolean,
    speakingAlpha: Float,
    articleTitle: String,
    articleDescription: String?,
    articleContent: String?,
    localizedStrings: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                isInitializing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                ttsState == TTSState.SPEAKING -> {
                    IconButton(
                        onClick = { ttsManager.stopSpeaking() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .semantics { 
                                contentDescription = localizedStrings["stop_reading"] ?: "Stop reading article"
                                role = Role.Button
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = speakingAlpha),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                else -> {
                    IconButton(
                        onClick = {
                            ttsManager.speakArticle(
                                title = articleTitle,
                                description = articleDescription,
                                content = articleContent
                            )
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .semantics { 
                                contentDescription = localizedStrings["listen_to_article"] ?: "Listen to article"
                                role = Role.Button
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (ttsState == TTSState.SPEAKING) {
                Text(
                    text = localizedStrings["reading"] ?: "Reading...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = localizedStrings["listen"] ?: "Listen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FullTTSControls(
    ttsManager: TextToSpeechManager,
    ttsState: TTSState,
    isInitializing: Boolean,
    progress: Float,
    speakingAlpha: Float,
    articleTitle: String,
    articleDescription: String?,
    articleContent: String?,
    localizedStrings: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizedStrings["voice_assistant"] ?: "Voice Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (ttsState) {
                            TTSState.SPEAKING -> localizedStrings["reading_article"] ?: "Reading article..."
                            TTSState.PAUSED -> localizedStrings["paused"] ?: "Paused"
                            TTSState.ERROR -> localizedStrings["tts_error"] ?: "Error occurred"
                            else -> localizedStrings["ready_to_read"] ?: "Ready to read article"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Progress indicator
            if (ttsState == TTSState.SPEAKING && progress > 0) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isInitializing -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = localizedStrings["initializing"] ?: "Initializing...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    ttsState == TTSState.SPEAKING -> {
                        // Stop button
                        FilledTonalButton(
                            onClick = { ttsManager.stopSpeaking() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.semantics { 
                                contentDescription = localizedStrings["stop_reading"] ?: "Stop reading article"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localizedStrings["stop"] ?: "Stop",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        // Speaking indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = speakingAlpha),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = localizedStrings["now_reading"] ?: "Now reading...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = speakingAlpha),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    else -> {
                        // Play button
                        Button(
                            onClick = {
                                ttsManager.speakArticle(
                                    title = articleTitle,
                                    description = articleDescription,
                                    content = articleContent
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.semantics { 
                                contentDescription = localizedStrings["listen_to_article"] ?: "Listen to article"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(localizedStrings["listen_to_article"] ?: "Listen to Article")
                        }

                        // Additional info
                        Text(
                            text = localizedStrings["accessibility_feature"] ?: "Accessibility feature for visually impaired users",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
} 