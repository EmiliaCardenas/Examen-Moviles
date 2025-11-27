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
        val dto = api.getSudoku(
            apiKey = "U6DBlvqcjB91MMQZFWbwqQ==CmvttfN5sHKonxJT\n",
            width = width, // prederteminado 3
            height = height, // prederterminado 3
            difficulty = difficulty, // prederterminado medium
            seed = seed
        )

        return dto.toDomain()
    }
}