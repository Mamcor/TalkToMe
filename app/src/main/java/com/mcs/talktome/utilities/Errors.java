package com.mcs.talktome.utilities;


import android.view.View;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Errors {
    public static void showError(TextInputLayout textInputLayout, String str) {
        textInputLayout.setError(str);
        textInputLayout.requestFocus();
    }
}
