package org.watsi.domain.entities

import java.text.DecimalFormat
import java.util.UUID

data class Billable(val id: UUID,
                    val type: Type,
                    val composition: String?,
                    val unit: String?,
                    val price: Int,
                    val name: String) {

    @JvmOverloads
    fun formattedPrice(quantity: Int = 1): String {
        return DecimalFormat("#,###").format(price * quantity)
    }

    fun dosageDetails(): String? {
        return if (composition != null) {
            if (unit != null) {
                "$unit $composition"
            } else {
                composition
            }
        } else {
            null
        }
    }

    enum class Type { DRUG, SERVICE, LAB, SUPPLY, VACCINE, UNSPECIFIED }
}
