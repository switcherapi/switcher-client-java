package com.switcherapi.client.service.validators;

import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.EntryOperation;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.StrategyConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateValidator extends DateTimeValidator {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.DATE;
	}

	@Override
	public boolean process(final StrategyConfig strategyConfig, final Entry switcherInput) throws SwitcherInvalidOperationException,
			SwitcherInvalidTimeFormat, SwitcherInvalidOperationInputException {

		try {
			return selectDateOperationCase(strategyConfig, switcherInput);
		} catch (DateTimeParseException e) {
			throw new SwitcherInvalidTimeFormat(strategyConfig.getStrategy(), e);
		}
	}

	private boolean selectDateOperationCase(final StrategyConfig strategyConfig, final Entry switcherInput) {
		LocalDateTime stgDate;
		LocalDateTime stgDate2;
		LocalDateTime inputDate;

		switch (strategyConfig.getEntryOperation()) {
		case LOWER:
			stgDate = LocalDateTime.parse(getFullDate(strategyConfig.getValues()[0]), FORMATTER);
			inputDate = LocalDateTime.parse(getFullDate(switcherInput.getInput()), FORMATTER);

			return inputDate.isBefore(stgDate);
		case GREATER:
			stgDate = LocalDateTime.parse(getFullDate(strategyConfig.getValues()[0]), FORMATTER);
			inputDate = LocalDateTime.parse(getFullDate(switcherInput.getInput()), FORMATTER);

			return inputDate.isAfter(stgDate);
		case BETWEEN:
			if (strategyConfig.getValues().length == 2) {
				stgDate = LocalDateTime.parse(getFullDate(strategyConfig.getValues()[0]), FORMATTER);
				stgDate2 = LocalDateTime.parse(getFullDate(strategyConfig.getValues()[1]), FORMATTER);
				inputDate = LocalDateTime.parse(getFullDate(switcherInput.getInput()), FORMATTER);

				return inputDate.isAfter(stgDate) && inputDate.isBefore(stgDate2);
			}

			throw new SwitcherInvalidOperationInputException(EntryOperation.BETWEEN.name());
		default:
			throw new SwitcherInvalidOperationException(strategyConfig.getOperation(), strategyConfig.getStrategy());
		}
	}

}
