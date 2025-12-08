package com.example.examen.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SudokuSolutionRequestDto(
    @SerializedName("puzzle") val currentBoard: List<List<Int?>>
)