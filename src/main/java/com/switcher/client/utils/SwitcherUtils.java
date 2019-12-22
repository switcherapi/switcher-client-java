package com.switcher.client.utils;

import java.util.Date;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SwitcherUtils {
	
	final static Logger logger = Logger.getLogger(SwitcherUtils.class);
	
	public static Date addTimeDuration(final String addValue, final Date date) throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("addValue: %s", addValue));
			logger.debug(String.format("date: %s", date));
		}
		
		if (addValue.endsWith("s")) {
			return DateUtils.addSeconds(date, Integer.parseInt(addValue.replace("s", StringUtils.EMPTY)));
		} else if (addValue.endsWith("m")) {
			return DateUtils.addMinutes(date, Integer.parseInt(addValue.replace("m", StringUtils.EMPTY)));
		} else if (addValue.endsWith("h")) {
			return DateUtils.addHours(date, Integer.parseInt(addValue.replace("h", StringUtils.EMPTY)));
		} else if (addValue.endsWith("d")) {
			return DateUtils.addDays(date, Integer.parseInt(addValue.replace("d", StringUtils.EMPTY)));
		}
		
		throw new Exception(String.format("Something went wrong. It was not possible to convert this time duration %s", addValue));
	}
	
	public static String getFullDate(final String date) {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("date: %s", date));
		}
		
		final String time = RegExUtils.removePattern(date, "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))").trim();
		return getFullTime(date, time);
	}

	public static String getFullTime(final String date, final String time) {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("date: %s", date));
			logger.debug(String.format("time: %s", time));
		}
		
		if (StringUtils.isBlank(time)) {
			return String.format("%s 00:00:00", date);
		} else if (time.split(":").length == 1) {
			return String.format("%s %s:00:00", date.split(" ")[0], time);
		} else if (time.split(":").length == 2) {
			return String.format("%s %s:00", date.split(" ")[0], time);
		}

		return date;
	}
	
	public static boolean isJson(final String Json) {
		
        final Gson gson = new Gson();
        try {
            gson.fromJson(Json, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

}
