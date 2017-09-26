package org.watsi.uhp.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

import java.util.Iterator;

/**
 * Factory for custom serialization of Encounter objects
 */
public class EncounterTypeAdapterFactory extends CustomizedTypeAdapterFactory<Encounter> {

    public EncounterTypeAdapterFactory() {
        super(Encounter.class);
    }

    /**
     * API expects difference structures for Billables in EncounterItems depending on whether
     * it is a new Billable. For new billables, the entire JSON object is sent and for existing
     * billables only the billable_id is specified.
     *
     * @param source
     * @param toSerialize
     */
    @Override
    protected void beforeWrite(Encounter source, JsonElement toSerialize) {
        JsonObject jsonObject = toSerialize.getAsJsonObject();
        JsonArray rawEncounterItemsJson = jsonObject.remove(Encounter.FIELD_NAME_ENCOUNTER_ITEMS)
                .getAsJsonArray();
        JsonArray convertedEncounterItemsJson = new JsonArray();
        Iterator<JsonElement> iterator = rawEncounterItemsJson.iterator();
        while (iterator.hasNext()) {
            JsonObject encounterItemJson = iterator.next().getAsJsonObject();
            JsonObject billableJson = encounterItemJson.remove(EncounterItem.FIELD_NAME_BILLABLE_ID)
                    .getAsJsonObject();
            if (billableJson.get(Billable.FIELD_NAME_ID).isJsonNull()) {
                encounterItemJson.add("billable", billableJson);
            } else {
                String billableId = billableJson.get(Billable.FIELD_NAME_ID).getAsString();
                encounterItemJson.addProperty(EncounterItem.FIELD_NAME_BILLABLE_ID, billableId);
            }
            convertedEncounterItemsJson.add(encounterItemJson);
        }
        jsonObject.add(Encounter.FIELD_NAME_ENCOUNTER_ITEMS, convertedEncounterItemsJson);
    }
}
