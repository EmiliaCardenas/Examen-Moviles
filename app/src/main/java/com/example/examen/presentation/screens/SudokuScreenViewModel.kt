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
        val isFixedCell = basePuzzle.getOrNull(row)?.getOrNull(col) != 0 &&
                basePuzzle.getOrNull(row)?.getOrNull(col) != null
        if (isFixedCell) {
            return
        }
        if (value != null && (value < 1 || value > boardSize)) {
            return
        }

        val newGrid = currentState.userInput.map { it.toMutableList() }
        newGrid[row][col] = value
        _uiState.value = currentState.copy(userInput = newGrid)
        _incorrectCells.remove(row to col)
    }

    fun verifySudoku() {
        viewModelScope.launch {
            val state = _uiState.value
            val userBoard = state.userInput
            val boardSize = state.boardSize
            val puzzle = state.puzzle ?: return@launch

            _incorrectCells.clear()

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

                if (response.status == "unsolvable") {
                    findAllIncorrectCells(userBoard, puzzle, boardSize)
                    _uiState.value = state.copy(
                        verificationMessage = "Revisa los números en rojo."
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

                for (i in 0 until boardSize) {
                    for (j in 0 until boardSize) {
                        val userVal = userBoard[i][j]
                        val solutionVal = apiSolution[i][j]
                        val isOriginalEmpty = puzzle[i][j] == 0 || puzzle[i][j] == null

                        if (isOriginalEmpty) {
                            if (userVal == null) {
                                isComplete = false
                            } else {
                                if (userVal != solutionVal) {
                                    _incorrectCells.add(i to j)
                                    hasErrors = true
                                }
                            }
                        }
                    }
                }

                val message = when {
                    hasErrors -> "Revisa los números en rojo (${_incorrectCells.size} errores)."
                    !isComplete -> "Continúa completando el tablero."
                    else -> "¡Felicidades! Sudoku completado correctamente."
                }

                _uiState.value = state.copy(
                    verificationMessage = message
                )

            } catch (e: Exception) {
                _uiState.value = state.copy(
                    verificationMessage = "Error de conexión."
                )
            }
        }
    }

    private fun findAllIncorrectCells(
        board: List<List<Int?>>,
        puzzle: List<List<Int?>>,
        boardSize: Int
    ) {
        val incorrectSet = mutableSetOf<Pair<Int, Int>>()

        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                val value = board[i][j]
                val isFixed = puzzle[i][j] != null && puzzle[i][j] != 0

                if (!isFixed && value != null) {
                    if (value < 1 || value > boardSize) {
                        incorrectSet.add(i to j)
                    }
                }
            }
        }

        val fullBoard = mutableListOf<MutableList<Int?>>()
        for (i in 0 until boardSize) {
            val row = mutableListOf<Int?>()
            for (j in 0 until boardSize) {
                val isFixed = puzzle[i][j] != null && puzzle[i][j] != 0
                row.add(if (isFixed) puzzle[i][j] else board[i][j])
            }
            fullBoard.add(row)
        }

        for (i in 0 until boardSize) {
            val seen = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
            for (j in 0 until boardSize) {
                val value = fullBoard[i][j]
                if (value != null) {
                    seen.getOrPut(value) { mutableListOf() }.add(i to j)
                }
            }

            for ((_, positions) in seen) {
                if (positions.size > 1) {
                    incorrectSet.addAll(positions)
                }
            }
        }

        for (j in 0 until boardSize) {
            val seen = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
            for (i in 0 until boardSize) {
                val value = fullBoard[i][j]
                if (value != null) {
                    seen.getOrPut(value) { mutableListOf() }.add(i to j)
                }
            }
            for ((_, positions) in seen) {
                if (positions.size > 1) {
                    incorrectSet.addAll(positions)
                }
            }
        }

        val subGridSize = if (boardSize == 9) 3 else 2

        for (boxRow in 0 until subGridSize) {
            for (boxCol in 0 until subGridSize) {
                val seen = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
                for (i in boxRow * subGridSize until (boxRow + 1) * subGridSize) {
                    for (j in boxCol * subGridSize until (boxCol + 1) * subGridSize) {
                        val value = fullBoard[i][j]
                        if (value != null) {
                            seen.getOrPut(value) { mutableListOf() }.add(i to j)
                        }
                    }
                }
                for ((_, positions) in seen) {
                    if (positions.size > 1) {
                        incorrectSet.addAll(positions)
                    }
                }
            }
        }

        val filteredSet = incorrectSet.filterNot { (row, col) ->
            val isFixed = puzzle[row][col] != null && puzzle[row][col] != 0
            isFixed
        }.toSet()

        _incorrectCells.addAll(filteredSet)
    }

    fun clearErrors() {
        val currentState = _uiState.value
        val userInput = currentState.userInput
        val newUserInput = userInput.map { it.toMutableList() }

        for ((row, col) in _incorrectCells) {
            val isFixed = currentState.puzzle?.getOrNull(row)?.getOrNull(col) != 0 &&
                    currentState.puzzle?.getOrNull(row)?.getOrNull(col) != null

            if (!isFixed) {
                newUserInput[row][col] = null
            }
        }

        clearErrorStates()

        _uiState.value = currentState.copy(
            userInput = newUserInput,
            verificationMessage = null,
            error = null
        )
    }

    fun newSudoku(difficulty: String = currentDifficulty) {
        clearErrorStates()
        _uiState.value = SudokuScreenUiState(
            isLoading = true,
            boardSize = currentBoardSize
        )
        loadSudoku(selectedDifficulty = difficulty)
    }

    private fun clearErrorStates() {
        _incorrectCells.clear()
    }
}

