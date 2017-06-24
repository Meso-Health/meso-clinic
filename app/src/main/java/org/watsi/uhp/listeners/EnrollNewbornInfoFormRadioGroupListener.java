package org.watsi.uhp.listeners;

import android.support.annotation.IdRes;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class EnrollNewbornInfoFormRadioGroupListener implements RadioGroup.OnCheckedChangeListener {

    private EditText nameEdit;
    private RadioGroup genderGroup;
    private EditText cardIdEdit;
    private Button saveButton;

    public EnrollNewbornInfoFormRadioGroupListener(EditText nameEdit, RadioGroup genderGroup, EditText cardIdEdit, Button saveButton) {
        this.nameEdit = nameEdit;
        this.genderGroup = genderGroup;
        this.cardIdEdit = cardIdEdit;
        this.saveButton = saveButton;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (nameEdit.getText().toString().isEmpty() || genderGroup.getCheckedRadioButtonId() == -1 || cardIdEdit.getText().toString().isEmpty()) {
            saveButton.setEnabled(false);
        } else {
            saveButton.setEnabled(true);
        }
    }
}
