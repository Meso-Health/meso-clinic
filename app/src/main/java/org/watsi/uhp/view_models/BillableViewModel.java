package org.watsi.uhp.view_models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.view.View;
import android.widget.Spinner;

import org.watsi.uhp.BR;
import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.models.Billable;

import java.util.ArrayList;
import java.util.List;

// This inverse binding method here is needed in order to expose attribute android:selectedItemPosition in XML
// and bind to the method Spinner.getSelectedItemPosition()
@InverseBindingMethods({
        @InverseBindingMethod(type = Spinner.class, attribute = "android:selectedItemPosition"),
})

public class BillableViewModel extends BaseObservable {
    private final static String SELECT_TYPE_PROMPT = "Select A Category...";
    private final Billable mBillable;
    private final FormFragment mFormFragment;
    private List<String> mBillableTypeChoices;

    private Integer mSelectedTypeIndex;

    public BillableViewModel(FormFragment formFragment) {
        mBillable = new Billable();
        mFormFragment = formFragment;
        mSelectedTypeIndex = 0;

        setUpBillableTypes();
    }

    private void setUpBillableTypes() {
        List<String> types = new ArrayList<>(Billable.getBillableTypes());
        types.add(0, SELECT_TYPE_PROMPT);

        mBillableTypeChoices = types;
    }

    @Bindable
    public String getName() {
        return mBillable.getName();
    }

    @Bindable
    public String getPrice() {
        if (mBillable.getPrice() == null) {
            return "";
        } else {
            return mBillable.getPrice().toString();
        }
    }

    @Bindable
    public String getUnit() {
        return mBillable.getUnit();
    }

    @Bindable
    public void setUnit(String unit) {
        mBillable.setUnit(unit);
        notifyPropertyChanged(BR.saveEnabled);
    }

    @Bindable
    public void setName(String name) {
        mBillable.setName(name);
        notifyPropertyChanged(BR.saveEnabled);
    }

    @Bindable
    public void setPrice(String price) {
        try {
            mBillable.setPrice(Integer.parseInt(price));
        } catch (NumberFormatException e) {
            mBillable.setPrice(null);
        }
        notifyPropertyChanged(BR.saveEnabled);
    }

    public Billable getBillable() {
        return mBillable;
    }

    @Bindable
    public List<String> getTypeSelections() {
        return mBillableTypeChoices;
    }

    @Bindable
    public String getComposition() {
        return mBillable.getComposition();
    }

    @Bindable
    public void setComposition(String composition) {
        mBillable.setComposition(composition);
        notifyPropertyChanged(BR.saveEnabled);
    }

    @Bindable
    public void setSelectedTypeIndex(Integer i) {
        if (i > 0) {
            mSelectedTypeIndex = i;
            Billable.TypeEnum selectedType = Billable.TypeEnum.valueOf(mBillableTypeChoices.get(mSelectedTypeIndex));
            mBillable.setType(selectedType);
            if (selectedType.equals(Billable.TypeEnum.VACCINE)) {
                // According to our DB, all vaccines are of composition "vial".
                mBillable.setComposition("vial");
            } else {
                mBillable.setComposition(null);
            }

            notifyPropertyChanged(BR.showUnit);
            notifyPropertyChanged(BR.showComposition);
            notifyPropertyChanged(BR.saveEnabled);
        }
    }

    @Bindable
    public Integer getSelectedTypeIndex() {
        return mSelectedTypeIndex;
    }

    @Bindable
    public boolean getSaveEnabled() {
        return mBillable.valid();
    }

    @Bindable
    public int getShowComposition() {
        if (mBillable.getType() != null && mBillable.getType().equals(Billable.TypeEnum.DRUG)) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    @Bindable
    public int getShowUnit() {
        if (mBillable.getType() != null && (mBillable.getType().equals(Billable.TypeEnum.DRUG) || mBillable.getType().equals(Billable.TypeEnum.VACCINE))) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public void onClickSave() {
        mFormFragment.nextStep();
    }
}
