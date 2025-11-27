package com.example.examen.data.repository

import com.example.examen.data.local.preferences.ExamenPreferences
import com.example.examen.data.remote.api.ExamenApi
import com.example.examen.domain.repository.ExamenRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ExamenRepositoryImpl @Inject constructor(
    private val api: ExamenApi,
    private val preferences: ExamenPreferences,
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
        *
        *  override suspend fun getPokemonList(): List<Pokemon> {
        // Intentar obtener del caché primero
        preferences.getPokemonCache()?.let { cache ->
            if (preferences.isCacheValid()) {
                return cache.pokemonList
            }
        }

        return try {
            // Si no hay caché o expiró, obtener de la API
            val response = api.getPokemonList()
            val pokemonList =
                response.results.map { result ->
                    val id =
                        result.url
                            .split("/")
                            .dropLast(1)
                            .last()
                    api.getPokemon(id).toDomain()
                }

            // Guardar en caché
            preferences.savePokemonList(
                pokemonList = pokemonList,
                offset = pokemonList.size,
                totalCount = response.count,
            )

            pokemonList
        } catch (e: Exception) {
            // Si hay error, intentar usar caché aunque haya expirado
            preferences.getPokemonCache()?.let { cache ->
                return cache.pokemonList
            } ?: throw e
        }
    }
    *
    * */
    // override suspend fun nombre(): respuesta {}
}