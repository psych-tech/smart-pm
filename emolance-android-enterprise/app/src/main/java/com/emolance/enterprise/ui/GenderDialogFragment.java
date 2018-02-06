package com.emolance.enterprise.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.emolance.enterprise.R;

/**
 * Created by user on 1/14/18.
 */

public class GenderDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.dialog_gender_select, null);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.genderRadioGroup);
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.create_user_gender_dialog))
                .setView(view)
                .setPositiveButton(getResources().getString(R.string.create_user_gender_dialog_confirm),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                int radioID = radioGroup.getCheckedRadioButtonId();
                                RadioButton radioButton = (RadioButton) view.findViewById(radioID);
                                EditText genderEditText = (EditText) getActivity().findViewById(R.id.genderEditText);
                                genderEditText.setText(radioButton.getText());
                                if(genderEditText.getError() != null || genderEditText.getText().length() == 0){
                                    genderEditText.setError(null);
                                }
                                dialog.dismiss();
                            }
                        }
                )
                .setNegativeButton(getResources().getString(R.string.create_user_gender_dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Log.i(String.valueOf(radioGroup.getCheckedRadioButtonId()),"");
                if(radioGroup.getCheckedRadioButtonId() == -1){
                    ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(!alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()){
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
        return alertDialog;
    }
}
