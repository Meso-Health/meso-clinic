package org.watsi.domain.entities

import java.io.Serializable
import java.util.UUID

data class Billable(val id: UUID,
                    val type: Type,
                    val composition: String?,
                    val unit: String?,
                    val name: String,
                    val active: Boolean,
                    val requiresLabResult: Boolean) : Serializable {

    fun details(): String? {
        return if (unit != null && composition != null) {
            "$unit $composition"
        } else if (unit != null) {
            unit
        } else if (composition != null) {
            composition
        } else {
            null
        }
    }

    companion object {
        fun requiresQuantity(type: Billable.Type): Boolean {
            return type == Type.DRUG || type == Type.SUPPLY || type == Type.VACCINE
        }

        private val BILLABLE_TYPES_THAT_ALLOW_PRICE_EDITS = listOf(
            Type.DRUG,
            Type.SUPPLY,
            Type.LAB
        )

        const val VACCINE_COMPOSITION = "vial"
        const val SUPPLY_COMPOSITION = "unit"

        fun canEditPrice(type: Billable.Type): Boolean {
            return BILLABLE_TYPES_THAT_ALLOW_PRICE_EDITS.contains(type)
        }
    }

    enum class Type { DRUG, SERVICE, LAB, SUPPLY, VACCINE, IMAGING, PROCEDURE, BED_DAY, UNSPECIFIED }
}
