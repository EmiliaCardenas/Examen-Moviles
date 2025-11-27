package com.example.examen.data.local.model

import com.example.examen.domain.model.Modelo

data class ExamenCache(
    val sudoku: List<Modelo>,
    val lastUpdate: Long,
    val offset: Int,
    val totalCount: Int,
)