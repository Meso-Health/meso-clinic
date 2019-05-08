package org.watsi.domain.repositories

import io.reactivex.Completable

interface MainRepository {
    fun deleteAllUserData(): Completable
}
