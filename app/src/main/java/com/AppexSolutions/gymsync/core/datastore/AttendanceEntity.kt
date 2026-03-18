package com.AppexSolutions.gymsync.core.datastore

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendances")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "cliente_id")
    val clienteId: String,

    @ColumnInfo(name = "nombre_cliente")
    val nombreCliente: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "fecha")
    val fecha: String
)
