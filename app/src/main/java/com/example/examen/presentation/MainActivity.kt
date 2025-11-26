package com.example.examen.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.examen.presentation.theme.ExamenTheme
import dagger.hilt.android.AndroidEntryPoint

// Se borraron los @Greeting

// Copy paste por si acaso: @Suppress("ktlint:standard:function-naming")
// Copy paste en casi necesario: @OptIn(ExperimentalMaterial3Api::class)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExamenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                    }
                }
            }
        }
    }
}
