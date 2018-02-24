package com.emolance.enterprise.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.util.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Erick on 7/26/17.
 */

public class UserReportCreatorFragment extends Fragment {

    @Inject
    EmolanceAPI emolanceAPI;

    private NewMainActivity activity;
    private String name;
    private String gender;
    private String email;
    private String dob;
    private String position;
    private String login;
    private String profileUri;
    private String[] emails;
    private String[] userNames;
    private boolean checked;

    @InjectView(R.id.nameEditText)
    EditText nameEditText;
    @InjectView(R.id.genderEditText)
    EditText genderEditText;
    @InjectView(R.id.genderPicker)
    Button genderPickerBtn;
    @InjectView(R.id.dateOfBirthEditText)
    EditText dateOfBirthEditText;
    @InjectView(R.id.datePicker)
    Button datePickerBtn;
    @InjectView(R.id.emailEditText)
    EditText emailEditText;
    @InjectView(R.id.positionEditText)
    EditText positionEditText;
    @InjectView(R.id.createButton)
    Button createBtn;
    @InjectView(R.id.cancelButton)
    ImageButton cancelBtn;
    @InjectView(R.id.profileImageSelector)
    ImageView profileImageView;
    @InjectView(R.id.checkBoxEmailAutomatically)
    CheckBox checkBox;
    @InjectView(R.id.sendReportAutoText)
    TextView sendReportTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_new_user_report, container, false);
        userNames = getArguments().getStringArray(Constants.LIST_USERNAMES);
        emails = getArguments().getStringArray(Constants.LIST_EMAILS);
        ButterKnife.inject(this, rootView);
        Injector.inject(this);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorCheck();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        profileUri = "@drawable/persona_landing_2";
        profileImageView.setOnClickListener(new View.OnClickListener() {
            int count = 2;
            @Override
            public void onClick(View v) {
                if(count >= 7){
                    count = 1;
                }
                else{
                    count++;
                }
                profileUri = "@drawable/persona_landing_" + count;
                int imageResource = getResources().getIdentifier(profileUri,null,getActivity().getPackageName());
                profileImageView.setImageDrawable(getResources().getDrawable(imageResource));
            }
        });
        sendReportTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()){
                    checkBox.setChecked(false);
                }
                else{
                    checkBox.setChecked(true);
                }
            }
        });
        genderEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = new GenderDialogFragment();
                fragment.show(getFragmentManager(),"genderPicker");
            }
        });
        genderPickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = new GenderDialogFragment();
                fragment.show(getFragmentManager(),"genderPicker");
            }
        });
        dateOfBirthEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = new DatePickerFragment();
                fragment.show(getFragmentManager(),"datePicker");
            }
        });
        datePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = new DatePickerFragment();
                fragment.show(getFragmentManager(),"datePicker");
            }
        });
        return rootView;
    }

    private void errorCheck(){
        String nameEntry = nameEditText.getText().toString().trim();
        String genderEntry = genderEditText.getText().toString().trim();
        String dateOfBirthEntry = dateOfBirthEditText.getText().toString().trim();
        String emailEntry = emailEditText.getText().toString().trim();
        String positionEntry = positionEditText.getText().toString().trim();
        checked = checkBox.isChecked();
        if(nameEntry.isEmpty()){
            nameEditText.setError(getResources().getString(R.string.create_user_name_error));
        }
        if(genderEntry.isEmpty()){
            genderEditText.setError(getResources().getString(R.string.create_user_gender_error));
        }
        if(dateOfBirthEntry.isEmpty()){

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            dateFormat.setLenient(false);
            try {
                dateFormat.parse(dateOfBirthEntry);
            } catch (ParseException pe) {
                dateOfBirthEditText.setError(getResources().getString(R.string.create_user_dob_error));
            }
        }
        if(emailEntry.isEmpty()){
            emailEditText.setError(getResources().getString(R.string.create_user_email_error));
        }
        if(positionEntry.isEmpty()){
            positionEditText.setError(getResources().getString(R.string.create_user_position_error));
        }
        if(!emailEntry.isEmpty() && checkEmail(emailEntry)){
            emailEditText.setError(getResources().getString(R.string.create_user_email_taken_error));
        }
        else if(!nameEntry.isEmpty() && checkUserName(nameEntry)){
            nameEditText.setError(getResources().getString(R.string.create_user_name_taken_error));
        }
        else{
            if(!nameEntry.isEmpty() && !genderEntry.isEmpty() && !dateOfBirthEntry.isEmpty() && !emailEntry.isEmpty() && !positionEntry.isEmpty()){
                name = nameEntry;
                gender = genderEntry;
                dob = dateOfBirthEntry;
                email = emailEntry;
                position = positionEntry;
                login = emailEntry;
                
                // TODO
                // There is a Hack here. Could we put "HH " + DOB + " " + Gender in the LastName Field?
                // The idea is that when loading the user info, if lastName starts with "HH", we are going to split it by " " and get the DOB and Gender
                // Otherwise, we will use it for lastName
                Call<ResponseBody> responseBodyCall = emolanceAPI.createUser(email.substring(0, email.indexOf("@")), name, "HH " + dob + " " + gender, email, profileUri, position);
                responseBodyCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.i("TEST", "Created user successfully");
                            activity.updateList();
                            //Toast.makeText(activity, getActivity().getResources().getString(R.string.create_user_success_toast), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Log.i("TEST", "Failed to Created user successfully");
                            /*Toast.makeText(activity, getActivity().getResources().getString(R.string.create_user_failure_toast),
                                    Toast.LENGTH_SHORT).show();*/
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(activity, getActivity().getResources().getString(R.string.create_user_failure_toast), Toast.LENGTH_SHORT).show();
                    }
                });
                activity.onBackPressed();
            }
        }
    }

    public boolean checkEmail(String s){
        if(Arrays.asList(emails).contains(s)){
            return true;
        }
        return false;
    }

    public boolean checkUserName(String s){
        if(Arrays.asList(userNames).contains(s)){
            return true;
        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        activity = (NewMainActivity) getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //removes keyboard if it is open
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

}
