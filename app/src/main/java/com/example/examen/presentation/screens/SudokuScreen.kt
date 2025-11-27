package com.example.examen.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.examen.presentation.theme.ErrorRed
import com.example.examen.presentation.theme.Pink
import com.example.examen.presentation.theme.PinkLight
import com.example.examen.presentation.theme.Purple
import com.example.examen.presentation.theme.PurpleLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    viewModel: SudokuScreenViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.loadSudoku()
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

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = Purple)
            }

            uiState.error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = ErrorRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadSudoku() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            uiState.puzzle != null -> {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        "Sudoku (muy) Feo",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Purple,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SudokuGrid(
                        puzzle = uiState.puzzle,
                        userInput = uiState.userInput,
                        onValueChange = { r, c, v ->
                            viewModel.updateCell(r, c, v)
                        },
                        incorrectCells = viewModel.incorrectCells
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    uiState.verificationMessage?.let {
                        Text(
                            it,
                            color = Purple,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {

                        Button(
                            onClick = { viewModel.verifySudoku() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Verificar")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.resetPuzzle() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Pink,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Limpiar")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.newSudoku() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurpleLight,
                                contentColor = Purple
                            )
                        ) {
                            Text("Nuevo")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SudokuGrid(
    puzzle: List<List<Int?>>,
    userInput: List<MutableList<Int?>>,
    onValueChange: (row: Int, col: Int, value: Int?) -> Unit,
    incorrectCells: Set<Pair<Int, Int>>
) {
    Column {
        for (row in 0 until 9) {
            Row {
                for (col in 0 until 9) {

                    val isFixed = puzzle[row][col] != null
                    val cellValue = if (isFixed) puzzle[row][col] else userInput[row][col]
                    val isIncorrect = !isFixed && incorrectCells.contains(row to col)

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .border(
                                width = 1.dp,
                                color = Purple,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                when {
                                    isIncorrect -> ErrorRed.copy(alpha = 0.7f)
                                    isFixed -> PurpleLight.copy(alpha = 0.6f)
                                    else -> PinkLight.copy(alpha = 0.6f)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        if (isFixed && cellValue != null) {
                            Text(
                                text = cellValue.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Purple
                                )
                            )
                        } else if (!isFixed) {

                            var text by remember(cellValue) {
                                mutableStateOf(cellValue?.toString() ?: "")
                            }

                            LaunchedEffect(cellValue) {
                                text = cellValue?.toString() ?: ""
                            }

                            BasicTextField(
                                value = text,
                                onValueChange = { newText ->
                                    if (newText.length <= 1) {
                                        val num = newText.toIntOrNull()
                                        if (num == null || num in 1..9) {
                                            text = newText
                                            onValueChange(row, col, num)
                                        } else if (newText.isEmpty()) {
                                            text = ""
                                            onValueChange(row, col, null)
                                        }
                                    }
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    color = Pink,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .fillMaxSize(),
                                decorationBox = { inner ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        inner()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
