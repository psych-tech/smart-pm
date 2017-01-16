package com.emolance.enterprise.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;

import java.util.List;

/**
 * Created by yusun on 5/26/15.
 */
public class UserListAdapter extends ArrayAdapter<EmoUser> {

    private List<EmoUser> users;
    private Context context;
    private AdminFragment adminFragment;

    public UserListAdapter(Context context, List<EmoUser> objects, AdminFragment adminFragment) {
        super(context, R.layout.list_user_item, objects);
        this.context = context;
        this.users = objects;
        this.adminFragment = adminFragment;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.list_user_item, parent, false);

        TextView nameText = (TextView) view.findViewById(R.id.userNameText);
        nameText.setText(users.get(position).getName());

        TextView orgText = (TextView) view.findViewById(R.id.orgText);
        orgText.setText(users.get(position).getOrganization().getName());

        final ImageView profileImageView = (ImageView) view.findViewById(R.id.userProfileImage);
        int profileIndex = 0; // reports.get(position).getProfilePhotoIndex();
        profileImageView.setImageResource(UserReportCreatorActivity.profileList.get(profileIndex));

        final Button opButton = (Button) view.findViewById(R.id.testsButton);

        opButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adminFragment.openUserTestsFragment(position);
            }
        });

        return view;
    }


}
