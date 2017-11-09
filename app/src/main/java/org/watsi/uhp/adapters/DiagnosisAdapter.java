package org.watsi.uhp.adapters;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;

import org.watsi.uhp.database.DiagnosisDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Diagnosis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiagnosisAdapter extends ArrayAdapter<Diagnosis> {
    public final Integer MIN_LENGTH_BEFORE_DISPLAY_SEARCH_RESULTS = 2;
    Filter mFilter;

    public DiagnosisAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId) {
        super(context, resource, textViewResourceId);
        mFilter = new DiagnosisListFilter();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public List<Diagnosis> onSearchWithQueryString(String queryString) throws SQLException {
        return DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias(queryString);
    }

    public class DiagnosisListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Diagnosis> ds = new ArrayList<>();
            if (constraint != null && constraint.length() >= MIN_LENGTH_BEFORE_DISPLAY_SEARCH_RESULTS) {
                try {
                    ds = onSearchWithQueryString(constraint.toString());
                } catch (SQLException e) {
                    ExceptionManager.reportException(e);
                }
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