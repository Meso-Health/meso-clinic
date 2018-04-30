package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Diagnosis

interface DiagnosisRepository {
    fun all(): Single<List<Diagnosis>>
    fun fetch(): Completable
}
