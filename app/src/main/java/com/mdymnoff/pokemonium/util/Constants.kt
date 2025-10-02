package com.mdymnoff.pokemonium.util

internal object Constants {
    const val CACHE_EXPIRY_TIME_MS = 5 * 60 * 1000L // 5 minutes

    const val PAGE_SIZE = 10
    
    // Database table names
    const val DATABASE_NAME = "pokemon_database"
    const val TABLE_POKEMON_CARDS = "pokemon_cards"
    const val TABLE_CACHE_METADATA = "cache_metadata"
    
    // Cache keys
    const val POKEMON_CACHE_KEY = "pokemon_cache"
    
    // API endpoints and parameters
    const val POKEMON_CONNECT_TIMEOUT_SECONDS = 20L
    const val POKEMON_READ_TIMEOUT_SECONDS = 20L
    const val CONTENT_TYPE_JSON = "application/json"
    
    // URL parsing constants
    const val URL_PARTS_ID_INDEX_FROM_END = 2
    
    // Error messages
    const val ERROR_UNKNOWN = "Unknown error occurred"
    const val ERROR_LOAD_MORE_POKEMON = "Failed to load more pokemon"}