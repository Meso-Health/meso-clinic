package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;
import org.watsi.uhp.R;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.LineItem;

import java.util.List;

public class ReceiptItemAdapter extends ArrayAdapter<LineItem> {
    public ReceiptItemAdapter(Context context, List<LineItem> lineItemList) {
        super(context, R.layout.item_receipt_list, lineItemList);
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

        LineItem lineItem = getItem(position);

        if (lineItem != null) {
            final Billable billable = lineItem.getBillable();
            final int quantity = lineItem.getQuantity();

            viewHolder.billableQuantity.setText(Integer.toString(quantity));
            viewHolder.billableDetails.setText(billable.getDisplayName());
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
