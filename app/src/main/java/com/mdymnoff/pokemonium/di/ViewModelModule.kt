package com.mdymnoff.pokemonium.di

import com.mdymnoff.pokemonium.viewmodels.PokemonViewModel
import com.mdymnoff.pokemonium.viewmodels.PokemonViewModelImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel<PokemonViewModel> { PokemonViewModelImpl(repository = get()) }
}
