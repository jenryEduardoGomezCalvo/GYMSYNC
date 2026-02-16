package com.AppexSolutions.gymsync.features.clients.data.datasource.remote.mapper

import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.ClientDto
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.GymDto
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.RolDto
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.entities.Gym
import com.AppexSolutions.gymsync.features.clients.domain.entities.Rol

fun ClientDto.toDomain(): Client = Client(
    id = id,
    nombres = nombres,
    apellidos = apellidos,
    email = email,
    telefono = telefono,
    fechaNacimiento = fechaNacimiento,
    activo = activo,
    rolId = rolId,
    rolNombre = rol?.nombre ?: "desconocido",
    gymId = gymId,
    gymNombre = gym?.nombre
)

fun RolDto.toDomain(): Rol = Rol(id = id, nombre = nombre, descripcion = descripcion)

fun GymDto.toDomain(): Gym = Gym(id = id, nombre = nombre)
