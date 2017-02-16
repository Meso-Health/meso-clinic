package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;
import org.watsi.uhp.R;
import org.watsi.uhp.models.Billable;

import java.util.List;

public class ReceiptItemAdapter implements ListAdapter {
    private List<Billable> mItemList;
    private Context mContext;

    public ReceiptItemAdapter(Context context) {
        mContext = context;
        // TODO: specify what item list is from encounter fragment:
        // mItemList = ;
    }

    @Override
    public Object getItem(int position) { return mItemList.get(position); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) mContext).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.item_receipt_list, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.billableQuantity = (TextView) convertView.findViewById(R.id.receipt_billable_quantity);
            viewHolder.billableName = (TextView) convertView.findViewById(R.id.receipt_billable_name);
            viewHolder.billableDetails = (TextView) convertView.findViewById(R.id.receipt_billable_details);
            viewHolder.billablePrice = (TextView) convertView.findViewById(R.id.receipt_billable_price);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Billable billable = (Billable) getItem(position);
        if (billable != null) {
            // TODO: viewHolder.billableQuantity.setText();
            viewHolder.billableName.setText(billable.getName());
            viewHolder.billableDetails.setText(billable.getDisplayName());
            viewHolder.billablePrice.setText(String.valueOf(billable.getPrice()));
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView billableQuantity;
        TextView billableName;
        TextView billableDetails;
        TextView billablePrice;
    }
}

