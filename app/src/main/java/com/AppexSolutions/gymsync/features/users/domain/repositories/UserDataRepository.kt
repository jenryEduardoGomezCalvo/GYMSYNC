package com.AppexSolutions.gymsync.features.users.domain.repositories

import com.AppexSolutions.gymsync.features.users.domain.entities.MemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.MembershipPlan

interface UserDataRepository {
    suspend fun getMemberProfile(userId: Int): MemberProfile
    suspend fun getMembershipPlans(): List<MembershipPlan>
}
