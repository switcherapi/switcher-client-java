package com.switcherapi.client.service.validators;

import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.EntryOperation;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.StrategyConfig;
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
	public boolean process(final StrategyConfig strategyConfig, final Entry switcherInput) throws SwitcherInvalidOperationException,
			SwitcherInvalidTimeFormat, SwitcherInvalidOperationInputException {

		try {
			return selectDateOperationCase(strategyConfig, switcherInput);
		} catch (ParseException e) {
			throw new SwitcherInvalidTimeFormat(strategyConfig.getStrategy(), e);
		}
	}

	private boolean selectDateOperationCase(final StrategyConfig strategyConfig, final Entry switcherInput) throws ParseException {
		Date stgDate;
		Date stgDate2;
		Date inputDate;

		switch (strategyConfig.getEntryOperation()) {
		case LOWER:
			stgDate = DateUtils.parseDate(getFullDate(strategyConfig.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(getFullDate(switcherInput.getInput()), DATE_FORMAT);

			return inputDate.before(stgDate);
		case GREATER:
			stgDate = DateUtils.parseDate(getFullDate(strategyConfig.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(getFullDate(switcherInput.getInput()), DATE_FORMAT);

			return inputDate.after(stgDate);
		case BETWEEN:
			if (strategyConfig.getValues().length == 2) {
				stgDate = DateUtils.parseDate(getFullDate(strategyConfig.getValues()[0]), DATE_FORMAT);
				stgDate2 = DateUtils.parseDate(getFullDate(strategyConfig.getValues()[1]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(getFullDate(switcherInput.getInput()), DATE_FORMAT);

				return inputDate.after(stgDate) && inputDate.before(stgDate2);
			}

			throw new SwitcherInvalidOperationInputException(EntryOperation.BETWEEN.name());
		default:
			throw new SwitcherInvalidOperationException(strategyConfig.getOperation(), strategyConfig.getStrategy());
		}
	}

}
