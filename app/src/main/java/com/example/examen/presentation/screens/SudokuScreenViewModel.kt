package com.example.examen.presentation.screens

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
    private var currentDifficulty: String = "medium"
    private var currentBoardSize: Int = 9

    fun loadSudoku(selectedDifficulty: String? = null, seed: String? = null, loadSavedGame: Boolean = false) {
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
                        _incorrectCells.clear()
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
                    userInput = modelo.puzzle.map { it.toMutableList() },
                    boardSize = currentBoardSize
                )

                _incorrectCells.clear()

            } catch (e: Exception) {
                _uiState.value = SudokuScreenUiState(
                    isLoading = false,
                    error = e.message ?: "Error",
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
        val newGrid = currentState.userInput.map { it.toMutableList() }
        newGrid[row][col] = value
        _uiState.value = currentState.copy(userInput = newGrid)
    }

    fun verifySudokuFromApi() {
        viewModelScope.launch {
            val state = _uiState.value
            val userBoard = state.userInput
            val boardSize = state.boardSize

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

                val apiSolution = response.solution

                _incorrectCells.clear()
                var hasErrors = false

                for (i in 0 until boardSize) {
                    for (j in 0 until boardSize) {
                        val userVal = userBoard[i][j]
                        val correctVal = apiSolution[i][j]

                        if (userVal == null || userVal != correctVal) {
                            _incorrectCells.add(i to j)
                            hasErrors = true
                        }
                    }
                }

                _uiState.value = state.copy(
                    verificationMessage = if (hasErrors)
                        "Hay errores en el Sudoku. Limpialo y sigue jugando"
                    else
                        "¡Sudoku completado correctamente! (Verificado por API)"
                )

            } catch (e: Exception) {
                _uiState.value = state.copy(
                    error = "Error al verificar con la API: ${e.message}",
                    verificationMessage = null
                )
            }
        }
    }

    fun resetPuzzle() {
        val basePuzzle = _uiState.value.puzzle ?: return
        val currentUserInput = _uiState.value.userInput
        val boardSize = _uiState.value.boardSize
        val newGrid = currentUserInput.map { it.toMutableList() }

        for ((row, col) in _incorrectCells) {
            if (row < boardSize && col < boardSize && basePuzzle[row][col] == null) {
                newGrid[row][col] = null
            }
        }

        _incorrectCells.clear()
        _uiState.value = _uiState.value.copy(
            userInput = newGrid,
            verificationMessage = null
        )
    }

    fun newSudoku(currentDifficulty: String) {
        _incorrectCells.clear()

        _uiState.value = _uiState.value.copy(
            verificationMessage = null,
            error = null
        )

        loadSudoku(selectedDifficulty = currentDifficulty)
    }
}