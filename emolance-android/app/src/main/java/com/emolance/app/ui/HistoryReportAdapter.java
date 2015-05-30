package com.emolance.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.emolance.app.R;
import com.emolance.app.data.Report;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

import java.util.List;
import java.util.Locale;

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

        DateTime dateTime = DateTime.parse(reports.get(position).getTimestamp(),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

        String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
                .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

        TextView timeText = (TextView) view.findViewById(R.id.timeText);
        timeText.setText(dateTimeStr);

        TextView valueText = (TextView) view.findViewById(R.id.valueText);
        valueText.setText(Double.toString(reports.get(position).getValue()));

        return view;
    }
}
