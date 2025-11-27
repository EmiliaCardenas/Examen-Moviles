package com.example.examen.domain.usecase

import com.example.examen.domain.model.Modelo
import com.example.examen.domain.repository.ExamenRepository
import jakarta.inject.Inject
import com.example.examen.domain.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ExamenUseCase @Inject constructor(
    private val repository: ExamenRepository
) {
    operator fun invoke(id: String): Flow<Result<Modelo>> =
        flow {
            try {
                emit(Result.Loading)
                val examen = repository.getSudoku()
                emit(Result.Success(examen))
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }
}