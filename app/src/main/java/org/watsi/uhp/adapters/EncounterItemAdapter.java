package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.LineItem;

import java.util.List;

public class EncounterItemAdapter extends ArrayAdapter<LineItem> {

    public EncounterItemAdapter(Context context, List<LineItem> lineItemList) {
        super(context, R.layout.item_line_item_list, lineItemList);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) getContext()).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.item_line_item_list, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.billableName = (TextView) convertView.findViewById(R.id.billable_name);
            viewHolder.billableDetails = (TextView) convertView.findViewById(R.id.billable_details);
            viewHolder.removeLineItemBtn = (Button) convertView.findViewById(R.id.remove_line_item_btn);
            viewHolder.billableQuantity = (EditText) convertView.findViewById(R.id.billable_quantity);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final LineItem lineItem = getItem(position);

        if (lineItem != null) {
            final Billable billable = lineItem.getBillable();

            viewHolder.billableName.setText(billable.getName());
            viewHolder.billableDetails.setText(billable.dosageDetails());
            viewHolder.removeLineItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(lineItem);
                }
            });

            if (billable.getCategory().equals(Billable.CategoryEnum.SERVICES) ||
                    billable.getCategory().equals(Billable.CategoryEnum.LABS)) {
                viewHolder.billableQuantity.setVisibility(View.GONE);
            } else {
                viewHolder.billableQuantity.setVisibility(View.VISIBLE);

                final ViewHolder vh = viewHolder;
                viewHolder.billableQuantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            String quantity = vh.billableQuantity.getText().toString();

                            if (quantity.equals("")) {
                                vh.billableQuantity.setText(String.valueOf(lineItem.getQuantity()));
                            } else {
                                lineItem.setQuantity(Integer.valueOf(quantity));
                            }
                        }
                    }
                });
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        Button removeLineItemBtn;
        TextView billableName;
        TextView billableDetails;
        EditText billableQuantity;
    }
}
