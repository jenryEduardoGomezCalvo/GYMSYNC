package com.AppexSolutions.gymsync.features.users.data

import com.AppexSolutions.gymsync.features.users.domain.entities.MemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.MembershipPlan
import com.AppexSolutions.gymsync.features.users.domain.repositories.UserDataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserDataRepository @Inject constructor() : UserDataRepository {

    private val fake = FakeUserRepository()

    override suspend fun getMemberProfile(userId: Int): MemberProfile {
        return fake.getProfileForClient(userId) ?: fake.getDefaultProfile()
    }

    override suspend fun getMembershipPlans(): List<MembershipPlan> {
        return fake.membershipPlans
    }
}
