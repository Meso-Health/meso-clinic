package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class HouseholdApi(
    @SerializedName("id") val householdId: UUID,
    @SerializedName("members") val members: List<MemberApi>
)
