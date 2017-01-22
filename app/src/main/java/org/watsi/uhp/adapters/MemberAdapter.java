package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;

public class MemberAdapter implements ListAdapter {

    private List<Member> mMemberList;
    private Context mContext;

    public MemberAdapter(Context context) {
        mContext = context;
        try {
            mMemberList = MemberDao.recentMembers();
        } catch (SQLException e) {
            // TODO: how to handle this case?
            Rollbar.reportException(e);
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // no-op
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // no-op
    }

    @Override
    public int getCount() {
        return mMemberList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMemberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ((Member) getItem(position)).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) mContext).getLayoutInflater();
            convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.titleView = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.subTitleView = (TextView) convertView.findViewById(android.R.id.text2);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Member member = (Member) getItem(position);
        if (member != null) {
            viewHolder.titleView.setText(member.getName());
            viewHolder.subTitleView.setText(String.valueOf(member.getId()));
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return mMemberList.size() == 0;
    }

    private static class ViewHolder {
        TextView titleView;
        TextView subTitleView;
    }
}
