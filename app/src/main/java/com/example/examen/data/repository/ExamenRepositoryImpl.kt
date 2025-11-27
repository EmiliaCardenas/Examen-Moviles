package com.example.examen.data.repository

import com.example.examen.data.remote.api.ExamenApi
import com.example.examen.domain.repository.ExamenRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ExamenRepositoryImpl @Inject constructor(
    private val api: ExamenApi
) : ExamenRepository {
    /*
    *
    *override suspend fun getPokemonList(): List<Pokemon> {
            val response = api.getPokemonList()
            return response.results.map { result ->
                // Obtenemos el id de la URL
                val id =
                    result.url
                        .split("/")
                        .dropLast(1)
                        .last()
                // Hacemos la llamada para obtener detalles
                api.getPokemon(id).toDomain()
            }
        }
    *
    * */
    // override suspend fun nombre(): respuesta {}
}