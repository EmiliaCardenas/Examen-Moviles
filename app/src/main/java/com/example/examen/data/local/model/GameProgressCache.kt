package com.example.examen.data.local.model

data class GameProgressCache(
    val puzzle: List<List<Int?>>,
    val userInput: List<List<Int?>>,
    val boardSize: Int,
    val difficulty: String,
    val savedAt: Long
)