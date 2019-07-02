package org.watsi.device.db

import android.arch.persistence.room.TypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Member.ArchivedReason
import org.watsi.domain.entities.Referral
import java.util.UUID

class TypeConverter {
    private val formatter = DateTimeFormatter.ISO_DATE
    private val LIST_DELIMITER = ","

    @TypeConverter
    fun fromUuid(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUuid(uuidString: String?): UUID? = if (uuidString == null) null else UUID.fromString(uuidString)

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(long: Long?): Instant? = if (long == null) null else Instant.ofEpochMilli(long)

    @TypeConverter
    fun fromLocalDate(localDate: LocalDate?): String? = localDate?.format(formatter)

    @TypeConverter
    fun toLocalDate(string: String?): LocalDate? = if (string == null) null else LocalDate.parse(string, formatter)

    @TypeConverter
    fun toAction(value: String?): Delta.Action? = value?.let { Delta.Action.valueOf(it) }

    @TypeConverter
    fun fromAction(action: Delta.Action?): String? = action?.toString()

    @TypeConverter
    fun toModelName(value: String?): Delta.ModelName? = value?.let { Delta.ModelName.valueOf(value) }

    @TypeConverter
    fun fromModelName(modelName: Delta.ModelName?): String? = modelName?.toString()

    @TypeConverter
    fun toBillableType(value: String?): Billable.Type? = value?.let { Billable.Type.valueOf(value) }

    @TypeConverter
    fun fromBillableType(type: Billable.Type?): String? = type?.toString()

    @TypeConverter
    fun toClinicNumberType(value: String?): IdentificationEvent.ClinicNumberType? {
        return value?.let { IdentificationEvent.ClinicNumberType.valueOf(it) }
    }

    @TypeConverter
    fun fromClinicNumberType(type: IdentificationEvent.ClinicNumberType?): String? = type?.toString()

    @TypeConverter
    fun toSearchMethod(value: String?): IdentificationEvent.SearchMethod? {
        return value?.let { IdentificationEvent.SearchMethod.valueOf(value) }
    }

    @TypeConverter
    fun fromSearchMethod(searchMethod: IdentificationEvent.SearchMethod?): String? {
        return searchMethod?.toString()
    }

    @TypeConverter
    fun toGender(value: String?): Member.Gender? = value?.let { Member.Gender.valueOf(value) }

    @TypeConverter
    fun fromGender(gender: Member.Gender?): String? = gender?.toString()

    @TypeConverter
    fun toDateAccuracy(value: String?): Member.DateAccuracy? {
        return value?.let { Member.DateAccuracy.valueOf(value) }
    }

    @TypeConverter
    fun fromDateAccuracy(accuracy: Member.DateAccuracy?): String? = accuracy?.toString()

    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.joinToString(LIST_DELIMITER)

    @TypeConverter
    fun toStringList(string: String?): List<String>? {
        return string?.let { if (it.isEmpty()) emptyList() else it.split(LIST_DELIMITER) }
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? = list?.joinToString(LIST_DELIMITER)

    @TypeConverter
    fun toIntList(string: String?): List<Int>? {
        return string?.let {
            if (it.isEmpty()) emptyList() else it.split(LIST_DELIMITER).map { it.toInt() }
        }
    }

    @TypeConverter
    fun toAdjudicationState(value: String?): Encounter.AdjudicationState? {
        return value?.let { Encounter.AdjudicationState.valueOf(value) }
    }

    @TypeConverter
    fun fromAdjudicationState(adjudicationState: Encounter.AdjudicationState?): String? {
        return adjudicationState?.toString()
    }

    @TypeConverter
    fun toArchivedReason(value: String?): ArchivedReason? = value?.let { ArchivedReason.valueOf(value) }

    @TypeConverter
    fun fromArchivedReason(type: ArchivedReason?): String? = type?.let { type.toString() }

    @TypeConverter
    fun toRelationshipToHead(value: String?): Member.RelationshipToHead? = value?.let { Member.RelationshipToHead.valueOf(value) }

    @TypeConverter
    fun fromRelationshipToHead(type: Member.RelationshipToHead?): String? = type?.let { type.toString() }

    @TypeConverter
    fun toReason(value: String?): Referral.Reason? = value?.let { Referral.Reason.valueOf(value) }

    @TypeConverter
    fun fromReason(reason: Referral.Reason?): String? = reason?.let { reason.toString() }

    @TypeConverter
    fun toPatientOutcome(value: String?): Encounter.PatientOutcome? = value?.let { Encounter.PatientOutcome.valueOf(value) }

    @TypeConverter
    fun fromPatientOutcome(patientOutcome: Encounter.PatientOutcome?): String? = patientOutcome?.let { patientOutcome.toString() }

    @TypeConverter
    fun toVisitReason(value: String?): Encounter.VisitReason? = value?.let { Encounter.VisitReason.valueOf(value) }

    @TypeConverter
    fun fromVisitReason(visitReason: Encounter.VisitReason?): String? = visitReason?.let { visitReason.toString() }
}
