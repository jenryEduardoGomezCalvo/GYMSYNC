package com.AppexSolutions.gymsync.core.datastore

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserEntity::class, ClientProfilePhotoEntity::class, AttendanceEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profilePhotoDao(): ProfilePhotoDao
    abstract fun attendanceDao(): AttendanceDao

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

        /** Migración v2 → v3: agrega tabla de asistencias escaneadas */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS attendances (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cliente_id TEXT NOT NULL,
                        nombre_cliente TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        fecha TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /** Migración v3 → v4: agrega columna fcm_token a tabla users */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE users ADD COLUMN fcm_token TEXT"
                )
            }
        }
    }
}
