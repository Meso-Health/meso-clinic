package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.models.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FilterableAdapter extends BaseAdapter implements Filterable {

    private Dao<User,Integer> mUserDao;
    private Filter simpleFilter;
    private Activity activity;
    private final List<String> filteredList = new ArrayList<String>();

    public FilterableAdapter(Activity activity) throws SQLException {
        DatabaseHelper helper = new DatabaseHelper(activity);
        this.mUserDao = helper.getUserDao();
        filteredList.addAll(getAllUserNames());
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

    private List<String> getAllUserNames() throws SQLException {
        List<String> allUserNames = new ArrayList<String>();
        List<User> users = mUserDao.queryForAll();
        for (User user : users) {
            if (user.getName() != null) {
                filteredList.add(user.getName());
            } else {
                Log.d("UHP", "whoops");
            }
        }

        return allUserNames;
    }

    private class SimpleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<User> matchingUsers = new ArrayList<User>();

                try {
                    PreparedQuery<User> pq = mUserDao.queryBuilder().where().like(User.FIELD_NAME_NAME, "%" + constraint + "%").prepare();
                    matchingUsers = mUserDao.query(pq);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                List<String> tempList = new ArrayList<String>();
                for (User user : matchingUsers) {
                    tempList.add(user.getName());
                }
                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                List<String> allUserNames = new ArrayList<String>();
                try {
                    allUserNames = getAllUserNames();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                filterResults.count = allUserNames.size();
                filterResults.values = allUserNames;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList.clear();
            filteredList.addAll((ArrayList<String>) results.values);
            notifyDataSetChanged();
        }
    }
}
