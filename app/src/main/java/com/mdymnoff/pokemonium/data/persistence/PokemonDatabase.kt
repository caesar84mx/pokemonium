package com.mdymnoff.pokemonium.data.persistence

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.mdymnoff.pokemonium.data.model.entities.CacheMetadataEntity
import com.mdymnoff.pokemonium.data.model.entities.PokemonCardEntity
import com.mdymnoff.pokemonium.data.dao.PokemonDao
import com.mdymnoff.pokemonium.util.Constants.DATABASE_NAME

@Database(
    entities = [PokemonCardEntity::class, CacheMetadataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {
    
    abstract fun pokemonDao(): PokemonDao
    
    companion object {
        fun create(context: Context): PokemonDatabase {
            return Room.databaseBuilder(
                context,
                PokemonDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}
