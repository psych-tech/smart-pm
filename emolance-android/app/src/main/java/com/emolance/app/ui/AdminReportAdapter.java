package com.emolance.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.app.R;
import com.emolance.app.data.Report;
import com.emolance.app.service.EmolanceAPI;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by yusun on 5/26/15.
 */
public class AdminReportAdapter extends ArrayAdapter<Report> {

    private List<Report> reports;
    private Context context;
    private EmolanceAPI emolanceAPI;

    public AdminReportAdapter(Context context, List<Report> objects, EmolanceAPI emolanceAPI) {
        super(context, R.layout.list_user_report_item, objects);
        this.context = context;
        this.reports = objects;
        this.emolanceAPI = emolanceAPI;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_user_report_item, parent, false);
        }

        DateTime dateTime = DateTime.parse(reports.get(position).getTimestamp(),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

        String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
                .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

        TextView nameText = (TextView) view.findViewById(R.id.nameText);
        nameText.setText(reports.get(position).getName());

        final TextView qrText = (TextView) view.findViewById(R.id.qrText);
        qrText.setText("QR ID: " + reports.get(position).getQrcode());

        TextView valueText = (TextView) view.findViewById(R.id.statusText);
        valueText.setText("Status: " + reports.get(position).getStatus());

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        final Button opButton = (Button) view.findViewById(R.id.opButton);
        opButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                opButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                emolanceAPI.triggerProcess("0000000047bb064b", reports.get(position).getQrcode(), new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Toast.makeText(context,"Processed successfully", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        opButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(context,"Process Failed", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        opButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        return view;
    }
}
