package com.example.examen.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExamenResponseDto(
    @SerializedName("puzzle")
    val puzzle: List<List<Int?>> = emptyList(),

    @SerializedName("solution")
    val solution: List<List<Int>>? = null,

    @SerializedName("difficulty")
    val difficulty: String? = null,

    @SerializedName("seed")
    val seed: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("error")
    val error: String? = null
)