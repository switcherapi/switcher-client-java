package com.switcherapi.client.service.validators;

import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.StrategyConfig;

import java.util.Arrays;

public class ValueValidator extends Validator {

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.VALUE;
	}

	@Override
	public boolean process(StrategyConfig strategyConfig, Entry switcherInput) {
		switch (strategyConfig.getEntryOperation()) {
		case EXIST:
			return Arrays.stream(strategyConfig.getValues()).anyMatch(val -> val.equals(switcherInput.getInput()));
		case NOT_EXIST:
			return Arrays.stream(strategyConfig.getValues()).noneMatch(val -> val.equals(switcherInput.getInput()));
		case EQUAL:
			return strategyConfig.getValues().length == 1 && strategyConfig.getValues()[0].equals(switcherInput.getInput());
		case NOT_EQUAL:
			return strategyConfig.getValues().length == 1 && !strategyConfig.getValues()[0].equals(switcherInput.getInput());
		default:
			throw new SwitcherInvalidOperationException(strategyConfig.getOperation(), strategyConfig.getStrategy());
		}
	}

}
