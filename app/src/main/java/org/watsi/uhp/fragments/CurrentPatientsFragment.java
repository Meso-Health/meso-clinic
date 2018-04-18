package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.listeners.SetBarcodeFragmentListener;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.repositories.IdentificationEventRepository;
import org.watsi.uhp.repositories.MemberRepository;

import java.util.List;

import javax.inject.Inject;

public class CurrentPatientsFragment extends BaseFragment {

    @Inject MemberRepository memberRepository;
    @Inject IdentificationEventRepository identificationEventRepository;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.current_patients_fragment_label);

        View view = inflater.inflate(R.layout.fragment_current_patients, container, false);
        TextView currentPatientsLabel = view.findViewById(R.id.current_patients_label);
        ListView listView = view.findViewById(R.id.current_patients);
        listView.setEmptyView(view.findViewById(R.id.current_patients_empty_text));

        view.findViewById(R.id.identification_button).setOnClickListener(
                new SetBarcodeFragmentListener(getNavigationManager(),
                        BarcodeFragment.ScanPurposeEnum.ID, null, null));

        List<Member> currentPatients = memberRepository.checkedInMembers();

        if (currentPatients.isEmpty()) {
            currentPatientsLabel.setVisibility(View.GONE);
        } else {
            currentPatientsLabel.setText(getActivity().getResources().getQuantityString(
                    R.plurals.current_patients_label, currentPatients.size(), currentPatients.size()));

            listView.setAdapter(new MemberAdapter(getContext(),
                                                  currentPatients,
                                                  true,
                                                  identificationEventRepository));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Member member = (Member) parent.getItemAtPosition(position);
                    getNavigationManager().setMemberDetailFragment(member);
                }
            });
        }

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_logout).setVisible(true);
        menu.findItem(R.id.menu_version).setVisible(true);
    }
}
