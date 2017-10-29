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
            // TODO This is shared code for encounter item adapter.
            viewHolder.billableQuantity.setText(String.valueOf(encounterItem.getQuantity()));
            viewHolder.billableName.setText(billable.getName());
            if (billable.requiresLabResult()) {
                viewHolder.billableDetails.setVisibility(View.VISIBLE);
                viewHolder.billableDetails.setText(encounterItem.getLabResult().getResult().toString());
            } else if (billable.dosageDetails() == null) {
                viewHolder.billableDetails.setVisibility(View.GONE);
            } else {
                viewHolder.billableDetails.setText(billable.dosageDetails());
                viewHolder.billableDetails.setVisibility(View.VISIBLE);
            }

            if (billable.getType() == Billable.TypeEnum.SERVICE || billable.getType() == Billable.TypeEnum.LAB) {
                viewHolder.billablePriceOfQuantity.setText(Billable.priceDecorator(billable.getPrice()));
            } else {
                viewHolder.billablePriceOfQuantity.setText(Billable.priceDecorator(encounterItem.getQuantity() * billable.getPrice()));
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView billableQuantity;
        TextView billableName;
        TextView billableDetails;
        TextView billablePriceOfQuantity;
    }
}
