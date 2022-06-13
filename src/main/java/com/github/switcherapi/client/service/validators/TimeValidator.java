package com.github.switcherapi.client.service.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.github.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.EntryOperation;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.utils.SwitcherUtils;

@ValidatorComponent(type = StrategyValidator.TIME)
public class TimeValidator extends Validator {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Override
	public boolean process(Strategy strategy, Entry switcherInput) throws SwitcherInvalidOperationException,
			SwitcherInvalidTimeFormat, SwitcherInvalidOperationInputException {

		try {
			final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			final String today = format.format(new Date());
			return selectTimeOperationCase(strategy, switcherInput, today);
		} catch (ParseException e) {
			logger.error(e);
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
			stgDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

			return inputDate.before(stgDate);
		case GREATER:
			stgDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

			return inputDate.after(stgDate);
		case BETWEEN:
			if (strategy.getValues().length == 2) {
				stgDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
				stgDate2 = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[1]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, switcherInput.getInput()),
						DATE_FORMAT);

				return inputDate.after(stgDate) && inputDate.before(stgDate2);
			}

			throw new SwitcherInvalidOperationInputException(EntryOperation.BETWEEN.name());
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

}
