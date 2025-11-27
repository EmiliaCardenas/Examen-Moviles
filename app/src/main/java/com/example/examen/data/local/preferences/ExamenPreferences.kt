package com.example.examen.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.examen.data.local.model.ExamenCache
import com.example.examen.data.local.model.GameProgressCache
import com.example.examen.data.local.preferences.PreferencesConstants.CACHE_DURATION
import com.example.examen.data.local.preferences.PreferencesConstants.KEY_EXAMEN_CACHE
import com.example.examen.data.local.preferences.PreferencesConstants.KEY_LAST_UPDATE
import com.example.examen.data.local.preferences.PreferencesConstants.KEY_OFFSET
import com.example.examen.data.local.preferences.PreferencesConstants.KEY_TOTAL_COUNT
import com.example.examen.domain.model.Modelo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamenPreferences
@Inject
constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            PreferencesConstants.PREF_NAME,
            Context.MODE_PRIVATE,
        )

    fun saveSudokuList(
        sudokuList: List<Modelo>,
        offset: Int,
        totalCount: Int,
    ) {
        prefs
            .edit {
                putString(KEY_EXAMEN_CACHE, gson.toJson(sudokuList))
                putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                putInt(KEY_OFFSET, offset)
                putInt(KEY_TOTAL_COUNT, totalCount)
            }
    }

    fun getSudokuCache(): ExamenCache? {
        val json = prefs.getString(KEY_EXAMEN_CACHE, null)
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
        val offset = prefs.getInt(KEY_OFFSET, 0)
        val totalCount = prefs.getInt(KEY_TOTAL_COUNT, 0)

        if (json == null) return null

        val type = object : TypeToken<List<Modelo>>() {}.type
        val sudokuList: List<Modelo> = try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            clearCache()
            return null
        }

        return ExamenCache(
            sudoku = sudokuList,
            lastUpdate = lastUpdate,
            offset = offset,
            totalCount = totalCount,
        )
    }

    fun isCacheValid(): Boolean {
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
        return System.currentTimeMillis() - lastUpdate < CACHE_DURATION
    }

    fun clearCache() {
        prefs.edit().clear().apply()
    }

    fun saveGameProgress(
        puzzle: List<List<Int?>>,
        userInput: List<MutableList<Int?>>,
        boardSize: Int,
        difficulty: String,
    ) {
        val progress = GameProgressCache(
            puzzle = puzzle,
            userInput = userInput.map { it.toList() },
            boardSize = boardSize,
            difficulty = difficulty,
            savedAt = System.currentTimeMillis()
        )

        prefs.edit {

            putString(PreferencesConstants.KEY_GAME_PROGRESS, gson.toJson(progress))
        }
    }

    fun getGameProgress(): GameProgressCache? {
        val json = prefs.getString(PreferencesConstants.KEY_GAME_PROGRESS, null)
        if (json == null) return null

        return try {
            val progress = gson.fromJson(json, GameProgressCache::class.java)
            progress.copy(userInput = progress.userInput.map { it.toMutableList() })
        } catch (e: Exception) {
            clearGameProgress()
            null
        }
    }


    fun clearGameProgress() {
        prefs.edit {
            remove(PreferencesConstants.KEY_GAME_PROGRESS)
        }
    }
}