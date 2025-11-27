package com.example.examen.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExamenRequestDto (

    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("seed") val seed: String?
)