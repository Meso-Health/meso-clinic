package org.watsi.domain.usecases

import io.reactivex.Single
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.EnrollmentPeriodRepository

class ShouldEnrollUseCase(private val enrollmentPeriodRepository: EnrollmentPeriodRepository) {

    fun execute(member: Member): Single<Boolean> {
        return enrollmentPeriodRepository.shouldEnroll(member)
    }
}
