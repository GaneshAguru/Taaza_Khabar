package com.brocoders.taaza_khabar

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.brocoders.taaza_khabar.data.service.PaymentService
import com.brocoders.taaza_khabar.navigation.NewsNavigation
import com.brocoders.taaza_khabar.presentation.splash.SplashScreen
import com.brocoders.taaza_khabar.ui.theme.Taaza_KhabarTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.razorpay.PaymentResultListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultListener {
    
    @Inject
    lateinit var paymentService: PaymentService
    
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        
        setContent {
            // Get initial dark mode preference
            val savedDarkMode = sharedPreferences.getBoolean("dark_mode", isSystemInDarkTheme())
            var isDarkMode by remember { mutableStateOf(savedDarkMode) }
            
            // State to control splash screen visibility
            var showSplashScreen by remember { mutableStateOf(true) }
            
            // Function to toggle dark mode
            val toggleDarkMode = { newDarkMode: Boolean ->
                isDarkMode = newDarkMode
                sharedPreferences.edit().putBoolean("dark_mode", newDarkMode).apply()
            }
            
            if (showSplashScreen) {
                // Show our beautiful custom splash screen
                SplashScreen(onSplashComplete = { 
                    showSplashScreen = false 
                })
            } else {
                // Show main app after splash
                Taaza_KhabarTheme(darkTheme = isDarkMode) {
                    SetupSystemUI(isDarkMode)

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NewsNavigation(
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = toggleDarkMode
                        )
                    }
                }
            }
        }
    }
    
    override fun onPaymentSuccess(paymentId: String) {
        paymentService.handlePaymentSuccess(paymentId)
    }
    
    override fun onPaymentError(code: Int, response: String) {
        paymentService.handlePaymentFailure(code, response)
    }
}

@Composable
private fun SetupSystemUI(isDarkMode: Boolean) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isDarkMode

    systemUiController.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = useDarkIcons
    )
}