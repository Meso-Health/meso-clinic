package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Member

interface EnrollmentPeriodRepository {
    fun fetch(): Completable
    fun shouldEnroll(member: Member): Single<Boolean>
}
