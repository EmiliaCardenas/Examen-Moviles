package com.example.examen.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExamenResponseDto(
    @SerializedName("puzzle") val puzzle: List<List<Int?>>,
    @SerializedName("solution") val solution: List<List<Int>>
)
