package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Member;

import java.util.List;

public class MemberAdapter extends ArrayAdapter<Member> {

    private Boolean showClinicNumber;

    public MemberAdapter(Context context, List<Member> memberList, boolean showClinicNumber) {
        super(context, R.layout.item_member_list, memberList);
        this.showClinicNumber = showClinicNumber;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) getContext()).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.item_member_list, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.member_name);
            viewHolder.age = (TextView) convertView.findViewById(R.id.member_age);
            viewHolder.gender = (TextView) convertView.findViewById(R.id.member_gender);
            viewHolder.card_id = (TextView) convertView.findViewById(R.id.member_card_id);
            viewHolder.photo = (ImageView) convertView.findViewById(R.id.member_photo);
            if (showClinicNumber) {
                viewHolder.clinic_number = (TextView) convertView.findViewById(R.id
                        .member_clinic_number);
            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Member member = getItem(position);

        if (member != null) {
            viewHolder.name.setText(member.getFullName());
            viewHolder.age.setText(member.getFormattedAge());
            viewHolder.gender.setText(member.getFormattedGender());
            viewHolder.card_id.setText(String.valueOf(member.getFormattedCardId()));
            if (showClinicNumber) {
                viewHolder.clinic_number.setVisibility(View.VISIBLE);
                viewHolder.clinic_number.setText(member.getLastIdentification().getFormattedClinicNumber());
            }

            Bitmap photoBitmap = member.getPhotoBitmap();
            if (photoBitmap != null) {
                viewHolder.photo.setImageBitmap(photoBitmap);
            } else {
                viewHolder.photo.setImageResource(R.drawable.portrait_placeholder);
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView age;
        TextView gender;
        TextView card_id;
        TextView clinic_number;
        ImageView photo;
    }
}
