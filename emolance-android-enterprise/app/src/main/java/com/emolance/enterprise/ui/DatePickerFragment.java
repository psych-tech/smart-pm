package com.emolance.enterprise.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

import com.emolance.enterprise.R;

import java.util.Calendar;

/**
 * Created by user on 12/12/17.
 */

public class DatePickerFragment extends DialogFragment implements android.app.DatePickerDialog.OnDateSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        EditText dobEditText = (EditText) getActivity().findViewById(R.id.dateOfBirthEditText);
        dobEditText.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
        if(dobEditText.getError() != null || dobEditText.getText().length() == 0){
            dobEditText.setError(null);
        }
    }
}
