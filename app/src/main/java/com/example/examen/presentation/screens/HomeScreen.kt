package com.example.examen.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.examen.presentation.navigation.Screen
import com.example.examen.presentation.theme.PinkLight
import com.example.examen.presentation.theme.PurpleLight
import com.example.examen.presentation.theme.Purple
import com.example.examen.presentation.theme.Pink
import androidx.compose.foundation.border


@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.checkSavedGame()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PinkLight, PurpleLight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Sudoku (muy) Feo",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Purple
                )
            )
            if (uiState.isGameSaved) {
                ContinueGameButton {
                    navController.navigate(Screen.SudokuScreen.createRoute(difficulty = "load_saved"))
                }
            }

            DifficultyButton("Easy") {
                navController.navigate(Screen.SudokuScreen.createRoute("easy"))
            }

            DifficultyButton("Medium") {
                navController.navigate(Screen.SudokuScreen.createRoute("medium"))
            }

            DifficultyButton("Hard") {
                navController.navigate(Screen.SudokuScreen.createRoute("hard"))
            }
        }
    }
}

@Composable
fun DifficultyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(50.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun ContinueGameButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Pink,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(50.dp)
            .border(2.dp, Purple, RoundedCornerShape(12.dp))
    ) {
        Text(text = "Continuar Partida", style = MaterialTheme.typography.titleMedium)
    }
}