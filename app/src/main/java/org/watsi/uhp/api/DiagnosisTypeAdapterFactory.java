package org.watsi.uhp.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.watsi.domain.entities.Diagnosis;

public class DiagnosisTypeAdapterFactory extends CustomizedTypeAdapterFactory<Diagnosis> {
    public DiagnosisTypeAdapterFactory() {
        super(Diagnosis.class);
    }

    @Override
    protected void afterRead(JsonElement deserialized) {
        // This basically converts ["blah", "blah2", "blah3"] in jsonarray into
        // a json string that's "["blah", "blah2", "blah3"]" because SQLite does not support arrays.
        JsonObject jsonObject = deserialized.getAsJsonObject();
        String searchAliases = jsonObject.remove(Diagnosis.FIELD_NAME_SEARCH_ALIASES).toString();
        String diagnosisIdsAsJsonString = new Gson().toJson(searchAliases);
        jsonObject.add(Diagnosis.FIELD_NAME_SEARCH_ALIASES, new Gson().fromJson(diagnosisIdsAsJsonString, JsonPrimitive.class));
    }
}
