package com.example.examen.data.local.preferences

import android.content.Context
import com.google.gson.Gson
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

}