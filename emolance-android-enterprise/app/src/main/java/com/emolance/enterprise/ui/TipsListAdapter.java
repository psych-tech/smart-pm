package com.emolance.enterprise.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.Tip;

import java.util.List;

/**
 * Created by user on 2/24/18.
 */

public class TipsListAdapter extends BaseAdapter {
    List<Tip> tips;
    Context context;

    public TipsListAdapter(List<Tip> tips, Context context) {
        this.tips = tips;
        this.context = context;
    }

    @Override
    public int getCount() {
        return tips.size();
    }

    @Override
    public Object getItem(int position) {
        return tips.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.list_tip_item, parent, false);
        }
        Tip currentTip = (Tip) getItem(position);
        TextView header = (TextView) convertView.findViewById(R.id.tip_header);
        TextView description = (TextView) convertView.findViewById(R.id.tip_description);
        header.setText(currentTip.getTitle());
        description.setText(currentTip.getDescription());
        return convertView;
    }
}
