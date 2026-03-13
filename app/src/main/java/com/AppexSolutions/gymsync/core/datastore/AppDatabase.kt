package com.AppexSolutions.gymsync.core.datastore

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserEntity::class, ClientProfilePhotoEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profilePhotoDao(): ProfilePhotoDao

    companion object {
        /** Migración v1 → v2: agrega tabla de fotos de perfil de clientes */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS client_profile_photos (
                        client_id INTEGER NOT NULL PRIMARY KEY,
                        photo_local_uri TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
