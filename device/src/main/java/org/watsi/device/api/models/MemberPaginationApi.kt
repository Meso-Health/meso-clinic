package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName

data class MemberPaginationApi(
    @SerializedName("page_key") val pageKey: String,
    @SerializedName("has_more") val hasMore: Boolean,
    @SerializedName("members") val members: List<MemberApi>
)
