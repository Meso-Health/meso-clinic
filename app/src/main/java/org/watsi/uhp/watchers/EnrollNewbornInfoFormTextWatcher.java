package org.watsi.uhp.watchers;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class EnrollNewbornInfoFormTextWatcher implements TextWatcher {

    private EditText nameEdit;
    private RadioGroup genderGroup;
    private EditText cardIdEdit;
    private Button saveButton;

    public EnrollNewbornInfoFormTextWatcher(EditText nameEdit, RadioGroup genderGroup, EditText cardIdEdit, Button saveButton) {
        this.nameEdit = nameEdit;
        this.genderGroup = genderGroup;
        this.cardIdEdit = cardIdEdit;
        this.saveButton = saveButton;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // no-op
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // no-op
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (nameEdit.getText().toString().isEmpty() || genderGroup.getCheckedRadioButtonId() == -1 || cardIdEdit.getText().toString().isEmpty()) {
            saveButton.setEnabled(false);
        } else {
            saveButton.setEnabled(true);
        }
    }
}
