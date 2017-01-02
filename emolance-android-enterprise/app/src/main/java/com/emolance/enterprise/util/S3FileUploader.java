package com.emolance.enterprise.util;

/**
 * Created by yusun on 4/10/16.
 */

import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;

/**
 * Created by yusun on 8/11/14.
 */
public class S3FileUploader {

    private static AmazonS3Client s3Client = new AmazonS3Client(
            new BasicAWSCredentials("AKIAJDVTSQYEMYC7NIFQ", "4mFmwn3+PHPYOX/GbEbSfn0Dq6/Td6CrGzwbj6j4"));

    public static final String PHOTO_S3_BUCKET = "emolance-enterprise-photos";

    public static void uploadPhotoSync(File photoFile) {
        if (photoFile != null) {
            PutObjectRequest por = new PutObjectRequest(PHOTO_S3_BUCKET, getPhotoS3Key(photoFile), photoFile);
            s3Client.putObject(por);
            Log.i("PhotoUploadingTask", "Successfully uploaded photo to S3");
            photoFile.delete();
            Log.i("PhotoUploadingTask", "Delete the photo file");
        }
    }

    public static String getPhotoS3Url(File photoFile) {
        return PHOTO_S3_BUCKET + ":" + getPhotoS3Key(photoFile);
    }

    private static String getPhotoS3Key(File photoFile) {
        if (photoFile != null) {
            return photoFile.getName();
        } else {
            return null;
        }
    }
}
