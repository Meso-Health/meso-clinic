package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Diagnosis

interface DiagnosisRepository {
    fun allActive(): Single<List<Diagnosis>>
    fun delete(ids: List<Int>): Completable
    fun countActive(): Single<Int>
    fun fetch(): Completable
    fun find(ids: List<Int>): Single<List<Diagnosis>>
}
