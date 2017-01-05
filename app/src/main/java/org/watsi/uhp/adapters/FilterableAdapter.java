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
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FilterableAdapter extends BaseAdapter implements Filterable {

    private Dao<Member,Integer> mMemberDao;
    private Filter mSimpleFilter;
    private Activity mActivity;
    private final List<String> mFilteredList = new ArrayList<String>();

    public FilterableAdapter(Activity activity) throws SQLException {
        DatabaseHelper helper = new DatabaseHelper(activity);
        this.mMemberDao = helper.getMemberDao();
        this.mFilteredList.addAll(getAllMemberNames());
        this.mActivity = activity;
    }

    @Override
    public int getCount() {
        return mFilteredList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFilteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = (TextView) view;
        textView.setText((String) getItem(position));
        return textView;
    }

    @Override
    public Filter getFilter() {
        if (mSimpleFilter == null) {
            mSimpleFilter = new SimpleFilter();
        }
        return mSimpleFilter;
    }

    private List<String> getAllMemberNames() throws SQLException {
        List<String> allMemberNames = new ArrayList<String>();
        List<Member> members = mMemberDao.queryForAll();
        for (Member member : members) {
            if (member.getName() != null) {
                mFilteredList.add(member.getName());
            } else {
                Log.d("UHP", "whoops");
            }
        }

        return allMemberNames;
    }

    private class SimpleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<Member> matchingMembers = new ArrayList<Member>();

                try {
                    PreparedQuery<Member> pq = mMemberDao.queryBuilder().where().like(Member.FIELD_NAME_NAME, "%" + constraint + "%").prepare();
                    matchingMembers = mMemberDao.query(pq);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                List<String> tempList = new ArrayList<String>();
                for (Member member : matchingMembers) {
                    tempList.add(member.getName());
                }
                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                List<String> allMemberNames = new ArrayList<String>();
                try {
                    allMemberNames = getAllMemberNames();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                filterResults.count = allMemberNames.size();
                filterResults.values = allMemberNames;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredList.clear();
            mFilteredList.addAll((ArrayList<String>) results.values);
            notifyDataSetChanged();
        }
    }
}
