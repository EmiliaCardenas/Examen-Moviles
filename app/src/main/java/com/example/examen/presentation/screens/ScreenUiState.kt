package com.example.examen.presentation.screens


data class ScreenUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)