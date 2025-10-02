package com.mdymnoff.pokemonium.di

import org.koin.dsl.module

val appModule = module {
    includes(
        networkModule,
        databaseModule,
        repositoryModule,
        viewModelModule
    )
}
