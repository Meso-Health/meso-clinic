package org.watsi.device.api.models

import org.watsi.domain.entities.Billable
import java.util.UUID

data class BillableApi(
    val id: UUID,
    val type: String,
    val composition: String?,
    val unit: String?,
    val price: Int,
    val name: String
) {

    constructor (billable: Billable) : this(
        id = billable.id,
        type = billable.type.toString().toLowerCase(),
        composition = billable.composition?.toLowerCase(),
        unit = billable.unit,
        price = billable.price,
        name = billable.name
    )

    fun toBillable(): Billable {
        return Billable(
            id = id,
            type = Billable.Type.valueOf(type.toUpperCase()),
            composition = composition,
            unit = unit,
            price = price,
            name = name
        )
    }
}
