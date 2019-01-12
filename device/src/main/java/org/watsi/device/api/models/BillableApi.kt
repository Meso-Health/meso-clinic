package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.Billable
import java.util.UUID

data class BillableApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("type") val type: String,
        @SerializedName("composition") val composition: String?,
        @SerializedName("unit") val unit: String?,
        @SerializedName("price") val price: Int,
        @SerializedName("name") val name: String
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
