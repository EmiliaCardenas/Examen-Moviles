package com.example.examen.di

import android.content.Context
import com.example.examen.data.local.preferences.ExamenPreferences
import com.example.examen.data.remote.api.ExamenApi
import com.example.examen.data.repository.ExamenRepositoryImpl
import com.example.examen.domain.repository.ExamenRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://api.api-ninjas.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideSudokuApi(retrofit: Retrofit): ExamenApi = retrofit.create(ExamenApi::class.java)

    @Provides
    @Singleton
    fun provideSudokuPreferences(
        @ApplicationContext context: Context,
        gson: Gson,
    ): ExamenPreferences = ExamenPreferences(context, gson)

    @Provides
    @Singleton
    fun provideSudokuRepository(
        api: ExamenApi,
        preferences: ExamenPreferences,
        gson: Gson
    ): ExamenRepository = ExamenRepositoryImpl(api, preferences, gson)

}