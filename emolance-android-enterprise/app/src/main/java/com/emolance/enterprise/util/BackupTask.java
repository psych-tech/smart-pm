package com.emolance.enterprise.util;

import android.os.AsyncTask;
import android.util.Log;

import com.emolance.enterprise.data.Report;
import com.firebase.client.Firebase;

import java.io.File;

/**
 * Created by yusun on 4/10/16.
 */
public class BackupTask extends AsyncTask<Report, Void, Void> {

    private File imageFile;
    private Firebase ref;

    public BackupTask(Firebase ref, File imageFile) {
        this.ref = ref;
        this.imageFile = imageFile;
    }

    @Override
    protected Void doInBackground(Report... params) {
        // backup test result
        Log.i("Admin", "Backup history report ...");
        if (params.length > 0) {
            ref.child("history/" + params[0].getTimestamp()).setValue(params[0]);
        }
        // backup images
        Log.i("Admin", "Uploading file to S3 ...");
        S3FileUploader.uploadPhotoSync(imageFile);
        return null;
    }

}