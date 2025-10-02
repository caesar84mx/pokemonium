package com.mdymnoff.pokemonium.di

import com.mdymnoff.pokemonium.data.persistence.PokemonDatabase
import com.mdymnoff.pokemonium.data.dao.PokemonDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<PokemonDatabase> { PokemonDatabase.create(androidContext()) }
    
    single<PokemonDao> { get<PokemonDatabase>().pokemonDao() }
}
