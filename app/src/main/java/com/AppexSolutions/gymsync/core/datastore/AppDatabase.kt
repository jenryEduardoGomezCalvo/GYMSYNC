package com.AppexSolutions.gymsync.core.datastore

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.AppexSolutions.gymsync.features.routines.data.local.dao.ExerciseDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineDayDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineExerciseDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineHistoryDao
import com.AppexSolutions.gymsync.features.routines.data.local.entity.ExerciseEntity
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineDayEntity
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineEntity
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineExerciseEntity
import com.AppexSolutions.gymsync.features.progress.data.local.dao.ProgressDao
import com.AppexSolutions.gymsync.features.progress.data.local.entity.ProgressEntryEntity
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineHistoryEntity

@Database(
    entities = [
        UserEntity::class,
        ClientProfilePhotoEntity::class,
        AttendanceEntity::class,
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineDayEntity::class,
        RoutineExerciseEntity::class,
        RoutineHistoryEntity::class,
        ProgressEntryEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profilePhotoDao(): ProfilePhotoDao
    abstract fun attendanceDao(): AttendanceDao

    // Rutinas
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun routineDayDao(): RoutineDayDao
    abstract fun routineExerciseDao(): RoutineExerciseDao
    abstract fun routineHistoryDao(): RoutineHistoryDao

    // Progreso físico
    abstract fun progressDao(): ProgressDao

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

        /** Migración v5 → v6: agrega columna image_url a exercises (si no existe ya) */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val cursor = db.query("PRAGMA table_info(exercises)")
                var hasImageUrl = false
                while (cursor.moveToNext()) {
                    val idx = cursor.getColumnIndex("name")
                    if (idx >= 0 && cursor.getString(idx) == "image_url") {
                        hasImageUrl = true
                        break
                    }
                }
                cursor.close()
                if (!hasImageUrl) {
                    db.execSQL("ALTER TABLE exercises ADD COLUMN image_url TEXT")
                }
            }
        }

        /** Migración v6 → v7: agrega tabla de seguimiento de progreso físico */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS progress_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id INTEGER NOT NULL,
                        recorded_at INTEGER NOT NULL,
                        weight REAL,
                        waist REAL,
                        hips REAL,
                        chest REAL,
                        arms REAL,
                        photo_uri TEXT,
                        notes TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        /** Migración v7 → v8: agrega columna photo_url a client_profile_photos para Supabase Storage */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE client_profile_photos ADD COLUMN photo_url TEXT"
                )
            }
        }

        /** Migración v4 → v5: agrega tablas del sistema de rutinas */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercises (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        wger_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        muscle_group TEXT NOT NULL,
                        description TEXT NOT NULL,
                        image_url TEXT,
                        cached_at INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_exercises_wger_id ON exercises(wger_id)"
                )

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS routines (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        notification_hour INTEGER NOT NULL DEFAULT 8,
                        notification_minute INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS routine_days (
                        routine_id INTEGER NOT NULL,
                        day_of_week INTEGER NOT NULL,
                        PRIMARY KEY (routine_id, day_of_week),
                        FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS routine_exercises (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        routine_id INTEGER NOT NULL,
                        exercise_id INTEGER NOT NULL,
                        sets INTEGER NOT NULL DEFAULT 3,
                        reps INTEGER NOT NULL DEFAULT 10,
                        rest_secs INTEGER NOT NULL DEFAULT 60,
                        sort_order INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE,
                        FOREIGN KEY (exercise_id) REFERENCES exercises(id)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS routine_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        routine_id INTEGER NOT NULL,
                        routine_name TEXT NOT NULL,
                        user_id INTEGER NOT NULL,
                        completed_at INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
