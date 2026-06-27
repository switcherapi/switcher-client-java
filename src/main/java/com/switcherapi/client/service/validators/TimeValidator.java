package com.switcherapi.client.service.validators;

import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.EntryOperation;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.StrategyConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeValidator extends DateTimeValidator {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.TIME;
	}

	@Override
	public boolean process(StrategyConfig strategyConfig, Entry switcherInput) throws SwitcherInvalidOperationException,
			SwitcherInvalidTimeFormat, SwitcherInvalidOperationInputException {

		try {
			final String today = LocalDate.now(ZoneId.systemDefault()).toString();
			return selectTimeOperationCase(strategyConfig, switcherInput, today);
		} catch (DateTimeParseException e) {
			throw new SwitcherInvalidTimeFormat(strategyConfig.getStrategy(), e);
		}

	}

	private boolean selectTimeOperationCase(final StrategyConfig strategyConfig, final Entry switcherInput, final String today) {
		LocalDateTime stgDate;
		LocalDateTime stgDate2;
		LocalDateTime inputDate;

		switch (strategyConfig.getEntryOperation()) {
		case LOWER:
			stgDate = LocalDateTime.parse(getFullTime(today, strategyConfig.getValues()[0]), FORMATTER);
			inputDate = LocalDateTime.parse(getFullTime(today, switcherInput.getInput()), FORMATTER);

			return inputDate.isBefore(stgDate);
		case GREATER:
			stgDate = LocalDateTime.parse(getFullTime(today, strategyConfig.getValues()[0]), FORMATTER);
			inputDate = LocalDateTime.parse(getFullTime(today, switcherInput.getInput()), FORMATTER);

			return inputDate.isAfter(stgDate);
		case BETWEEN:
			if (strategyConfig.getValues().length == 2) {
				stgDate = LocalDateTime.parse(getFullTime(today, strategyConfig.getValues()[0]), FORMATTER);
				stgDate2 = LocalDateTime.parse(getFullTime(today, strategyConfig.getValues()[1]), FORMATTER);
				inputDate = LocalDateTime.parse(getFullTime(today, switcherInput.getInput()), FORMATTER);

				return inputDate.isAfter(stgDate) && inputDate.isBefore(stgDate2);
			}

			throw new SwitcherInvalidOperationInputException(EntryOperation.BETWEEN.name());
		default:
			throw new SwitcherInvalidOperationException(strategyConfig.getOperation(), strategyConfig.getStrategy());
		}
	}

}
