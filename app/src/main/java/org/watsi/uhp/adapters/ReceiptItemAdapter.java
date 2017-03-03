package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.EncounterItem;

import java.util.List;

public class ReceiptItemAdapter extends ArrayAdapter<EncounterItem> {
    public ReceiptItemAdapter(Context context, List<EncounterItem> encounterItemList) {
        super(context, R.layout.item_receipt_list, encounterItemList);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) getContext()).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.item_receipt_list, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.billableQuantity = (TextView) convertView.findViewById(R.id.receipt_billable_quantity);
            viewHolder.billableName = (TextView) convertView.findViewById(R.id.receipt_billable_name);
            viewHolder.billableDetails = (TextView) convertView.findViewById(R.id.receipt_billable_details);
            viewHolder.billablePriceOfQuantity = (TextView) convertView.findViewById(R.id.receipt_billable_price_of_quantity);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        EncounterItem encounterItem = getItem(position);

        if (encounterItem != null) {
            final Billable billable = encounterItem.getBillable();

            viewHolder.billableQuantity.setText(String.valueOf(encounterItemQuantity(encounterItem)));
            viewHolder.billableName.setText(billable.getName());
            viewHolder.billableDetails.setText(billable.dosageDetails());

            if (billable.getType() == Billable.TypeEnum.SERVICE || billable.getType() == Billable.TypeEnum.LAB) {
                viewHolder.billablePriceOfQuantity.setText(Billable.priceDecorator(billable.getPrice()) + " UGX");
            } else {
                viewHolder.billablePriceOfQuantity.setText(Billable.priceDecorator(encounterItemQuantity(encounterItem) * billable.getPrice()) + " UGX");
            }
        }
        return convertView;
    }

    private int encounterItemQuantity(EncounterItem encounterItem) {
        if (String.valueOf(encounterItem.getQuantity()) == null) {
            return 1;
        } else {
            return encounterItem.getQuantity();
        }
    }

    private static class ViewHolder {
        TextView billableQuantity;
        TextView billableName;
        TextView billableDetails;
        TextView billablePriceOfQuantity;
    }
}
