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
            viewHolder.billableDetails = (TextView) convertView.findViewById(R.id.receipt_billable_details);
            viewHolder.billablePrice = (TextView) convertView.findViewById(R.id.receipt_billable_price);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        EncounterItem encounterItem = getItem(position);

        if (encounterItem != null) {
            final Billable billable = encounterItem.getBillable();
            final int quantity = encounterItem.getQuantity();

            viewHolder.billableQuantity.setText(String.valueOf(quantity));
            viewHolder.billableDetails.setText(billable.toString());
            viewHolder.billablePrice.setText(String.valueOf(billable.getPrice()) + " UGX");
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView billableQuantity;
        TextView billableDetails;
        TextView billablePrice;
    }
}
