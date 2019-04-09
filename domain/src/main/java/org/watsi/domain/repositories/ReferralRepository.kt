package org.watsi.domain.repositories

import io.reactivex.Completable
import java.util.UUID

interface ReferralRepository {
    fun deleteAll(): Completable
    fun delete(referralId: UUID): Completable
}
