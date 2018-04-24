package org.watsi.uhp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.watsi.domain.entities.IdentificationEvent;
import org.watsi.domain.entities.Member;
import org.watsi.domain.repositories.IdentificationEventRepository;
import org.watsi.uhp.R;
import org.watsi.uhp.helpers.PhotoLoaderHelper;

import java.util.List;

public class MemberAdapter extends ArrayAdapter<Member> {

    private Boolean showClinicNumber;
    private final IdentificationEventRepository identificationEventRepository;

    public MemberAdapter(Context context,
                         List<Member> memberList,
                         boolean showClinicNumber,
                         IdentificationEventRepository identificationEventRepository) {
        super(context, R.layout.item_member_list, memberList);
        this.showClinicNumber = showClinicNumber;
        this.identificationEventRepository = identificationEventRepository;
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
            viewHolder.age_and_gender = (TextView) convertView.findViewById(R.id
                    .member_age_and_gender);
            viewHolder.card_id = (TextView) convertView.findViewById(R.id.member_card_id);
            viewHolder.phone_number = (TextView) convertView.findViewById(R.id.member_phone_number);
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
            // TODO: clean up formatting
            viewHolder.name.setText(member.getName());
            viewHolder.age_and_gender.setText(member.getGender().toString());
            viewHolder.card_id.setText(member.getCardId());
            viewHolder.phone_number.setText(member.formattedPhoneNumber());
            if (showClinicNumber) {
                viewHolder.phone_number.setVisibility(View.GONE);
                viewHolder.clinic_number.setVisibility(View.VISIBLE);
                IdentificationEvent currentCheckIn = identificationEventRepository
                        .openCheckIn(member.getId());
                if (currentCheckIn != null) {
                    viewHolder.clinic_number.setText(currentCheckIn.getClinicNumber());
                }
            }

            PhotoLoaderHelper.loadMemberPhoto(getContext(), member, viewHolder.photo,
                    R.dimen.item_member_list_photo_width, R.dimen.item_member_list_photo_height);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView age_and_gender;
        TextView card_id;
        TextView phone_number;
        TextView clinic_number;
        ImageView photo;
    }
}
