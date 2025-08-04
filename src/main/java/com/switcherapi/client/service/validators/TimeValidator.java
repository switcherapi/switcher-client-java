package com.switcherapi.client.service.validators;

import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.EntryOperation;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.Strategy;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeValidator extends DateTimeValidator {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.TIME;
	}

	@Override
	public boolean process(Strategy strategy, Entry switcherInput) throws SwitcherInvalidOperationException,
			SwitcherInvalidTimeFormat, SwitcherInvalidOperationInputException {

		try {
			final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			final String today = format.format(new Date());
			return selectTimeOperationCase(strategy, switcherInput, today);
		} catch (ParseException e) {
			throw new SwitcherInvalidTimeFormat(strategy.getStrategy(), e);
		}

	}

	private boolean selectTimeOperationCase(final Strategy strategy, final Entry switcherInput, final String today)
			throws ParseException {
		Date stgDate;
		Date stgDate2;
		Date inputDate;

		switch (strategy.getEntryOperation()) {
		case LOWER:
			stgDate = DateUtils.parseDate(getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

			return inputDate.before(stgDate);
		case GREATER:
			stgDate = DateUtils.parseDate(getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

			return inputDate.after(stgDate);
		case BETWEEN:
			if (strategy.getValues().length == 2) {
				stgDate = DateUtils.parseDate(getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
				stgDate2 = DateUtils.parseDate(getFullTime(today, strategy.getValues()[1]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(getFullTime(today, switcherInput.getInput()),
						DATE_FORMAT);

				return inputDate.after(stgDate) && inputDate.before(stgDate2);
			}

			throw new SwitcherInvalidOperationInputException(EntryOperation.BETWEEN.name());
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

}
