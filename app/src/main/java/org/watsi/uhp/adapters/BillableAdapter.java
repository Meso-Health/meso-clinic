package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.watsi.uhp.models.Billable;

import java.util.List;

public class BillableAdapter extends ArrayAdapter<Billable> {

    public BillableAdapter(Context context, int resource, List<Billable> billableList) {
        super(context, resource, billableList);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) getContext()).getLayoutInflater();
            convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.titleView = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.subTitleView = (TextView) convertView.findViewById(android.R.id.text2);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Billable billable = getItem(position);
        if (billable != null) {
            viewHolder.titleView.setText(billable.getName());
            if (billable.getUnit() != null) {
                viewHolder.subTitleView.setText(billable.getUnit() + " " + billable.getAmount());
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView titleView;
        TextView subTitleView;
    }
}
