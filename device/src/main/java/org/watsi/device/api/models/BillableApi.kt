package org.watsi.device.api.models

import org.watsi.domain.entities.Billable
import java.util.UUID

data class BillableApi(
    val id: UUID,
    val type: String,
    val composition: String?,
    val unit: String?,
    val name: String,
    val active: Boolean
) {

    constructor (billable: Billable) : this(
        id = billable.id,
        type = billable.type.toString().toLowerCase(),
        composition = billable.composition?.toLowerCase(),
        unit = billable.unit,
        name = billable.name,
        active = billable.active
    )

    fun toBillable(): Billable {
        return Billable(
            id = id,
            type = Billable.Type.valueOf(type.toUpperCase()),
            composition = composition,
            unit = unit,
            name = name,
            active = active
            // Billables inside returned claims may or may not be active.
            // If we keep this true, de-activated billables would be upserted to have active = true.
            // If we keep this false, any billable that are part of returned claims would set billables to active = false.
            // Both these scenarios don't work, so we will need to look at what backend returns.
        )
    }
}
