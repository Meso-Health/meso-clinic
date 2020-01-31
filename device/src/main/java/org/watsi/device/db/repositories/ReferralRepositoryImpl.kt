package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.db.daos.ReferralDao
import org.watsi.domain.repositories.ReferralRepository
import java.util.UUID

class ReferralRepositoryImpl(
    private val referralDao: ReferralDao
) : ReferralRepository {
    override fun delete(referralId: UUID): Completable {
        return Completable.fromAction {
            referralDao.delete(referralId)
        }.subscribeOn(Schedulers.io())
    }
}
