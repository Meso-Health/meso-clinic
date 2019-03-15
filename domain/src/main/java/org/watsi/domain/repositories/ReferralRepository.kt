package org.watsi.domain.repositories

import io.reactivex.Completable

interface ReferralRepository {
    fun deleteAll(): Completable
}