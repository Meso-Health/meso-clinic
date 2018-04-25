package org.watsi.uhp.services

import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.domain.repositories.MemberRepository

import javax.inject.Inject

/**
 * Service class that polls the UHP API and updates the device with updated member and billables data
 */
class FetchService : AbstractSyncJobService() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var billableRepository: BillableRepository
    @Inject lateinit var diagnosisRepository: DiagnosisRepository

    override fun performSync(): Boolean {
        memberRepository.fetch()
        billableRepository.fetch()
        return true
    }
}
