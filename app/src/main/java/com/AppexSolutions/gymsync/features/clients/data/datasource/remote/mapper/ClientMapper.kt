package com.AppexSolutions.gymsync.features.clients.data.datasource.remote.mapper

import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.ClientDto
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.entities.ClientStatus
import com.AppexSolutions.gymsync.features.clients.domain.entities.MembershipType

fun ClientDto.toDomain(): Client {
    return Client(
        id = id,
        name = "$nombres $apellidos",
        membershipType = MembershipType.BASICA, // backend aún no lo envía
        status = if (activo) ClientStatus.ACTIVO else ClientStatus.INACTIVO
    )
}
