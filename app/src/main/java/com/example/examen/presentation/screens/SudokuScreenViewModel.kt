package com.example.examen.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen.domain.model.Modelo
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

    fun loadSudoku(
        width: Int? = null,
        height: Int? = null,
        difficulty: String? = null,
        seed: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = SudokuScreenUiState(isLoading = true)

            try {
                val modelo = repository.getSudoku(width, height, difficulty, seed)

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
                "❌ Hay errores en el Sudoku. Revisa las celdas marcadas."
            else
                "✔ Sudoku completado correctamente"
        )
    }

    fun resetPuzzle() {
        val basePuzzle = _uiState.value.puzzle ?: return

        _incorrectCells.clear()

        _uiState.value = _uiState.value.copy(
            userInput = basePuzzle.map { it.toMutableList() },
            verificationMessage = null
        )
    }

    fun newSudoku() {
        _incorrectCells.clear()

        _uiState.value = _uiState.value.copy(
            verificationMessage = null,
            error = null
        )

        loadSudoku()
    }

}

