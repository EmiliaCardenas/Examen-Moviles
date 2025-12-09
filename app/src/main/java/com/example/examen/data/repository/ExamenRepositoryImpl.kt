package com.example.examen.data.repository

import android.util.Log
import com.example.examen.core.Constants
import com.example.examen.data.local.preferences.ExamenPreferences
import com.example.examen.data.mapper.toDomain
import com.example.examen.data.remote.api.ExamenApi
import com.example.examen.data.remote.dto.ExamenResponseDto
import com.example.examen.domain.model.Modelo
import com.example.examen.domain.repository.ExamenRepository
import com.google.gson.Gson
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ExamenRepositoryImpl @Inject constructor(
    private val api: ExamenApi,
    private val preferences: ExamenPreferences,
    private val gson: Gson
) : ExamenRepository {
    private fun getApiKey(): String {
        return preferences.getApiKey() ?: Constants.SUDOKU_API_KEY
    }

    override suspend fun getSudoku(
        width: Int?,
        height: Int?,
        difficulty: String?,
        seed: String?
    ): Modelo {

        return try {

            val apiKey = getApiKey()

            val dto = api.getSudoku(
                apiKey = apiKey,
                width = width ?: 3,
                height = height ?: 3,
                difficulty = difficulty ?: "medium",
                seed = seed
            )

            val modelo = dto.toDomain()

            preferences.saveSudokuList(
                sudokuList = listOf(modelo),
                offset = 1,
                totalCount = 1,
            )

            modelo
        } catch (e: Exception) {

            preferences.getSudokuCache()?.let { cache ->
                return cache.sudoku.firstOrNull() ?: throw e
            } ?: throw e
        }
    }

    override suspend fun solveSudoku(
        currentBoard: List<List<Int>>,
        width: Int,
        height: Int
    ): ExamenResponseDto {

        val apiKey = getApiKey()

        // Convertir la lista 2D a JSON
        val puzzleJson = gson.toJson(currentBoard)

        Log.d("SUDOKU_API", "Puzzle JSON original: $puzzleJson")

        // Para la API de Ninjas, NO codifiques el JSON adicionalmente
        // Retrofit ya lo codificará automáticamente como query parameter
        // Pero asegurémonos de que no tenga espacios
        val cleanPuzzleJson = puzzleJson.replace(" ", "")

        Log.d("SUDOKU_API", "Puzzle JSON limpio: $cleanPuzzleJson")
        Log.d("SUDOKU_API", "Width: $width, Height: $height")

        return try {
            val response = api.solveSudoku(
                apiKey = apiKey,
                puzzle = cleanPuzzleJson,
                width = width,
                height = height
            )

            Log.d("SUDOKU_API", "Respuesta recibida: $response")
            response

        } catch (e: Exception) {
            Log.e("SUDOKU_API", "Error en la llamada: ${e.message}", e)
            throw e
        }
    }
}