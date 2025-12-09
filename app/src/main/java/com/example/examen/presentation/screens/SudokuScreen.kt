package com.example.examen.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.examen.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    navController: NavController,
    viewModel: SudokuScreenViewModel = hiltViewModel(),
    difficulty: String
) {
    val uiState = viewModel.uiState.collectAsState().value
    val incorrectCells by remember { derivedStateOf { viewModel.incorrectCells } }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PinkVeryLight, BlueVeryLight, PurpleVeryLight),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        TopAppBar(
            title = {
                Text(
                    "Sudoku (menos) Feo",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PurpleDark
                    )
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(PurpleLight, shape = RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = PurpleDark
            ),
            modifier = Modifier.shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Purple,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Cargando",
                            style = MaterialTheme.typography.titleMedium,
                            color = PurpleDark
                        )
                    }
                }

                uiState.error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = ErrorRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error,
                            color = ErrorRed,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadSudoku() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 4.dp
                            ),
                            modifier = Modifier
                                .height(50.dp)
                                .width(200.dp)
                        ) {
                            Text(
                                "Reintentar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                uiState.puzzle != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        GameControls(
                            currentSize = uiState.boardSize,
                            currentDifficulty = difficulty,
                            onSizeSelected = { newSize ->
                                viewModel.changeBoardSize(newSize, difficulty)
                            },
                            onNewGame = { viewModel.newSudoku(difficulty) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SudokuBoard(
                            puzzle = uiState.puzzle,
                            userInput = uiState.userInput,
                            onValueChange = { r, c, v ->
                                viewModel.updateCell(r, c, v)
                            },
                            incorrectCells = incorrectCells,
                            boardSize = uiState.boardSize
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        uiState.verificationMessage?.let { message ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        message.contains("¡Felicidades!") -> Green.copy(alpha = 0.8f)
                                        message.contains("¡Bien!") -> BlueLight.copy(alpha = 0.8f)
                                        else -> ErrorRedLight.copy(alpha = 0.8f)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = when {
                                        message.contains("¡Felicidades!") -> Color.White
                                        message.contains("¡Bien!") -> BlueDark
                                        else -> ErrorRed
                                    },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ActionButtons(
                            onVerify = { viewModel.verifySudoku() },
                            onClear = { viewModel.clearErrors() },
                            onNewGame = { viewModel.newSudoku(difficulty) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GameControls(
    currentSize: Int,
    currentDifficulty: String,
    onSizeSelected: (Int) -> Unit,
    onNewGame: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = PurpleDark,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tamaño del tablero:",
                style = MaterialTheme.typography.titleSmall.copy(color = Purple),
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                BoardSizeButton(
                    size = 9,
                    isSelected = currentSize == 9,
                    onClick = { onSizeSelected(9) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                BoardSizeButton(
                    size = 4,
                    isSelected = currentSize == 4,
                    onClick = { onSizeSelected(4) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Dificultad: ${currentDifficulty.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleSmall.copy(color = PinkDark)
            )
        }
    }
}

@Composable
fun BoardSizeButton(
    size: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Purple else PurpleLight.copy(alpha = 0.3f)
    val textColor = if (isSelected) Color.White else Purple

    ElevatedButton(
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        modifier = modifier
    ) {
        Text(
            text = "${size}x$size",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun ActionButtons(
    onVerify: () -> Unit,
    onClear: () -> Unit,
    onNewGame: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        ActionButton(
            text = "Verificar",
            backgroundColor = Purple,
            textColor = Color.White,
            onClick = onVerify,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "Limpiar",
            backgroundColor = PinkLight,
            textColor = PinkDark,
            onClick = onClear,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "Nuevo",
            backgroundColor = BlueLight,
            textColor = BlueDark,
            icon = Icons.Filled.Refresh,
            onClick = onNewGame,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
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
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        modifier = modifier.height(48.dp)
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleSmall.fontSize * 0.9f
            )
        )
    }
}

@Composable
fun SudokuBoard(
    puzzle: List<List<Int?>>,
    userInput: List<MutableList<Int?>>,
    onValueChange: (row: Int, col: Int, value: Int?) -> Unit,
    incorrectCells: Set<Pair<Int, Int>>,
    boardSize: Int
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    val cellSizeDp = if (boardSize == 9) {
        val maxBoardWidth = screenWidthDp * 0.85f
        val calculatedSize = (maxBoardWidth / boardSize)
        calculatedSize.coerceIn(28.dp, 36.dp)
    } else {
        val maxBoardWidth = screenWidthDp * 0.8f
        val calculatedSize = (maxBoardWidth / boardSize)
        calculatedSize.coerceIn(45.dp, 60.dp)
    }

    val textStyle = if (boardSize == 9) {
        MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold
        )
    } else {
        MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        )
    }

    val userTextStyle = if (boardSize == 9) {
        MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium
        )
    } else {
        MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Medium
        )
    }

    val subGridSize = if (boardSize == 9) 3 else 2
    val focusManager = LocalFocusManager.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = SudokuThickLine,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .background(Color.White)
        ) {
            for (row in 0 until boardSize) {
                Row {
                    for (col in 0 until boardSize) {
                        val isFixed = puzzle[row][col] != null
                        val cellValue = if (isFixed) puzzle[row][col] else userInput[row][col]
                        val isIncorrect = !isFixed && incorrectCells.contains(row to col)

                        val borderEnd = if ((col + 1) % subGridSize == 0 && col != boardSize - 1)
                            2.dp else 0.5.dp
                        val borderBottom = if ((row + 1) % subGridSize == 0 && row != boardSize - 1)
                            2.dp else 0.5.dp

                        val backgroundColor = when {
                            isIncorrect -> SudokuErrorBackground
                            isFixed -> BlueVeryLight.copy(alpha = 0.15f)
                            else -> Color.White
                        }

                        val textColor = when {
                            isIncorrect -> SudokuErrorText
                            isFixed -> SudokuFixedNumber
                            else -> SudokuUserNumber
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSizeDp)
                                .border(
                                    width = borderEnd,
                                    color = if (borderEnd == 2.dp) SudokuThickLine else SudokuGridLine,
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .border(
                                    width = borderBottom,
                                    color = if (borderBottom == 2.dp) SudokuThickLine else SudokuGridLine,
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .background(backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFixed && cellValue != null) {
                                Text(
                                    text = cellValue.toString(),
                                    style = textStyle.copy(color = textColor),
                                    fontSize = if (boardSize == 9) 18.sp else 24.sp
                                )
                            } else {
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
                                            val maxValue = boardSize
                                            if (num == null || num in 1..maxValue) {
                                                text = newText
                                                onValueChange(row, col, num)
                                                if (num != null && col < boardSize - 1) {
                                                    focusManager.moveFocus(FocusDirection.Right)
                                                }
                                            } else if (newText.isEmpty()) {
                                                text = ""
                                                onValueChange(row, col, null)
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = userTextStyle.copy(
                                        color = textColor,
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            focusManager.moveFocus(FocusDirection.Right)
                                        }
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (text.isEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(if (boardSize == 9) 2.dp else 3.dp)
                                                        .background(PinkLight.copy(alpha = 0.2f))
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}