package com.privatevault.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.privatevault.core.ImportMode
import com.privatevault.core.LinkType

@Database(
    entities = [
        LibraryEntity::class,
        MovieEntity::class,
        MovieImageEntity::class,
        MovieLinkEntity::class,
        TagEntity::class,
        MovieTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(VaultTypeConverters::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao

    companion object {
        fun open(context: Context): VaultDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
                "private_vault.db"
            ).build()
        }
    }
}

class VaultTypeConverters {
    @TypeConverter
    fun importModeToString(value: ImportMode): String = value.name

    @TypeConverter
    fun stringToImportMode(value: String): ImportMode = ImportMode.valueOf(value)

    @TypeConverter
    fun linkTypeToString(value: LinkType): String = value.name

    @TypeConverter
    fun stringToLinkType(value: String): LinkType = LinkType.valueOf(value)
}
