package org.watsi.domain.usecases

import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.User
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.utils.DateUtils
import org.watsi.domain.utils.EthiopianDateHelper
import org.watsi.domain.utils.EthiopianDateHelper.formatAsEthiopianDate
import java.io.OutputStream
import java.math.BigDecimal


class ExportUnsyncedClaimsAsTextUseCase(
    private val deltaRepository: DeltaRepository,
    private val encounterRepository: EncounterRepository,
    private val clock: Clock
) {

    // TODO: Took from CurrencyUtil for now until we figure out bring that over to domain module.
    private fun formatEthiopianMoney(amount: Int): String {
        return BigDecimal(amount).setScale(2).divide(BigDecimal(100)).toString()
    }

    private fun rowsForEncounterWithExtras(encounterWithExtras: EncounterWithExtras): List<String> {
        val encounter = encounterWithExtras.encounter
        val referral = encounterWithExtras.referral
        val diagnoses = encounterWithExtras.diagnoses.map { it.description }
        val encounterId = encounter.id
        val claimId = encounter.shortenedClaimId()
        val providerComment = encounter.providerComment
        val reimbursalAmount = formatEthiopianMoney(encounterWithExtras.price())

        val gson = GsonBuilder().setPrettyPrinting().create()
        val member = encounterWithExtras.member
        val memberInfo = linkedMapOf(
            "fullName" to member.name,
            "membershipNumber" to member.membershipNumber,
            "cardId" to member.cardId,
            "gender" to member.gender,
            "birthdate" to EthiopianDateHelper.formatAsEthiopianDate(member.birthdate),
            "age" to member.getAgeYears(clock),
            "birthdateAccuracy" to member.birthdateAccuracy,
            "medicalRecordNumber" to member.medicalRecordNumber
        )
        val visitInfo = linkedMapOf(
            "visitType" to encounter.visitType,
            "visitReason" to encounter.visitReason,
            "patientOutcome" to encounter.patientOutcome,
            "inboundReferralDate" to encounter.inboundReferralDate?.let { formatAsEthiopianDate(it) },
            "serviceDate" to encounter.occurredAt.let {
                formatAsEthiopianDate(DateUtils.instantToLocalDate(it, clock))
            },
            "preparationDate" to encounter.preparedAt?.let {
                formatAsEthiopianDate(DateUtils.instantToLocalDate(it, clock))
            },
            "referral" to referral?.let { r ->
                linkedMapOf(
                    "id" to r.id,
                    "receivingFacility" to r.receivingFacility,
                    "date" to formatAsEthiopianDate(r.date),
                    "reason" to r.reason
                )
            }
        )

        val encounterItems = encounterWithExtras.encounterItemRelations.map {
            val encounterItem = it.encounterItem
            val billable = it.billableWithPriceSchedule.billable
            val priceSchedule = it.billableWithPriceSchedule.priceSchedule
            if (encounterItem.stockout) {
                linkedMapOf(
                    "quantity" to encounterItem.quantity,
                    "billable" to "${billable.type}: ${billable.name} ${billable.details().orEmpty()}",
                    "stockout" to true
                )
            } else {
                linkedMapOf(
                    "quantity" to encounterItem.quantity,
                    "billable" to "${billable.type}: ${billable.name} ${billable.details().orEmpty()}",
                    "unitPrice" to formatEthiopianMoney(priceSchedule.price)
                )
            }
        }

        return listOf(
            "Claim ID: $claimId",
            "Submission ID $encounterId",
            "Member: ${gson.toJson(memberInfo)}",
            "Visit Information: ${gson.toJson(visitInfo)}",
            "Diagnoses : ${gson.toJson(diagnoses)}",
            "Encounter Items: ${gson.toJson(encounterItems)}",
            "Comment: $providerComment",
            "Total Claimed: $reimbursalAmount"
        )
    }
    fun execute(outStream: OutputStream, user: User): Completable {
        return Completable.fromAction {
            val unsyncedEncounterDeltas = deltaRepository.unsynced(Delta.ModelName.ENCOUNTER).blockingGet()
            val unsyncedEncounterIds = unsyncedEncounterDeltas.map { it.modelId }
            val encountersWithExtras = encounterRepository.findAllWithExtras(unsyncedEncounterIds).blockingGet()

            val rows = mutableListOf<String>()
            val now = EthiopianDateHelper.formatAsEthiopianDate(LocalDate.now(clock))
            rows.add("Here are all the unsynced claims as of $now for ${user.username}")
            rows.add("")
            rows.add("")

            // Loop through each claim.
            encountersWithExtras.forEach { encounterWithExtras ->
                rowsForEncounterWithExtras(encounterWithExtras).forEach { row -> rows.add(row) }
                // Add two empty lines between claims.
                rows.add("")
                rows.add("")
            }

            outStream.use { out ->
                out.write(rows.joinToString("\n").toByteArray(charset = Charsets.UTF_8))
            }
        }.subscribeOn(Schedulers.io())
    }
}
