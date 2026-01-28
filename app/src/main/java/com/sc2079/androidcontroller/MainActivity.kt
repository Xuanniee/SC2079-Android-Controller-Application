package com.sc2079.androidcontroller

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.stringResource
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.features.language.presentation.LocaleState
import com.sc2079.androidcontroller.ui.AppScaffold
import com.sc2079.androidcontroller.ui.screens.LoadingScreen
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize locale state from current app locale
        LocaleState.initFromCurrentLocale()
        
        enableEdgeToEdge()
        setContent {
            SC2079AndroidControllerApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
private fun MainContent() {
    val isChangingLanguage by LocaleState.isChangingLanguage
    
    Box(modifier = Modifier.fillMaxSize()) {
        val navController = rememberNavController()
        AppScaffold(navController = navController)
        
        // Show loading screen overlay when language is changing
        if (isChangingLanguage) {
            LoadingScreen(
                message = stringResource(R.string.changing_language),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
