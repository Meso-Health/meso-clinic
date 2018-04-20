package org.watsi.uhp.view_models;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.watsi.domain.entities.Encounter;
import org.watsi.uhp.R;

public class EncounterViewModel extends BaseObservable {
    private final Encounter mEncounter;
    private final Context mContext;

    public EncounterViewModel(Encounter encounter, Context context) {
        this.mEncounter = encounter;
        this.mContext = context;
    }

    @Bindable
    public String getPriceTotal() {
        String formattedPrice = Encounter.PRICE_FORMAT.format(mEncounter.price());
        return mContext.getString(R.string.price_with_currency, formattedPrice);
    }

    @Bindable
    public String getItemsCountLabel() {
        int lineItemsCount = mEncounter.getEncounterItems().size();
        return mContext.getResources()
                .getQuantityString(R.plurals.receipt_line_item_count, lineItemsCount, lineItemsCount);
    }

    @Bindable
    public String getFormsAttachedLabel() {
        int numFormsAttached = mEncounter.getEncounterForms().size();
        return mContext.getResources()
                .getQuantityString(R.plurals.forms_attached_label, numFormsAttached, numFormsAttached);
    }
}
