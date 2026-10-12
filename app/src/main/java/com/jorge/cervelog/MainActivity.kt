package com.jorge.cervelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jorge.cervelog.ui.navigation.AppNavigation
import com.jorge.cervelog.ui.theme.CerveLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CerveLogTheme {
                AppNavigation()
            }
        }
    }
}
