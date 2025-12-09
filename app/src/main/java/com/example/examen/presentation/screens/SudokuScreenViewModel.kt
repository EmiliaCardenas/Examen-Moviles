package com.example.examen.presentation.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen.data.local.preferences.ExamenPreferences
import com.example.examen.domain.repository.ExamenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SudokuScreenViewModel @Inject constructor(
    private val repository: ExamenRepository,
    private val preferences: ExamenPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(SudokuScreenUiState())
    val uiState: StateFlow<SudokuScreenUiState> = _uiState

    private val _incorrectCells = mutableSetOf<Pair<Int, Int>>()
    val incorrectCells: Set<Pair<Int, Int>> get() = _incorrectCells

    private val _duplicateCells = mutableSetOf<Pair<Int, Int>>()
    val duplicateCells: Set<Pair<Int, Int>> get() = _duplicateCells

    private var currentDifficulty: String = "medium"
    private var currentBoardSize: Int = 9

    fun loadSudoku(
        selectedDifficulty: String? = null,
        seed: String? = null,
        loadSavedGame: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = SudokuScreenUiState(isLoading = true)

            if (selectedDifficulty != null) {
                currentDifficulty = selectedDifficulty.lowercase()
            }

            try {
                if (loadSavedGame) {
                    val savedProgress = preferences.getGameProgress()
                    if (savedProgress != null) {
                        currentDifficulty = savedProgress.difficulty
                        currentBoardSize = savedProgress.boardSize

                        _uiState.value = SudokuScreenUiState(
                            isLoading = false,
                            isSuccess = true,
                            puzzle = savedProgress.puzzle,
                            solution = null,
                            userInput = savedProgress.userInput.map { it.toMutableList() },
                            boardSize = currentBoardSize
                        )
                        clearErrorStates()
                        preferences.clearGameProgress()
                        return@launch
                    }
                }

                val apiDifficulty = when (currentDifficulty) {
                    "easy" -> "easy"
                    "medium" -> "medium"
                    "hard" -> "hard"
                    else -> "medium"
                }

                val (width, height) = when (currentBoardSize) {
                    4 -> 2 to 2
                    9 -> 3 to 3
                    else -> 3 to 3
                }

                val modelo = repository.getSudoku(
                    width = width,
                    height = height,
                    difficulty = apiDifficulty,
                    seed = seed
                )

                _uiState.value = SudokuScreenUiState(
                    isLoading = false,
                    isSuccess = true,
                    puzzle = modelo.puzzle,
                    solution = modelo.solution,
                    userInput = modelo.puzzle.map { row ->
                        row.map { if (it == 0) null else it }.toMutableList()
                    },
                    boardSize = currentBoardSize
                )

                clearErrorStates()

            } catch (e: Exception) {
                _uiState.value = SudokuScreenUiState(
                    isLoading = false,
                    error = e.message ?: "Error al cargar el Sudoku",
                    boardSize = currentBoardSize
                )
            }
        }
    }

    fun saveGame() {
        val state = _uiState.value
        if (state.isSuccess && state.puzzle != null) {
            preferences.saveGameProgress(
                puzzle = state.puzzle,
                userInput = state.userInput,
                boardSize = state.boardSize,
                difficulty = currentDifficulty
            )
        }
    }

    fun isGameSaved(): Boolean {
        return preferences.getGameProgress() != null
    }

    fun changeBoardSize(newSize: Int, difficulty: String) {
        if (newSize != currentBoardSize) {
            currentBoardSize = newSize
            loadSudoku(difficulty)
        }
    }

    fun updateCell(row: Int, col: Int, value: Int?) {
        val currentState = _uiState.value
        val basePuzzle = currentState.puzzle ?: return
        val boardSize = currentState.boardSize

        // Verificar si es celda fija
        val isFixedCell = basePuzzle.getOrNull(row)?.getOrNull(col) != 0 &&
                basePuzzle.getOrNull(row)?.getOrNull(col) != null

        if (isFixedCell) {
            return
        }

        // Validar rango
        if (value != null && (value < 1 || value > boardSize)) {
            return
        }

        val newGrid = currentState.userInput.map { it.toMutableList() }
        newGrid[row][col] = value

        // Actualizar estado
        _uiState.value = currentState.copy(userInput = newGrid)

        // Limpiar errores de esta celda al actualizar
        _incorrectCells.remove(row to col)
        _duplicateCells.remove(row to col)

        // Validar duplicados automáticamente al actualizar
        validateDuplicates()
    }

    // Un solo botón para verificar todo
    fun verifySudoku() {
        viewModelScope.launch {
            val state = _uiState.value
            val userBoard = state.userInput
            val boardSize = state.boardSize

            // Limpiar estados previos
            _duplicateCells.clear()
            _incorrectCells.clear()

            // 1. Primero validar duplicados localmente
            val duplicateValidation = validateBoardForDuplicates(userBoard, boardSize)
            if (duplicateValidation.hasDuplicates) {
                _duplicateCells.addAll(duplicateValidation.duplicateCells)
                _uiState.value = state.copy(
                    verificationMessage = "Hay números duplicados en el tablero."
                )
                return@launch
            }

            // 2. Si no hay duplicados, verificar con API
            // Convertir el board del usuario para enviar a la API
            val boardToSend: List<List<Int>> = userBoard.map { row ->
                row.map { cell -> cell ?: 0 }
            }

            val (width, height) = when (boardSize) {
                4 -> 2 to 2
                9 -> 3 to 3
                else -> 3 to 3
            }

            try {
                val response = repository.solveSudoku(
                    currentBoard = boardToSend,
                    width = width,
                    height = height
                )

                // Manejar el caso de "unsolvable"
                if (response.status == "unsolvable") {
                    _uiState.value = state.copy(
                        verificationMessage = "El Sudoku tiene errores. Revisa los números."
                    )
                    return@launch
                }

                val apiSolution = response.solution

                if (apiSolution == null) {
                    _uiState.value = state.copy(
                        verificationMessage = "No se pudo verificar la solución."
                    )
                    return@launch
                }

                var hasErrors = false
                var isComplete = true
                var correctCells = 0
                var totalCells = 0

                // Comparar cada celda
                for (i in 0 until boardSize) {
                    for (j in 0 until boardSize) {
                        val userVal = userBoard[i][j]
                        val solutionVal = apiSolution[i][j]

                        // Solo contar celdas que deben ser llenadas
                        if (state.puzzle?.get(i)?.get(j) == 0 ||
                            state.puzzle?.get(i)?.get(j) == null) {
                            totalCells++

                            // Verificar si está completo
                            if (userVal == null) {
                                isComplete = false
                            } else {
                                // Si el usuario puso un valor y es correcto
                                if (userVal == solutionVal) {
                                    correctCells++
                                } else {
                                    // Marcar como incorrecto
                                    _incorrectCells.add(i to j)
                                    hasErrors = true
                                }
                            }
                        }
                    }
                }

                // Determinar el mensaje apropiado
                val message = when {
                    hasErrors -> "Hay errores ($correctCells/$totalCells correctos)."
                    !isComplete -> "¡Bien! ($correctCells/$totalCells correctos). Completa las celdas vacías."
                    else -> "¡Felicidades! Sudoku completado correctamente."
                }

                _uiState.value = state.copy(
                    verificationMessage = message
                )

            } catch (e: Exception) {
                _uiState.value = state.copy(
                    verificationMessage = "Error de conexión. Intenta más tarde."
                )
            }
        }
    }

    private fun validateDuplicates() {
        val state = _uiState.value
        val userBoard = state.userInput
        val boardSize = state.boardSize

        val validationResult = validateBoardForDuplicates(userBoard, boardSize)

        _duplicateCells.clear()
        if (validationResult.hasDuplicates) {
            _duplicateCells.addAll(validationResult.duplicateCells)
        }
    }

    private fun validateBoardForDuplicates(
        board: List<List<Int?>>,
        boardSize: Int
    ): DuplicateValidationResult {
        val duplicateCells = mutableSetOf<Pair<Int, Int>>()

        // Validar filas
        for (i in 0 until boardSize) {
            val seenInRow = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
            for (j in 0 until boardSize) {
                val value = board[i][j]
                if (value != null) {
                    seenInRow.getOrPut(value) { mutableListOf() }.add(i to j)
                }
            }

            // Marcar duplicados en fila
            for ((value, positions) in seenInRow) {
                if (positions.size > 1) {
                    duplicateCells.addAll(positions)
                }
            }
        }

        // Validar columnas
        for (j in 0 until boardSize) {
            val seenInCol = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
            for (i in 0 until boardSize) {
                val value = board[i][j]
                if (value != null) {
                    seenInCol.getOrPut(value) { mutableListOf() }.add(i to j)
                }
            }

            // Marcar duplicados en columna
            for ((value, positions) in seenInCol) {
                if (positions.size > 1) {
                    duplicateCells.addAll(positions)
                }
            }
        }

        // Validar cajas (para Sudoku 9x9)
        if (boardSize == 9) {
            for (boxRow in 0 until 3) {
                for (boxCol in 0 until 3) {
                    val seenInBox = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
                    for (i in boxRow*3 until (boxRow+1)*3) {
                        for (j in boxCol*3 until (boxCol+1)*3) {
                            val value = board[i][j]
                            if (value != null) {
                                seenInBox.getOrPut(value) { mutableListOf() }.add(i to j)
                            }
                        }
                    }

                    // Marcar duplicados en caja
                    for ((value, positions) in seenInBox) {
                        if (positions.size > 1) {
                            duplicateCells.addAll(positions)
                        }
                    }
                }
            }
        }

        return DuplicateValidationResult(
            hasDuplicates = duplicateCells.isNotEmpty(),
            duplicateCells = duplicateCells
        )
    }

    fun clearErrors() {
        clearErrorStates()
        _uiState.value = _uiState.value.copy(
            verificationMessage = null,
            error = null
        )
    }

    fun newSudoku(difficulty: String = currentDifficulty) {
        clearErrorStates()
        _uiState.value = _uiState.value.copy(
            verificationMessage = null,
            error = null
        )
        loadSudoku(selectedDifficulty = difficulty)
    }

    private fun clearErrorStates() {
        _incorrectCells.clear()
        _duplicateCells.clear()
    }
}

data class DuplicateValidationResult(
    val hasDuplicates: Boolean,
    val duplicateCells: Set<Pair<Int, Int>>
)

