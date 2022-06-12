package com.github.switcherapi.client.service.validators;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.github.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.utils.SwitcherUtils;

@ValidatorComponent(type = Entry.DATE)
public class DateValidator extends Validator {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Override
	public boolean process(final Strategy strategy, final Entry switcherInput) throws SwitcherInvalidOperationException,
			SwitcherInvalidTimeFormat, SwitcherInvalidOperationInputException {

		try {
			return selectDateOperationCase(strategy, switcherInput);
		} catch (ParseException e) {
			logger.error(e);
			throw new SwitcherInvalidTimeFormat(strategy.getStrategy(), e);
		}
	}

	private boolean selectDateOperationCase(final Strategy strategy, final Entry switcherInput) throws ParseException {
		Date stgDate;
		Date stgDate2;
		Date inputDate;

		switch (strategy.getOperation()) {
		case Entry.LOWER:
			stgDate = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(SwitcherUtils.getFullDate(switcherInput.getInput()), DATE_FORMAT);

			return inputDate.before(stgDate);
		case Entry.GREATER:
			stgDate = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(SwitcherUtils.getFullDate(switcherInput.getInput()), DATE_FORMAT);

			return inputDate.after(stgDate);
		case Entry.BETWEEN:
			if (strategy.getValues().length == 2) {
				stgDate = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[0]), DATE_FORMAT);
				stgDate2 = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[1]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(SwitcherUtils.getFullDate(switcherInput.getInput()), DATE_FORMAT);

				return inputDate.after(stgDate) && inputDate.before(stgDate2);
			}

			throw new SwitcherInvalidOperationInputException(Entry.BETWEEN);
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

}
