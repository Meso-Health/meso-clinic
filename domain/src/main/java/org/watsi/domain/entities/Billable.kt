package org.watsi.domain.entities

import java.io.Serializable
import java.util.UUID

data class Billable(val id: UUID,
                    val type: Type,
                    val composition: String?,
                    val unit: String?,
                    val price: Int,
                    val name: String) : Serializable {

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

        const val VACCINE_COMPOSITION = "vail"
        const val SUPPLY_COMPOSITION = "unit"
    }

    enum class Type { DRUG, SERVICE, LAB, SUPPLY, VACCINE }
}
