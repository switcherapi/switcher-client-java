package com.github.switcherapi.client.service.validators;

import java.util.Arrays;

import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;

@ValidatorComponent(type = Entry.REGEX)
public class RegexValidator extends Validator {

	private static final String DELIMITER_REGEX = "\\b%s\\b";

	@Override
	public boolean process(Strategy strategy, Entry switcherInput) throws SwitcherInvalidOperationException {
		switch (strategy.getOperation()) {
		case Entry.EXIST:
			return Arrays.stream(strategy.getValues()).anyMatch(val -> switcherInput.getInput().matches(val));
		case Entry.NOT_EXIST:
			return Arrays.stream(strategy.getValues()).noneMatch(val -> switcherInput.getInput().matches(val));
		case Entry.EQUAL:
			return strategy.getValues().length == 1
					&& switcherInput.getInput().matches(String.format(DELIMITER_REGEX, strategy.getValues()[0]));
		case Entry.NOT_EQUAL:
			return strategy.getValues().length == 1
					&& !switcherInput.getInput().matches(String.format(DELIMITER_REGEX, strategy.getValues()[0]));
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

}
