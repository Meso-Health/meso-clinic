package org.watsi.uhp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProviderAssignment {

    @Expose
    @SerializedName("provider_id")
    private int mProviderId;

    @Expose
    @SerializedName("start_reason")
    private String mStartReason;

    public ProviderAssignment(int providerId, String startReason) {
        this.mProviderId = providerId;
        this.mStartReason = startReason;
    }

    public int getProviderId() {
        return mProviderId;
    }

    public void setProviderId(int providerId) {
        this.mProviderId = providerId;
    }

    public String getStartReason() {
        return mStartReason;
    }

    public void setStartReason(String startReason) {
        this.mStartReason = startReason;
    }
}
