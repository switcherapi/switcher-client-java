package com.switcherapi.client.service.validators;

import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.StrategyConfig;

import java.util.Arrays;

public class RegexValidator extends Validator {

	private static final String DELIMITER_REGEX = "\\b%s\\b";

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.REGEX;
	}

	@Override
	public boolean process(StrategyConfig strategyConfig, Entry switcherInput) throws SwitcherInvalidOperationException {
		switch (strategyConfig.getEntryOperation()) {
			case EXIST:
				return Arrays.stream(strategyConfig.getValues()).anyMatch(val -> switcherInput.getInput().matches(val));
			case NOT_EXIST:
				return Arrays.stream(strategyConfig.getValues()).noneMatch(val -> switcherInput.getInput().matches(val));
			case EQUAL:
				return strategyConfig.getValues().length == 1
						&& switcherInput.getInput().matches(String.format(DELIMITER_REGEX, strategyConfig.getValues()[0]));
			case NOT_EQUAL:
				return strategyConfig.getValues().length == 1
						&& !switcherInput.getInput().matches(String.format(DELIMITER_REGEX, strategyConfig.getValues()[0]));
			default:
				throw new SwitcherInvalidOperationException(strategyConfig.getOperation(), strategyConfig.getStrategy());
		}
	}

}