package com.carlosribeiro.quickjot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.carlosribeiro.quickjot.ui.QuickJotNavHost
import com.carlosribeiro.quickjot.ui.theme.QuickJotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            QuickJotTheme {
                QuickJotNavHost()
            }
        }
    }
}
