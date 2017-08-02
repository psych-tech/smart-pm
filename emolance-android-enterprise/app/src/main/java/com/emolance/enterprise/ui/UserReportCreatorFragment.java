package com.emolance.enterprise.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.Organization;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Erick on 7/26/17.
 */

public class UserReportCreatorFragment extends Fragment {
    private NewMainActivity activity;
    private Context context;
    private String name;
    private String email;
    private int age;
    private int zipcode;
    private String organizationName;
    private String profession;

    @InjectView(R.id.nameEditText)
    EditText nameEditText;
    @InjectView(R.id.emailEditText)
    EditText emailEditText;
    @InjectView(R.id.ageEditText)
    EditText ageEditText;
    @InjectView(R.id.zipcodeEditText)
    EditText zipcodeEditText;
    @InjectView(R.id.organizationNameEditText)
    EditText organizationNameEditText;
    @InjectView(R.id.professionEditText)
    EditText professionEditText;
    @InjectView(R.id.createButton)
    Button createBtn;
    @InjectView(R.id.cancelButton)
    Button cancelBtn;
    @InjectView(R.id.profileImageSelector)
    ImageView profileImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_new_user_report, container, false);
        ButterKnife.inject(this, rootView);
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
        profileImageView.setOnClickListener(new View.OnClickListener() {
            int count = 0;
            @Override
            public void onClick(View v) {
                if(count >= 7){
                    count = 1;
                }
                else{
                    count++;
                }
                String uri = "@drawable/persona_landing_" + count;
                int imageResource = getResources().getIdentifier(uri,null,getActivity().getPackageName());
                profileImageView.setImageDrawable(getResources().getDrawable(imageResource));
            }
        });
        return rootView;
    }

    private void errorCheck(){
        String nameEntry = nameEditText.getText().toString().trim();
        String emailEntry = emailEditText.getText().toString().trim();
        String ageEntry = ageEditText.getText().toString().trim();
        String zipEntry = zipcodeEditText.getText().toString().trim();
        String organizationNameEntry = organizationNameEditText.toString().trim();
        String professionEntry = professionEditText.getText().toString().trim();

        if(nameEntry.isEmpty()){
            nameEditText.setError("Name field cannot be empty.");
        }
        if(emailEntry.isEmpty()){
            emailEditText.setError("Email field cannot be empty.");
        }
        if(ageEntry.isEmpty()){
            ageEditText.setError("Age field cannot be empty.");
        }
        if(zipEntry.isEmpty()){
            zipcodeEditText.setError("Zipcode field cannot be empty.");
        }
        if(professionEntry.isEmpty()){
            professionEditText.setError("Profession field cannot be empty.");
        }
        if(!nameEntry.isEmpty() && !emailEntry.isEmpty() && !ageEntry.isEmpty() && !zipEntry.isEmpty()
                && !organizationNameEntry.isEmpty() && !professionEntry.isEmpty()){
            name = nameEntry;
            email = emailEntry;
            age = Integer.parseInt(ageEntry);
            zipcode = Integer.parseInt(zipEntry);
            organizationName = organizationNameEntry;
            profession = professionEntry;
            activity.onBackPressed();
        }
    }

    public void createNewUser(){

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
