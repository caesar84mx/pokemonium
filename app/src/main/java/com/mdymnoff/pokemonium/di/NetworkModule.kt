package com.mdymnoff.pokemonium.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mdymnoff.pokemonium.BuildConfig
import com.mdymnoff.pokemonium.data.api_service.PokemonApiService
import com.mdymnoff.pokemonium.util.Constants.POKEMON_CONNECT_TIMEOUT_SECONDS
import com.mdymnoff.pokemonium.util.Constants.POKEMON_READ_TIMEOUT_SECONDS
import com.mdymnoff.pokemonium.util.Constants.CONTENT_TYPE_JSON
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
    
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(POKEMON_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(POKEMON_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory(CONTENT_TYPE_JSON.toMediaType()))
            .build()
    }
    
    single<PokemonApiService> { get<Retrofit>().create(PokemonApiService::class.java) }
}
