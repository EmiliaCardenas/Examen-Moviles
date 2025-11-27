package com.example.examen.data.repository

import com.example.examen.data.local.preferences.ExamenPreferences
import com.example.examen.data.remote.api.ExamenApi
import com.example.examen.domain.model.Modelo
import com.example.examen.domain.repository.ExamenRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import toDomain

@Singleton
class ExamenRepositoryImpl @Inject constructor(
    private val api: ExamenApi,
    private val preferences: ExamenPreferences,
) : ExamenRepository {

    override suspend fun getSudoku(
        width: Int?,
        height: Int?,
        difficulty: String?,
        seed: String?
    ): Modelo {

        return try {

            val dto = api.getSudoku(
                apiKey = "U6DBlvqcjB91MMQZFWbwqQ==CmvttfN5sHKonxJT",
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
}