package com.mdymnoff.pokemonium.di

import com.mdymnoff.pokemonium.data.repositories.PokemonRepository
import com.mdymnoff.pokemonium.data.repositories.PokemonRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<PokemonRepository> { PokemonRepositoryImpl(apiService = get(), pokemonDao = get()) }
}
