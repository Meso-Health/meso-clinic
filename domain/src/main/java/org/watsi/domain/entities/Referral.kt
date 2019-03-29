package org.watsi.domain.entities

import org.threeten.bp.LocalDate
import java.io.Serializable
import java.util.UUID

data class Referral(
    val id: UUID,
    val receivingFacility: String,
    val reason: Reason,
    val encounterId: UUID,
    val number: String?,
    val date: LocalDate
): Serializable {

    enum class Reason {
        FURTHER_CONSULTATION,
        DRUG_STOCKOUT,
        INVESTIGATIVE_TESTS,
        INPATIENT_CARE,
        BED_SHORTAGE,
        FOLLOW_UP,
        OTHER
    }

    companion object {
        val RECEIVING_FACILITY_CHOICES = listOf(
            "Mehoni Hospital",
            "Adi Shihu Hospital",
            "Adi Gudem Hospital",
            "Samre Hospital",
            "Hagere Selam Hospital",
            "Mulu Asfeha (Atsibi) Hospital",
            "Fire Semaetat (Hawzen) Hospital",
            "Frewoini Hospital",
            "Fatsi Hospital",
            "Dewhan Hospital",
            "Enticho Hospital",
            "Rama Hospital",
            "Edaga Arbi Hospital",
            "Yechila Hospital",
            "Semema Hospital",
            "Wukro Marai Hospital",
            "Selekleka Hospital",
            "Endabaguna Hospital",
            "Mai Tsebri Hospital",
            "Adi Daero",
            "Adi Remets",
            "Ketema Nigus (Tsegede)",
            "Alamata Hospital",
            "Korem Hospital",
            "Lemlem Karl (Maichew) Hospital",
            "Quiha Hospital",
            "Mekelle Hospital",
            "Wukro Hospital",
            "Abi Adi Hospital",
            "Adigrat Hospital",
            "Adwa Hospital",
            "Kidist Mariam (Aksum) Hospital",
            "Suhul (Shire) Hospital",
            "Mayani (Sheraro) Hospital",
            "Mearig (Dansha) Hospital",
            "Kahsay Abera (Humera) Hospital",
            "Ayder Hospital",
            "Aksum Hospital"
        ).sorted()
    }
}
