package com.brocoders.taaza_khabar.presentation.splash

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brocoders.taaza_khabar.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("SplashScreen", "SplashScreen composable started")
    
    var progress by remember { mutableFloatStateOf(0f) }
    var loadingText by remember { mutableStateOf("Loading...") }
    
    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = EaseOutCubic),
        label = "progress"
    )
    
    // Logo scale animation
    val logoScale by animateFloatAsState(
        targetValue = if (progress > 0f) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "logoScale"
    )
    
    // Simulate loading process
    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "Starting loading sequence")
        val loadingSteps = listOf(
            "Initializing App..." to 0.2f,
            "Loading Resources..." to 0.4f,
            "Setting up Database..." to 0.6f,
            "Fetching Latest News..." to 0.8f,
            "Almost Ready..." to 1f
        )
        
        loadingSteps.forEach { (text, targetProgress) ->
            Log.d("SplashScreen", "Loading step: $text - $targetProgress")
            loadingText = text
            delay(800) // Delay for each step
            progress = targetProgress
            delay(600) // Brief pause after progress update
        }
        
        Log.d("SplashScreen", "Loading complete, calling onSplashComplete")
        delay(1000) // Final pause before navigation
        onSplashComplete()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1565C0), // Deep Blue
                        Color(0xFF1976D2), // Primary Blue
                        Color(0xFF1E88E5), // Medium Blue
                        Color(0xFF42A5F5)  // Light Blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(40.dp)
        ) {
            // App Logo
            Card(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.appicon),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(90.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // App Name
            Text(
                text = "Taaza Khabar",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // App Tagline
            Text(
                text = "Stay Informed. Stay Ahead.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Loading Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Loading Text
                Text(
                    text = loadingText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Progress Bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Progress Fill
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color(0xFFE3F2FD),
                                            Color.White
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Progress Percentage
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Loading dots animation
        LoadingDots(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        )
    }
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingDots")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot$index"
            )
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(scale)
                    .background(Color.White, CircleShape)
            )
        }
    }
} 