package com.github.switcherapi.client.service.validators;

import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class DateTimeValidator extends Validator {

	private static final String LOG_DATE = "date: {}";
	private static final String LOG_DATE_TIME = "date time: {} {}";
	private static final String FULL_DATE_REGEX = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";

	private static final String DATE_FORMAT = "%s 00:00:00";
	private static final String HOUR_FORMAT = "%s %s:00:00";
	private static final String MINUTE_FORMAT = "%s %s:00";
	private static final String TIME_SPLIT = ":";
	private static final String DATE_SPLIT = " ";

	protected String getFullDate(final String date) {
		SwitcherUtils.debug(logger, LOG_DATE, date);

		final String time = RegExUtils.removePattern((CharSequence) date, FULL_DATE_REGEX).trim();
		return getFullTime(date, time);
	}

	protected String getFullTime(final String date, final String time) {
		SwitcherUtils.debug(logger, LOG_DATE_TIME, date, time);

		if (StringUtils.isBlank(time)) {
			return String.format(DATE_FORMAT, date);
		}

		String[] timeSplit = time.split(TIME_SPLIT);
		if (timeSplit.length == 1) {
			return String.format(HOUR_FORMAT, date.split(DATE_SPLIT)[0], time);
		}

		if (timeSplit.length == 2) {
			return String.format(MINUTE_FORMAT, date.split(DATE_SPLIT)[0], time);
		}

		return date;
	}

}
