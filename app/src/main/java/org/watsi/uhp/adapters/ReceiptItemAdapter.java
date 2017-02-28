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

import java.text.DecimalFormat;
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
            viewHolder.billableDetails = (TextView) convertView.findViewById(R.id.receipt_billable_details);
            viewHolder.billablePriceAndQuantity = (TextView) convertView.findViewById(R.id.receipt_billable_price_and_quantity);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        EncounterItem encounterItem = getItem(position);

        if (encounterItem != null) {
            final Billable billable = encounterItem.getBillable();

            viewHolder.billableDetails.setText(billable.toString());

            if (billable.getType() == Billable.TypeEnum.SERVICE || billable.getType() == Billable.TypeEnum.LAB) {
                viewHolder.billablePriceAndQuantity.setText(priceDecorator(billable.getPrice()) + " UGX");
            } else {
                viewHolder.billablePriceAndQuantity.setText(String.valueOf(encounterItem.getQuantity()) + "  x  " + priceDecorator(billable.getPrice()) + " UGX");
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView billableDetails;
        TextView billablePriceAndQuantity;
    }

    private String priceDecorator(int price) {
        DecimalFormat df = new DecimalFormat("#,###,###");
        String formattedPrice = df.format(price);

        return formattedPrice;
    }
}
