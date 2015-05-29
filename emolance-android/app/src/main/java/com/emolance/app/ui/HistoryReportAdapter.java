package com.emolance.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.emolance.app.R;
import com.emolance.app.data.Report;

import java.util.List;

/**
 * Created by yusun on 5/26/15.
 */
public class HistoryReportAdapter extends ArrayAdapter<Report> {

    private List<Report> reports;
    private Context context;

    public HistoryReportAdapter(Context context, List<Report> objects) {
        super(context, R.layout.list_history_item, objects);
        this.context = context;
        this.reports = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_history_item, parent, false);
        }

        TextView timeText = (TextView) view.findViewById(R.id.timeText);
        timeText.setText(reports.get(position).getTimestamp());
        TextView valueText = (TextView) view.findViewById(R.id.valueText);
        valueText.setText(Double.toString(reports.get(position).getValue()));

        return view;
    }
}
