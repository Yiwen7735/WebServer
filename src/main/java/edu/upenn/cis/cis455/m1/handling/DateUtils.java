package edu.upenn.cis.cis455.m1.handling;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis.cis455.exceptions.HaltException;


public class DateUtils {

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String OTHER_DATE_FORMAT1 = "EEE MMM dd HH:mm:ss yyyy";
    public static final String OTHER_DATE_FORMAT2 = "EEE, dd-MMM-yy HH:mm:ss zzz";
    
    private static SimpleDateFormat getSDF() {
    	SimpleDateFormat sdf = new SimpleDateFormat();
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    	return sdf;
    }

    public static long dateToUnix(String date) {
        Date dt;
        SimpleDateFormat sdf = getSDF();
        try {
            sdf.applyPattern(HTTP_DATE_FORMAT);
            dt = sdf.parse(date);
        } catch (ParseException e1) {
            try {
                sdf.applyPattern(OTHER_DATE_FORMAT1);
                dt = sdf.parse(date);
            } catch (ParseException e2) {
                try {
                    sdf.applyPattern(OTHER_DATE_FORMAT2);
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    dt = sdf.parse(date);
                    cal.setTime(dt);
                    int yy = cal.get(Calendar.YEAR) % 100; // last two digit

                    int future = Calendar.getInstance().get(Calendar.YEAR) + 50;
                    yy += 2000 + yy > future ? 1900 : 2000;
                    
                    cal.set(Calendar.YEAR, yy);
                    dt = cal.getTime();
                    
                } catch (ParseException e3) {
                    throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
                }
            }
        }
        return dt.getTime();
    }

    public static String unixToDate(long date) {
    	SimpleDateFormat sdf = getSDF();
        sdf.applyPattern(HTTP_DATE_FORMAT);
        return sdf.format(new Date(date));
    }

    public static String getCurrentTime() {
    	SimpleDateFormat sdf = getSDF();
        sdf.applyPattern(HTTP_DATE_FORMAT);
        return sdf.format(new Date());
    }
}
