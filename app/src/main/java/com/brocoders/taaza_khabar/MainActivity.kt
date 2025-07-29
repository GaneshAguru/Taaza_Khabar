package com.brocoders.taaza_khabar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.brocoders.taaza_khabar.navigation.NewsNavigation
import com.brocoders.taaza_khabar.ui.theme.Taaza_KhabarTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Taaza_KhabarTheme {
                SetupSystemUI()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NewsNavigation()
                }
            }
        }
    }
}

@Composable
private fun SetupSystemUI() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !androidx.compose.foundation.isSystemInDarkTheme()

    systemUiController.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = useDarkIcons
    )
}