package com.example.examen.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen.domain.repository.ExamenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SudokuScreenViewModel @Inject constructor(
    private val repository: ExamenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SudokuScreenUiState())
    val uiState: StateFlow<SudokuScreenUiState> = _uiState

    private val _incorrectCells = mutableSetOf<Pair<Int, Int>>()
    val incorrectCells: Set<Pair<Int, Int>> get() = _incorrectCells

    private var currentDifficulty: String = "medium"

    fun loadSudoku(selectedDifficulty: String? = null, seed: String? = null) {
        viewModelScope.launch {
            _uiState.value = SudokuScreenUiState(isLoading = true)

            if (selectedDifficulty != null) {
                currentDifficulty = selectedDifficulty.lowercase()
            }

            try {
                val apiDifficulty = when (currentDifficulty) {
                    "easy" -> "easy"
                    "medium" -> "medium"
                    "hard" -> "hard"
                    else -> "medium"
                }

                val modelo = repository.getSudoku(
                    width = 3,
                    height = 3,
                    difficulty = apiDifficulty,
                    seed = seed
                )

                _uiState.value = SudokuScreenUiState(
                    isLoading = false,
                    isSuccess = true,
                    puzzle = modelo.puzzle,
                    solution = modelo.solution,
                    userInput = modelo.puzzle.map { it.toMutableList() }
                )

            } catch (e: Exception) {
                _uiState.value = SudokuScreenUiState(
                    isLoading = false,
                    error = e.message ?: "Error"
                )
            }
        }
    }

    fun updateCell(row: Int, col: Int, value: Int?) {
        val newGrid = _uiState.value.userInput.map { it.toMutableList() }
        newGrid[row][col] = value
        _uiState.value = _uiState.value.copy(userInput = newGrid)
    }

    fun verifySudoku() {
        val state = _uiState.value
        val solution = state.solution ?: return

        _incorrectCells.clear()

        var hasErrors = false

        for (i in 0 until 9) {
            for (j in 0 until 9) {
                val fixed = state.puzzle!![i][j] != null
                val userVal = state.userInput[i][j]
                val correctVal = solution[i][j]

                if (!fixed && (userVal == null || userVal != correctVal)) {
                    _incorrectCells.add(i to j)
                    hasErrors = true
                }
            }
        }

        _uiState.value = state.copy(
            verificationMessage = if (hasErrors)
                "Hay errores en el Sudoku. Limpialo y sigue jugando"
            else
                "Sudoku completado correctamente"
        )
    }

    fun resetPuzzle() {
        val basePuzzle = _uiState.value.puzzle ?: return
        val currentUserInput = _uiState.value.userInput
        val newGrid = currentUserInput.map { it.toMutableList() }

        for ((row, col) in _incorrectCells) {
            if (basePuzzle[row][col] == null) {
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

