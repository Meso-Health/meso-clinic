package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Billable billable = getItem(position);
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
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView billableName;
        Button removeBillableBtn;
    }
}
