package org.watsi.domain.factories

import org.watsi.domain.entities.Billable
import java.util.UUID

object BillableFactory {

    fun build(
            id: UUID = UUID.randomUUID(),
            type: Billable.Type = Billable.Type.SERVICE,
            composition: String? = null,
            unit: String? = null,
            name: String = "Delivery") : Billable {
        return Billable(id = id,
                        type = type,
                        composition = composition,
                        unit = unit,
                        name = name)
    }
}
