package com.example.examen.data.repository

import com.example.examen.data.local.preferences.ExamenPreferences
import com.example.examen.data.mapper.toDomain
import com.example.examen.data.remote.api.ExamenApi
import com.example.examen.data.remote.dto.ExamenResponseDto
import com.example.examen.data.remote.dto.SudokuSolutionRequestDto
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
        return preferences.getApiKey() ?: throw IllegalStateException("API Key not found")
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

        val puzzleJsonString = gson.toJson(currentBoard)

        return api.solveSudoku(
            apiKey = apiKey,
            puzzle = puzzleJsonString,
            width = width,
            height = height
        )
    }
}