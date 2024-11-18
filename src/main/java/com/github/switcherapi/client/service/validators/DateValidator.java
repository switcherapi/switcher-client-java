package com.github.switcherapi.client.service.validators;

import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.github.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.EntryOperation;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.criteria.Strategy;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class DateValidator extends DateTimeValidator {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.DATE;
	}

	@Override
	public boolean process(final Strategy strategy, final Entry switcherInput) throws SwitcherInvalidOperationException,
			SwitcherInvalidTimeFormat, SwitcherInvalidOperationInputException {

		try {
			return selectDateOperationCase(strategy, switcherInput);
		} catch (ParseException e) {
			throw new SwitcherInvalidTimeFormat(strategy.getStrategy(), e);
		}
	}

	private boolean selectDateOperationCase(final Strategy strategy, final Entry switcherInput) throws ParseException {
		Date stgDate;
		Date stgDate2;
		Date inputDate;

		switch (strategy.getEntryOperation()) {
		case LOWER:
			stgDate = DateUtils.parseDate(getFullDate(strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(getFullDate(switcherInput.getInput()), DATE_FORMAT);

			return inputDate.before(stgDate);
		case GREATER:
			stgDate = DateUtils.parseDate(getFullDate(strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(getFullDate(switcherInput.getInput()), DATE_FORMAT);

			return inputDate.after(stgDate);
		case BETWEEN:
			if (strategy.getValues().length == 2) {
				stgDate = DateUtils.parseDate(getFullDate(strategy.getValues()[0]), DATE_FORMAT);
				stgDate2 = DateUtils.parseDate(getFullDate(strategy.getValues()[1]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(getFullDate(switcherInput.getInput()), DATE_FORMAT);

				return inputDate.after(stgDate) && inputDate.before(stgDate2);
			}

			throw new SwitcherInvalidOperationInputException(EntryOperation.BETWEEN.name());
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

}
