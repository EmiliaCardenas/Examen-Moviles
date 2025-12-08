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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    navController: NavController,
    viewModel: SudokuScreenViewModel = hiltViewModel(),
    difficulty: String
) {
    val uiState = viewModel.uiState.collectAsState().value
    LaunchedEffect(difficulty) {
        if (difficulty == "load_saved") {
            viewModel.loadSudoku(loadSavedGame = true)
        } else {
            viewModel.loadSudoku(difficulty)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveGame()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Sudoku") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PurpleLight,
                titleContentColor = Purple,
                navigationIconContentColor = Purple
            )
        )

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

                        BoardSizeSelector(
                            currentSize = uiState.boardSize,
                            onSizeSelected = { newSize ->
                                viewModel.changeBoardSize(newSize, difficulty)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SudokuGrid(
                            puzzle = uiState.puzzle,
                            userInput = uiState.userInput,
                            onValueChange = { r, c, v ->
                                viewModel.updateCell(r, c, v)
                            },
                            incorrectCells = viewModel.incorrectCells,
                            boardSize = uiState.boardSize
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
                                onClick = { viewModel.verifySudokuFromApi() },
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
                                onClick = { viewModel.newSudoku(difficulty) },
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
}

@Composable
fun BoardSizeSelector(
    currentSize: Int,
    onSizeSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tamaño:",
            style = MaterialTheme.typography.titleMedium.copy(color = Purple),
            modifier = Modifier.padding(end = 16.dp)
        )

        FilterChip(
            selected = currentSize == 9,
            onClick = { onSizeSelected(9) },
            label = { Text("9x9") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Purple,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = PinkLight,
                labelColor = Purple
            )
        )

        Spacer(Modifier.width(8.dp))

        FilterChip(
            selected = currentSize == 4,
            onClick = { onSizeSelected(4) },
            label = { Text("4x4") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Purple,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = PinkLight,
                labelColor = Purple
            )
        )
    }
}

@Composable
fun SudokuGrid(
    puzzle: List<List<Int?>>,
    userInput: List<MutableList<Int?>>,
    onValueChange: (row: Int, col: Int, value: Int?) -> Unit,
    incorrectCells: Set<Pair<Int, Int>>,
    boardSize: Int
) {
    val cellSize = if (boardSize == 9) 36.dp else 48.dp
    val textStyle = if (boardSize == 9) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleMedium
    val subGridSize = if (boardSize == 9) 3 else 2

    Box(
        modifier = Modifier
            .padding(16.dp)
            .border(
                width = 3.dp,
                color = Purple,
                shape = RoundedCornerShape(4.dp)
            )
            .background(MaterialTheme.colorScheme.background)
            .padding(4.dp)
    ) {
        Column {
            for (row in 0 until boardSize) {
                Row {
                    for (col in 0 until boardSize) {
                        val isFixed = puzzle[row][col] != null
                        val cellValue = if (isFixed) puzzle[row][col] else userInput[row][col]
                        val isIncorrect = !isFixed && incorrectCells.contains(row to col)

                        // Determinar bordes más gruesos para los sub-grids
                        val borderEnd = if ((col + 1) % subGridSize == 0 && col != boardSize - 1) 2.dp else 1.dp
                        val borderBottom = if ((row + 1) % subGridSize == 0 && row != boardSize - 1) 2.dp else 1.dp

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .border(
                                    width = borderEnd,
                                    color = Purple.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .border(
                                    width = borderBottom,
                                    color = Purple.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .background(
                                    when {
                                        isIncorrect -> ErrorRed.copy(alpha = 0.3f)
                                        isFixed -> PurpleLight.copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.background
                                    }
                                )
                                .padding(1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFixed && cellValue != null) {
                                Text(
                                    text = cellValue.toString(),
                                    style = textStyle.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Purple
                                    )
                                )
                            } else {
                                var text by remember(cellValue) { mutableStateOf(cellValue?.toString() ?: "") }

                                LaunchedEffect(cellValue) {
                                    text = cellValue?.toString() ?: ""
                                }

                                BasicTextField(
                                    value = text,
                                    onValueChange = { newText ->
                                        if (newText.length <= 1) {
                                            val num = newText.toIntOrNull()
                                            val maxValue = if (boardSize == 4) 4 else 9
                                            if (num == null || num in 1..maxValue) {
                                                text = newText
                                                onValueChange(row, col, num)
                                            } else if (newText.isEmpty()) {
                                                text = ""
                                                onValueChange(row, col, null)
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = textStyle.copy(
                                        color = if (isIncorrect) ErrorRed else Pink,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.fillMaxSize(),
                                    decorationBox = { inner ->
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) { inner() }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}