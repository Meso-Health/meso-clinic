package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.repositories.MainRepository

class DeleteUserDataUseCase(private val mainRepository: MainRepository) {
    fun execute(): Completable {
        return mainRepository.deleteAllUserData()
    }
}
