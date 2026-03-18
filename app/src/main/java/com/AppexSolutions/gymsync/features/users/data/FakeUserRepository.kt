package com.AppexSolutions.gymsync.features.users.data

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.users.domain.entities.MemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.MembershipPlan
import com.AppexSolutions.gymsync.features.users.domain.entities.PlanIcon
import com.AppexSolutions.gymsync.features.users.domain.entities.PlanStatus

class FakeUserRepository {

    /** Lista de clientes mock — compartida con el admin */
    val mockClients: List<Client> = listOf(
        Client(
            id = 1, nombres = "Sofía", apellidos = "Martínez",
            email = "sofia@gym.com", telefono = "555-1234",
            fechaNacimiento = "1995-03-15", activo = true,
            rolId = 3, rolNombre = "Cliente",
            gymId = 1, gymNombre = "GymSync Central"
        ),
        Client(
            id = 2, nombres = "Carlos", apellidos = "López",
            email = "carlos@gym.com", telefono = "555-5678",
            fechaNacimiento = "1990-07-22", activo = true,
            rolId = 3, rolNombre = "Cliente",
            gymId = 1, gymNombre = "GymSync Central"
        ),
        Client(
            id = 3, nombres = "María", apellidos = "García",
            email = "maria@gym.com", telefono = "555-9012",
            fechaNacimiento = "1998-11-08", activo = true,
            rolId = 3, rolNombre = "Cliente",
            gymId = 1, gymNombre = "GymSync Central"
        ),
        Client(
            id = 4, nombres = "Diego", apellidos = "Hernández",
            email = "diego@gym.com", telefono = "555-3456",
            fechaNacimiento = "1992-01-30", activo = false,
            rolId = 3, rolNombre = "Cliente",
            gymId = 2, gymNombre = "GymSync Norte"
        )
    )

    /** Perfiles de miembro con datos mock extendidos */
    private val mockProfiles = mapOf(
        1 to MemberProfile(
            id = 1, nombres = "Sofía", apellidos = "Martínez",
            email = "sofia@gym.com", telefono = "555-1234",
            currentPlan = "Premium", planStatus = PlanStatus.ACTIVO,
            nextPaymentDate = "10 Abril 2026", daysRemaining = 28,
            currentStreak = 12, monthlyVisits = 18,
            qrCode = "GYMSYNC-USR-001-2026"
        ),
        2 to MemberProfile(
            id = 2, nombres = "Carlos", apellidos = "López",
            email = "carlos@gym.com", telefono = "555-5678",
            currentPlan = "Pro", planStatus = PlanStatus.ACTIVO,
            nextPaymentDate = "25 Marzo 2026", daysRemaining = 7,
            currentStreak = 5, monthlyVisits = 10,
            qrCode = "GYMSYNC-USR-002-2026"
        ),
        3 to MemberProfile(
            id = 3, nombres = "María", apellidos = "García",
            email = "maria@gym.com", telefono = "555-9012",
            currentPlan = "Ultimate", planStatus = PlanStatus.ACTIVO,
            nextPaymentDate = "5 Mayo 2026", daysRemaining = 48,
            currentStreak = 30, monthlyVisits = 22,
            qrCode = "GYMSYNC-USR-003-2026"
        ),
        4 to MemberProfile(
            id = 4, nombres = "Diego", apellidos = "Hernández",
            email = "diego@gym.com", telefono = "555-3456",
            currentPlan = "Pro", planStatus = PlanStatus.VENCIDO,
            nextPaymentDate = "—", daysRemaining = 0,
            currentStreak = 0, monthlyVisits = 0,
            qrCode = "GYMSYNC-USR-004-2026"
        )
    )

    /** Planes de membresía */
    val membershipPlans: List<MembershipPlan> = listOf(
        MembershipPlan(
            id = 1,
            name = "Pro",
            description = "Para quienes toman en serio su fitness",
            pricePerMonth = 45,
            iconType = PlanIcon.BOLT,
            features = listOf(
                "Acceso ilimitado al gimnasio",
                "2 clases grupales a la semana",
                "Casillero personal",
                "Wi-Fi de alta velocidad",
                "Acceso a zona de cardio",
                "Ducha y vestuarios"
            )
        ),
        MembershipPlan(
            id = 2,
            name = "Ultimate",
            description = "Experiencia premium completa",
            pricePerMonth = 70,
            isPopular = true,
            iconType = PlanIcon.CROWN,
            features = listOf(
                "Todo lo incluido en Pro",
                "Clases grupales ilimitadas",
                "Entrenador personal (2 sesiones/mes)",
                "Plan nutricional personalizado",
                "Acceso a zona VIP y sauna",
                "Estacionamiento gratuito",
                "Toalla y amenidades incluidas",
                "Invitaciones para amigos (2/mes)"
            )
        )
    )

    fun getProfileForClient(clientId: Int): MemberProfile? = mockProfiles[clientId]

    fun getDefaultProfile(): MemberProfile = mockProfiles[1]!!

    /**
     * Busca un cliente activo por email (case-insensitive).
     * Retorna el Client si existe y está activo, null si no.
     */
    fun findActiveClientByEmail(email: String): Client? =
        mockClients.firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) && it.activo
        }
}
