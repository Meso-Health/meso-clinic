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

import java.util.List;

public class BillableAdapter extends ArrayAdapter<Billable> {

    private Button mCreateEncounterButton;

    public BillableAdapter(Context context, List<Billable> billableList, Button createEncounterButton) {
        super(context, R.layout.item_billable_list, billableList);
        this.mCreateEncounterButton = createEncounterButton;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) getContext()).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.item_billable_list, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.billableName = (TextView) convertView.findViewById(R.id.billable_name);
            viewHolder.removeBillableBtn = (Button) convertView.findViewById(R.id.remove_billable_btn);
            viewHolder.billableQuantity = (EditText) convertView.findViewById(R.id.billable_quantity);
            viewHolder.decQuantityBtn = (Button) convertView.findViewById(R.id.dec_billable_quantity);
            viewHolder.incQuantityBtn = (Button) convertView.findViewById(R.id.inc_billable_quantity);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Billable billable = getItem(position);

        if (billable.getCategory().equals(Billable.CategoryEnum.SERVICES) ||
                billable.getCategory().equals(Billable.CategoryEnum.LABS)) {
            viewHolder.incQuantityBtn.setVisibility(View.GONE);
            viewHolder.billableQuantity.setVisibility(View.GONE);
            viewHolder.decQuantityBtn.setVisibility(View.GONE);
        }

        if (billable != null) {
            viewHolder.billableName.setText(billable.getName());
            viewHolder.removeBillableBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(billable);
                    if (isEmpty()) {
                        mCreateEncounterButton.setVisibility(View.GONE);
                    }
                }
            });

            final ViewHolder vh = viewHolder;

            viewHolder.decQuantityBtn.setOnClickListener(new View.OnClickListener() {
                protected String decreaseQuantity(ViewHolder vh) {
                    String value = vh.billableQuantity.getText().toString();

                    if (value.equals("1")) {
                        return "1";
                    }
                    else {
                        int int_value = Integer.parseInt(value);
                        int new_int_value = int_value - 1;
                        String new_value = Integer.toString(new_int_value);
                        return new_value;
                    }
                }

                public void onClick(View v) {
                    vh.billableQuantity.setText(decreaseQuantity(vh));
                }
            });

            viewHolder.incQuantityBtn.setOnClickListener(new View.OnClickListener() {
                protected String increaseQuantity(ViewHolder vh) {
                    String value = vh.billableQuantity.getText().toString();
                    int int_value = Integer.parseInt(value);
                    int new_int_value = int_value + 1;
                    String new_value = Integer.toString(new_int_value);
                    return new_value;
                }

                public void onClick(View v) {
                    vh.billableQuantity.setText(increaseQuantity(vh));
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView billableName;
        Button removeBillableBtn;
        EditText billableQuantity;
        Button decQuantityBtn;
        Button incQuantityBtn;
    }
}
