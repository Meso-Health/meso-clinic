package org.watsi.uhp.adapters;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import org.watsi.uhp.models.Diagnosis;
import org.watsi.uhp.repositories.DiagnosisRepository;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisAdapter extends ArrayAdapter<Diagnosis> {
    public final Integer MIN_LENGTH_BEFORE_DISPLAY_SEARCH_RESULTS = 2;
    Filter mFilter;

    public DiagnosisAdapter(@NonNull Context context,
                            @LayoutRes int resource,
                            @IdRes int textViewResourceId,
                            DiagnosisRepository diagnosisRepository) {
        super(context, resource, textViewResourceId);
        this.mFilter = new DiagnosisListFilter(diagnosisRepository);
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public class DiagnosisListFilter extends Filter {

        private final DiagnosisRepository diagnosisRepository;

        DiagnosisListFilter(DiagnosisRepository diagnosisRepository) {
            this.diagnosisRepository = diagnosisRepository;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Diagnosis> ds = new ArrayList<>();
            if (constraint != null && constraint.length() >= MIN_LENGTH_BEFORE_DISPLAY_SEARCH_RESULTS) {
                diagnosisRepository.fuzzySearchByName(constraint.toString());
            }
            FilterResults results = new FilterResults();
            results.values = ds;
            results.count = ds.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results.count > 0) {
                addAll((List<Diagnosis>) results.values);
            }
        }
    }
}
