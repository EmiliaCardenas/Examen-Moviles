package com.example.examen.domain.repository

import com.example.examen.domain.model.Modelo

interface ExamenRepository {

    suspend fun getSudoku(
        width: Int? = null,
        height: Int? = null,
        difficulty: String? = null,
        seed: String? = null
    ): Modelo
}
