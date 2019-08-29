package org.watsi.domain.usecases

import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository
import java.io.OutputStream

class ExportUnsyncedClaimsUseCase(
    private val deltaRepository: DeltaRepository,
    private val encounterRepository: EncounterRepository
) {
    fun execute(outStream: OutputStream, gson: Gson): Completable {
        return Completable.fromAction {
            val unsyncedEncounterDeltas = deltaRepository.unsynced(Delta.ModelName.ENCOUNTER).blockingGet()
            val unsyncedEncounterIds = unsyncedEncounterDeltas.map { it.modelId }
            val encountersWithExtras = encounterRepository.findAllWithExtras(unsyncedEncounterIds).blockingGet()

            val fileContent = gson.toJson(encountersWithExtras).toByteArray(charset = Charsets.UTF_8)

            outStream.use { out ->
                out.write(fileContent)
            }
        }.subscribeOn(Schedulers.io())
    }
}
