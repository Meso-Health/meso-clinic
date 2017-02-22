package org.watsi.uhp.managers;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class KeyboardManager {
    public static void hideKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);

    }
}
