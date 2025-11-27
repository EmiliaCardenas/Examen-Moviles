package com.example.examen.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.error != null -> {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadSudoku() }) {
                        Text("Reintentar")
                    }
                }
            }

            uiState.puzzle != null -> {
                val puzzle = uiState.puzzle

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    SudokuGrid(
                        puzzle = puzzle,
                        userInput = uiState.userInput,
                        onValueChange = { r, c, v ->
                            viewModel.updateCell(r, c, v)
                        },
                        incorrectCells = viewModel.incorrectCells
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    uiState.verificationMessage?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it)
                    }

                    Row {

                        Button(onClick = { viewModel.verifySudoku() }) {
                            Text("Verificar")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(onClick = { viewModel.resetPuzzle() }) {
                            Text("Limpiar Sudoku")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(onClick = { viewModel.newSudoku() }) {
                            Text("Nuevo Sudoku")
                        }
                    }

                    uiState.error?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(it)
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
                    val isIncorrect = !isFixed && incorrectCells.contains(Pair(row, col))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            .background(
                                if (isIncorrect) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        if (isFixed && cellValue != null) {
                            // número original del puzzle - EN NEGRITA
                            Text(
                                text = cellValue.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        } else if (!isFixed) {
                            // celda editable - texto azul y bien centrado
                            var text by remember(cellValue) {
                                mutableStateOf(cellValue?.toString() ?: "")
                            }

                            LaunchedEffect(cellValue) {
                                text = cellValue?.toString() ?: ""
                            }

                            BasicTextField(
                                value = text,
                                onValueChange = { newText ->
                                    // Limitar a un solo carácter
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
                                    color = MaterialTheme.colorScheme.primary, // Color azul
                                    fontWeight = FontWeight.Normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .wrapContentHeight(Alignment.CenterVertically),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        innerTextField()
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