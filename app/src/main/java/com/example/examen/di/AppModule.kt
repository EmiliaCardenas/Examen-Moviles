package com.example.examen.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /**
     *
     *     @Provides
     *     @Singleton       Obtener el retrofit, y poder utilizar el api
     *     fun provideRetrofit(): Retrofit =
     *         Retrofit
     *             .Builder()
     *             .baseUrl("https://pokeapi.co/api/v2/")                 Url del api
     *             .addConverterFactory(GsonConverterFactory.create())
     *             .build()
     *
     *     @Provides
     *     @Singleton       Obtener el api
     *     fun providePokemonApi(retrofit: Retrofit): PokemonApi = retrofit.create(PokemonApi::class.java)
     *
     *     @Provides
     *     @Singleton      Obtener el repositorio
     *     fun providePokemonRepository(api: PokemonApi): PokemonRepository = PokemonRepositoryImpl(api)
     *
     *
     *
     * */
}