package com.emolance.enterprise.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String dob;
    private String groupSelected;
    private String profession;
    private String profileUri;
    private String[] emails;
    private String[] userNames;

    @InjectView(R.id.userNameEditText)
    EditText userNameEditText;
    @InjectView(R.id.firstNameEditText)
    EditText firstNameEditText;
    @InjectView(R.id.dateOfBirthEditText)
    EditText dateOfBirthEditText;
    @InjectView(R.id.datePicker)
    Button datePickerBtn;
    @InjectView(R.id.emailEditText)
    EditText emailEditText;
    @InjectView(R.id.spinnerGroupNewUser)
    Spinner spinnerGroup;
    @InjectView(R.id.createButton)
    Button createBtn;
    @InjectView(R.id.cancelButton)
    ImageButton cancelBtn;
    @InjectView(R.id.profileImageSelector)
    ImageView profileImageView;

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
        String[] spinnerArray = getResources().getStringArray(R.array.new_user_group);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, spinnerArray);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerGroup.setAdapter(spinnerAdapter);
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
        String userNameEntry = userNameEditText.getText().toString().trim();
        String firstNameEntry = firstNameEditText.getText().toString().trim();
        String dateOfBirthEntry = dateOfBirthEditText.getText().toString().trim();
        String emailEntry = emailEditText.getText().toString().trim();
        String spinnerEntry = spinnerGroup.getSelectedItem().toString();
        if(firstNameEntry.isEmpty()){
            firstNameEditText.setError("First Name field cannot be empty.");
        }
        if(dateOfBirthEntry.isEmpty()){

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            dateFormat.setLenient(false);
            try {
                dateFormat.parse(dateOfBirthEntry);
            } catch (ParseException pe) {
                dateOfBirthEditText.setError("Date of birth must be in MM/dd/yyyy format.");
            }
        }
        if(emailEntry.isEmpty()){
            emailEditText.setError("Email field cannot be empty.");
        }
        if(spinnerEntry.equals("---Select Group---")){
            spinnerEntry = "";
        }
        if(!emailEntry.isEmpty() && checkEmail(emailEntry)){
            emailEditText.setError("Email field has been taken.");
        }
        else if(!userNameEntry.isEmpty() && checkUserName(userNameEntry)){
            userNameEditText.setError("User Name field has been taken.");
        }
        else{
            if(!userNameEntry.isEmpty() && !firstNameEntry.isEmpty() && !dateOfBirthEntry.isEmpty() && !emailEntry.isEmpty() ){
                userName = userNameEntry;
                firstName = firstNameEntry;
                dob = dateOfBirthEntry;
                email = emailEntry;
                groupSelected = spinnerEntry;

                // This is how to post a new uesr:
                // We need the following fields, please modify the UI accordingly:
                //  - Username (login)
                //  - First Name
                //  - Last Name
                //  - Email
                //  - ProfileImage. Use "1", "2", "3", "4" to indicate which image you have chosen from the list of the images we have
                //  - Profession
                //
                // We don't need the organization, age, zipcode anymore
                // Please test this and make sure it refreshes the fragment after adding it
                //Call<ResponseBody> responseBodyCall = emolanceAPI.createUser(userName, firstName, dob, email, profileUri, groupSelected);
                Call<ResponseBody> responseBodyCall = emolanceAPI.createUser(userName, firstName, dob, email, profileUri, profession);
                responseBodyCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        activity.updateList();
                        Toast.makeText(activity, "Successfully created the user.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(activity, "Failed to create the user.", Toast.LENGTH_SHORT).show();
                    }
                });
                activity.onBackPressed();
            }
        }
    }

    public void createNewUser(){

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
        //will use to create new EmoUser

    }

}
