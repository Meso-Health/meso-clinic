package org.watsi.uhp.custom_components;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.watsi.uhp.managers.KeyboardManager;

import java.util.List;

public class BillableCompositionInput extends AppCompatAutoCompleteTextView {
    public BillableCompositionInput(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCompositionChoices(List<String> compositionChoices) {
        ArrayAdapter<String> adapter = new ArrayAdapter(
                    getContext(),
                    android.R.layout.simple_list_item_1,
                    compositionChoices
        );
        setAdapter(adapter);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            showDropDown();
        }
    }

    @Override
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyboardManager.hideKeyboard(view, getContext());
            }
        };
    }
}
