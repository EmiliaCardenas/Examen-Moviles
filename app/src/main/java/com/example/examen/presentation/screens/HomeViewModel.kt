package com.example.examen.presentation.screens

import androidx.lifecycle.ViewModel
import com.example.examen.data.local.preferences.ExamenPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferences: ExamenPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkSavedGame()
    }

    fun checkSavedGame() {
        val gameProgress = preferences.getGameProgress()
        _uiState.update {
            it.copy(
                isGameSaved = gameProgress != null,
                isLoading = false
            )
        }
    }
}