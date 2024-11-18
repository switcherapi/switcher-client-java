package com.github.switcherapi.client.service.validators;

import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.criteria.Strategy;

import java.util.Arrays;

public class ValueValidator extends Validator {

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.VALUE;
	}

	@Override
	public boolean process(Strategy strategy, Entry switcherInput) {
		switch (strategy.getEntryOperation()) {
		case EXIST:
			return Arrays.stream(strategy.getValues()).anyMatch(val -> val.equals(switcherInput.getInput()));
		case NOT_EXIST:
			return Arrays.stream(strategy.getValues()).noneMatch(val -> val.equals(switcherInput.getInput()));
		case EQUAL:
			return strategy.getValues().length == 1 && strategy.getValues()[0].equals(switcherInput.getInput());
		case NOT_EQUAL:
			return strategy.getValues().length == 1 && !strategy.getValues()[0].equals(switcherInput.getInput());
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

}
