package com.mdymnoff.pokemonium.data.api_service

import com.mdymnoff.pokemonium.data.model.networking.PokemonPageApi
import retrofit2.http.GET
import retrofit2.http.Query

interface PokemonApiService {
    @GET("pokemon")
    suspend fun getPokemonPage(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): PokemonPageApi
}
