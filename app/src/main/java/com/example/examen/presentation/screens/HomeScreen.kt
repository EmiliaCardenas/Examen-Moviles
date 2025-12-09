package com.example.examen.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.examen.presentation.navigation.Screen
import com.example.examen.presentation.theme.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    // Refrescar cuando se vuelve a esta pantalla
    LaunchedEffect(Unit) {
        viewModel.checkSavedGame()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PinkVeryLight, BlueVeryLight, PurpleVeryLight),
                    startY = 0f,
                    endY = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Título
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(120.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SUDOKU (menos) FEO",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = PurpleDark,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón de continuar partida (solo si hay guardada)
            if (uiState.isGameSaved) {
                MenuButton(
                    text = "CONTINUAR PARTIDA",
                    icon = Icons.Filled.PlayArrow,
                    backgroundColor = PinkLight,
                    textColor = Color.White,
                    onClick = {
                        // Esto usará el caché automáticamente porque carga
                        // desde las preferences que ya guardaste
                        navController.navigate(Screen.SudokuScreen.createRoute(difficulty = "load_saved"))
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            // Dificultades para nuevas partidas
            MenuButton(
                text = "FÁCIL",
                backgroundColor = Blue,
                textColor = Color.White,
                onClick = {
                    navController.navigate(Screen.SudokuScreen.createRoute("easy"))
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            MenuButton(
                text = "MEDIO",
                backgroundColor = Purple,
                textColor = Color.White,
                onClick = {
                    navController.navigate(Screen.SudokuScreen.createRoute("medium"))
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            MenuButton(
                text = "DIFÍCIL",
                backgroundColor = PinkDark,
                textColor = Color.White,
                onClick = {
                    navController.navigate(Screen.SudokuScreen.createRoute("hard"))
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector? = null,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(60.dp)
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}