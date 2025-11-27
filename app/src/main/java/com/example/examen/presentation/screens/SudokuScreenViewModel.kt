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

    fun loadSudoku(
        width: Int? = null,
        height: Int? = null,
        difficulty: String? = null,
        seed: String? = null
    ) {
        viewModelScope.launch {
            // Estado de carga
            _uiState.value = SudokuScreenUiState(isLoading = true)

            try {
                // Llamada al repository
                repository.getSudoku(
                    width = width,
                    height = height,
                    difficulty = difficulty,
                    seed = seed
                )

                // Éxito
                _uiState.value = SudokuScreenUiState(
                    isLoading = false,
                    isSuccess = true
                )

            } catch (e: Exception) {
                // Error
                _uiState.value = SudokuScreenUiState(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }
}
