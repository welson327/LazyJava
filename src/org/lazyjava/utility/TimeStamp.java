package org.lazyjava.utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;

public class TimeStamp 
{
	public static String getTimeStamp(String separater, boolean zero_padding) {
        Calendar c = Calendar.getInstance();
        String sep = (separater!=null) ? separater : "";
        String format = zero_padding ? "%04d%s%02d%s%02d%s%02d%s%02d%s%02d" : "%d%s%d%s%d%s%d%s%d%s%d";
        return String.format(format, c.get(Calendar.YEAR),
                                     sep,
                                     c.get(Calendar.MONTH)+1,
                                     sep,
                                     c.get(Calendar.DAY_OF_MONTH),
                                     sep,
                                     c.get(Calendar.HOUR_OF_DAY),
                                     sep,
                                     c.get(Calendar.MINUTE),
                                     sep,
                                     c.get(Calendar.SECOND));
    }
	
	public static String getUTCTimeStamp() {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    String utcDate = sdf.format(new Date());
	    return utcDate;
	}
	
	public static String getDateFormatString(long ts, String format) {
		SimpleDateFormat df = new SimpleDateFormat(format); // format ex: [yyyy-MM-dd kk:mm:ss], [dd/MM/yyyy]
		return df.format(new Date(ts));
	}
	
	//=================================================================
	// Purpose:		
	// Parameters:	timeZone: 
	//					ex: TimeZone.getDefault(), 
	//						TimeZone.getTimeZone("GMT"),
	//						TimeZone.getDefault().setRawOffset(12*60*60*1000)
	// Return:
	// Remark:		http://stackoverflow.com/questions/308683/how-can-i-get-the-current-date-and-time-in-utc-or-gmt-in-java
	// Authro:		welson
	//=================================================================
	public static String getDateFormatString(long ts, String format, TimeZone timeZone) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(timeZone);
		return df.format(new Date(ts));
	}
	
	public static long getTimeStampByDateFormat(String date, String format) throws ParseException {
		DateFormat df = new SimpleDateFormat(format); // ex: "yyyy-MM-dd"
		return ((Date) df.parse(date)).getTime(); 
	}
	
	public static String addDays(String date, int amount, String format) throws ParseException {
		Calendar c = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat(format);
		c.setTime(df.parse(date)); 		// ex: "yyyy-MM-dd" or "yyyy-MM-dd kk:mm:ss"
		c.add(Calendar.DATE, amount);  	// number of days to add(minus is permitted)
		return df.format(c.getTime());
	}
	
	public static boolean expired(long experimentGroupTime, long controlGroupTime, long interval) {
		if(experimentGroupTime > controlGroupTime + interval) {
			return true;
		} else {
			return false;
		}
	}
}
