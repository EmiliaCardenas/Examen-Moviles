package com.example.examen.presentation.screens


data class SudokuScreenUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)