package com.example.examen.data.repository

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
            val cache = preferences.getSudokuCache()
            if (cache != null && cache.sudoku.isNotEmpty()) {
                return cache.sudoku.first()
            } else {
                throw e
            }
        }
    }

    override suspend fun solveSudoku(
        currentBoard: List<List<Int>>,
        width: Int,
        height: Int
    ): ExamenResponseDto {

        val apiKey = getApiKey()
        val puzzleJson = gson.toJson(currentBoard)

        val cleanPuzzleJson = puzzleJson.replace(" ", "")

        return try {
            val response = api.solveSudoku(
                apiKey = apiKey,
                puzzle = cleanPuzzleJson,
                width = width,
                height = height
            )
            response

        } catch (e: Exception) {
            throw e
        }
    }
}