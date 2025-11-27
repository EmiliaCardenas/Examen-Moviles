package com.example.examen.presentation.screens


data class SudokuScreenUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val puzzle: List<List<Int?>>? = null,
    val solution: List<List<Int>>? = null,
    val userInput: List<MutableList<Int?>> = List(9) { MutableList(9) { null } },
    val error: String? = null,
    val verificationMessage: String? = null,
    val boardSize: Int = 9
)

