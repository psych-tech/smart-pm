package com.emolance.enterprise.util;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

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
}
