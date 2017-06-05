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
import org.watsi.uhp.models.EncounterItem;

import java.util.List;

public class EncounterItemAdapter extends ArrayAdapter<EncounterItem> {

    public EncounterItemAdapter(Context context, List<EncounterItem> encounterItemList) {
        super(context, R.layout.item_encounter_item_list, encounterItemList);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) getContext()).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.item_encounter_item_list, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.billableName = (TextView) convertView.findViewById(R.id.billable_name);
            viewHolder.billableDetails = (TextView) convertView.findViewById(R.id.billable_details);
            viewHolder.removeLineItemBtn = (Button) convertView.findViewById(R.id.remove_line_item_btn);
            viewHolder.billableQuantity = (EditText) convertView.findViewById(R.id.billable_quantity);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final EncounterItem encounterItem = getItem(position);

        if (encounterItem != null) {
            final Billable billable = encounterItem.getBillable();

            viewHolder.billableName.setText(billable.getName());
            if (billable.dosageDetails() == null) {
                viewHolder.billableDetails.setVisibility(View.GONE);
            } else {
                viewHolder.billableDetails.setText(billable.dosageDetails());
                viewHolder.billableDetails.setVisibility(View.VISIBLE);
            }
            viewHolder.removeLineItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(encounterItem);
                }
            });

            final ViewHolder vh = viewHolder;
            viewHolder.billableQuantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        String quantity = vh.billableQuantity.getText().toString();

                        if (quantity.equals("") || quantity.equals("0")) {
                            vh.billableQuantity.setText(String.valueOf(encounterItem.getQuantity()));
                        } else {
                            encounterItem.setQuantity(Integer.valueOf(quantity));
                        }
                    }
                }
            });
            viewHolder.billableQuantity.setText(String.valueOf(encounterItem.getQuantity()));

            if (billable.getType().equals(Billable.TypeEnum.SERVICE) ||
                    billable.getType().equals(Billable.TypeEnum.LAB)) {

                viewHolder.billableQuantity.setEnabled(false);

            } else {
                viewHolder.billableQuantity.setEnabled(true);
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
