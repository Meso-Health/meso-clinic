
package org.watsi.domain.entities

import java.io.Serializable

data class CbhiId(val regionNumber: Int?,
                  val woredaNumber: Int?,
                  val kebeleNumber: Int?,
                  val paying: Paying?,
                  val householdNumber: Int?,
                  val householdMemberNumber: Int?): Serializable {
    // TODO: better name
    enum class Paying { P, I }
    fun valid(): Boolean {
        if (regionNumber == null || regionNumber < 1 || regionNumber > 99) return false
        if (woredaNumber == null || woredaNumber < 1 || woredaNumber > 99) return false
        if (kebeleNumber == null || kebeleNumber < 1 || kebeleNumber > 99) return false
        if (paying == null) return false
        if (householdNumber == null || householdNumber < 1 || householdNumber > 999999) return false
        if (householdMemberNumber == null || householdMemberNumber < 0 || householdMemberNumber > 99) return false
        return true
    }
    fun formatted(): String? {
        if (!valid()) return null
        val regionString = regionNumber.toString().padStart(2, '0')
        val woredaString = woredaNumber.toString().padStart(2, '0')
        val kebeleString = kebeleNumber.toString().padStart(2, '0')
        val householdNumber = householdNumber.toString().padStart(6, '0')
        val householdMemberNumber = householdMemberNumber.toString().padStart(2, '0')
        return "$regionString/$woredaString/$kebeleString/$paying-$householdNumber/$householdMemberNumber"
    }
}
