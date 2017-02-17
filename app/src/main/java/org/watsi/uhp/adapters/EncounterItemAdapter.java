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

    private Button mCreateEncounterButton;

    public EncounterItemAdapter(Context context, List<LineItem> lineItemList, Button
            createEncounterButton) {
        super(context, R.layout.item_line_item_list, lineItemList);
        this.mCreateEncounterButton = createEncounterButton;
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
            viewHolder.billableDosage = (TextView) convertView.findViewById(R.id.billable_dosage);
            viewHolder.billableUnit = (TextView) convertView.findViewById(R.id.billable_unit);
            viewHolder.removeLineItemBtn = (Button) convertView.findViewById(R.id.remove_line_item_btn);
            viewHolder.billableQuantity = (EditText) convertView.findViewById(R.id.billable_quantity);
            viewHolder.decQuantityBtn = (Button) convertView.findViewById(R.id.dec_billable_quantity);
            viewHolder.incQuantityBtn = (Button) convertView.findViewById(R.id.inc_billable_quantity);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final LineItem lineItem = getItem(position);

        if (lineItem != null) {
            final Billable billable = lineItem.getBillable();

            viewHolder.billableName.setText(billable.getName());
            viewHolder.billableDosage.setText(billable.getAmount());
            viewHolder.billableUnit.setText(billable.getUnit());
            viewHolder.removeLineItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(lineItem);
                    if (isEmpty()) {
                        mCreateEncounterButton.setVisibility(View.GONE);
                    }
                }
            });

            if (billable.getCategory().equals(Billable.CategoryEnum.SERVICES) ||
                    billable.getCategory().equals(Billable.CategoryEnum.LABS)) {
                viewHolder.incQuantityBtn.setVisibility(View.GONE);
                viewHolder.billableQuantity.setVisibility(View.GONE);
                viewHolder.decQuantityBtn.setVisibility(View.GONE);
            } else {
                viewHolder.incQuantityBtn.setVisibility(View.VISIBLE);
                viewHolder.billableQuantity.setVisibility(View.VISIBLE);
                viewHolder.decQuantityBtn.setVisibility(View.VISIBLE);

                final ViewHolder vh = viewHolder;
                viewHolder.decQuantityBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (vh.billableQuantity.getText().toString().equals("")) {
                            lineItem.setQuantity(1);
                        } else {
                            lineItem.decreaseQuantity();
                        }
                        vh.billableQuantity.setText(Integer.toString(lineItem.getQuantity()));
                    }
                });

                viewHolder.incQuantityBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (vh.billableQuantity.getText().toString().equals("")) {
                            lineItem.setQuantity(1);
                        } else {
                            lineItem.increaseQuantity();
                        }
                        vh.billableQuantity.setText(Integer.toString(lineItem.getQuantity()));
                    }
                });
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        Button removeLineItemBtn;
        TextView billableName;
        TextView billableUnit;
        TextView billableDosage;
        EditText billableQuantity;
        Button decQuantityBtn;
        Button incQuantityBtn;
    }
}
