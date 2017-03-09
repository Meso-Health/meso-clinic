package org.watsi.uhp.managers;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardManager {
    public static void showKeyboard(View view, Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void focusAndShowKeyboard(View view, Context context) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // This works in certain situations where focusAndShowKeyboard does not, but note that this
    // *toggles* the default keyboard, which (1) can actually hide the keyboard if it's
    // already showing, (2) may not even show the correct keyboard (e.g. default instead of
    // search or numeric), and (3) can cause unexpected behavior when pressing "back".
    public static void focusAndForceShowKeyboard(View view, Context context) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    public static void hideKeyboard(View view, Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
