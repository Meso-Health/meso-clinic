package org.watsi.domain.repositories

import io.reactivex.Completable
import java.util.UUID

interface ReferralRepository {
    fun delete(referralId: UUID): Completable
}
