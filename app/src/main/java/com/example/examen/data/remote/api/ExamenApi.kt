package com.example.examen.data.remote.api

import com.example.examen.data.remote.dto.ExamenResponseDto
import com.example.examen.data.remote.dto.SudokuSolutionRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ExamenApi {

    @GET("sudokugenerate")
    suspend fun getSudoku(
        @Header("X-Api-Key") apiKey: String,
        @Query("width") width: Int? = null,
        @Query("height") height: Int? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("seed") seed: String? = null
    ): ExamenResponseDto

    @GET("sudokusolve")
    suspend fun solveSudoku(
        @Header("X-Api-Key") apiKey: String,
        @Query("puzzle") puzzle: String,
        @Query("width") width: Int,
        @Query("height") height: Int
    ): ExamenResponseDto
}
