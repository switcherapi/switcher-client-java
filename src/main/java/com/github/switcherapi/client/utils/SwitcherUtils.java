package com.github.switcherapi.client.utils;

import java.util.Date;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
import com.github.switcherapi.client.factory.SwitcherExecutor;
import com.google.gson.Gson;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherUtils {
	
	private static final Logger logger = LogManager.getLogger(SwitcherUtils.class);
	
	private static final String LOG_DATE = "date: %s";
	
	private static final String LOG_TME = "time: %s";
	
	private static final String LOG_ADDVALUE = "addValue: %s";
	
	/**
	 * [0] = s (seconds) [1] = m (minutes) [2] = h (hours) [3] = d (days)
	 */
	private static final String[] DURATION = { "s", "m", "h", "d" };
	
	private static final String FULL_DATE_REGEX = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";
	
	private static SnapshotWatcher watcher;
	
	private SwitcherUtils() {}
	
	public static Date addTimeDuration(final String addValue, final Date date) 
			throws SwitcherInvalidDateTimeArgumentException {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(LOG_ADDVALUE, addValue));
			logger.debug(String.format(LOG_DATE, date));
		}
		
		if (addValue.endsWith(DURATION[0])) {
			return DateUtils.addSeconds(date, Integer.parseInt(addValue.replace(DURATION[0], StringUtils.EMPTY)));
		} else if (addValue.endsWith(DURATION[1])) {
			return DateUtils.addMinutes(date, Integer.parseInt(addValue.replace(DURATION[1], StringUtils.EMPTY)));
		} else if (addValue.endsWith(DURATION[2])) {
			return DateUtils.addHours(date, Integer.parseInt(addValue.replace(DURATION[2], StringUtils.EMPTY)));
		} else if (addValue.endsWith(DURATION[3])) {
			return DateUtils.addDays(date, Integer.parseInt(addValue.replace(DURATION[3], StringUtils.EMPTY)));
		}
		
		throw new SwitcherInvalidDateTimeArgumentException(addValue);
	}
	
	public static String getFullDate(final String date) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(LOG_DATE, date));
		}
		
		final String time = RegExUtils.removePattern(date, FULL_DATE_REGEX).trim();
		return getFullTime(date, time);
	}

	public static String getFullTime(final String date, final String time) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(LOG_DATE, date));
			logger.debug(String.format(LOG_TME, time));
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
	
	public static void watchSnapshot(final SwitcherExecutor executorInstance) {
		if (watcher == null)
			watcher = new SnapshotWatcher(executorInstance);
		
		new Thread(watcher, SnapshotWatcher.class.toString()).start();
	}
	
	public static void stopWatchingSnapshot() {
		if (watcher != null)
			watcher.terminate();
	}

}
