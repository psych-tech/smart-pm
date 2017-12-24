package com.emolance.enterprise.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yusun on 5/26/15.
 */
public class UserListAdapter extends ArrayAdapter<EmoUser> implements Filterable {

    private List<EmoUser> users;
    private List<EmoUser> orig;
    private Context context;
    private AdminFragment adminFragment;

    public UserListAdapter(Context context, List<EmoUser> objects, AdminFragment adminFragment) {
        super(context, R.layout.list_user_item, objects);
        this.context = context;
        this.users = objects;
        this.adminFragment = adminFragment;
        orig = new ArrayList<>();
        orig.addAll(users);
    }

    public class EmoUserHolder
    {
        TextView name;
        TextView org;
        ImageView profileImageView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        EmoUserHolder holder;
        if(convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_user_item, parent, false);
            holder=new EmoUserHolder();
            holder.name= (TextView) convertView.findViewById(R.id.userNameText);
            holder.org=(TextView) convertView.findViewById(R.id.orgText);
            holder.profileImageView = (ImageView) convertView.findViewById(R.id.userProfileImage);
            convertView.setTag(holder);
        }
        else {
            holder=(EmoUserHolder) convertView.getTag();
        }

        holder.name.setText(users.get(position).getName());
        holder.org.setText(users.get(position).getOrganization().getName());

        if(users.get(position).getProfileImage() != null){
            String uri = users.get(position).getProfileImage() ;
            int imageResource = context.getResources().getIdentifier(uri,null,context.getApplicationContext().getPackageName());
            holder.profileImageView.setImageDrawable(context.getResources().getDrawable(imageResource));
        }
        return convertView;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        users.clear();
        if (charText.length() == 0) {
            users.addAll(orig);
        } else {
            for (EmoUser user : orig) {
                if (user.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    users.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void reset(){
        users.clear();
        users.addAll(orig);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


}
