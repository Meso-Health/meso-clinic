package org.watsi.uhp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FilterableAdapter extends BaseAdapter implements Filterable {

    private List<String> list;
    private List<String> filteredList;
    private Filter simpleFilter;
    private Activity activity;

    public FilterableAdapter(Activity activity) {
        list = new ArrayList<String>();
        list.add("Foo");
        list.add("Bar");
        list.add("Baz");

        filteredList = new ArrayList<String>();
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = (TextView) view;
        textView.setText((String) getItem(position));
        return textView;
    }

    @Override
    public Filter getFilter() {
        if (simpleFilter == null) {
            simpleFilter = new SimpleFilter();
        }
        return simpleFilter;
    }

    private class SimpleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<String> tempList = new ArrayList<String>();

                for (String item : list) {
                    if (item.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(item);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = list.size();
                filterResults.values = list;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }
    }
}
