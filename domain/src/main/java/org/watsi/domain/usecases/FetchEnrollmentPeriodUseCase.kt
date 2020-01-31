package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.repositories.EnrollmentPeriodRepository

class FetchEnrollmentPeriodUseCase(private val enrollmentPeriodRepository: EnrollmentPeriodRepository) {

    fun execute(): Completable {
        return enrollmentPeriodRepository.fetch()
    }
}
