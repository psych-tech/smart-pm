package com.emolance.enterprise.util;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yusun on 1/16/17.
 */

public class DateUtils {

    public static String getCurrentDateStr() {
        long currentTimestamp = System.currentTimeMillis();
        DateTime dateTime = new DateTime();

//                DateTime.parse(testReport.getReportDate(),
//                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
//                        .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

        String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:sss'Z'")
                .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));
        return dateTimeStr;
    }

    public static String getDateBirthInStr(String dateStr) {
        if (dateStr == null) {
            return "N/A";
        }

        try {
            DateTime dateTime = DateTime.parse(dateStr,
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy")
                    .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            return dateTimeStr;
        } catch (IllegalArgumentException e) {
            DateTime dateTime = DateTime.parse(dateStr,
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy")
                    .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            return dateTimeStr;
        }
    }

    public static String getTestDateTimeInStr(String dateStr) {
        if (dateStr == null) {
            return "N/A";
        }

        try {
            DateTime dateTime = DateTime.parse(dateStr,
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
                    .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            return dateTimeStr;
        } catch (IllegalArgumentException e) {
            DateTime dateTime = DateTime.parse(dateStr,
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
                    .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            return dateTimeStr;
        }
    }

    public static Date getMillisecondsFromDate(String s){
        if(s == null){
            return null;
        }
        try {
            DateTime dateTime = DateTime.parse(s,
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            return dateTime.toDate();
        } catch (IllegalArgumentException e) {
            DateTime dateTime = DateTime.parse(s,
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

            return dateTime.toDate();
        }
    }
}
