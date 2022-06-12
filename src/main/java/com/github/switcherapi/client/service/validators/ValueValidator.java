package com.github.switcherapi.client.service.validators;

import java.util.Arrays;

import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;

@ValidatorComponent(type = Entry.VALUE)
public class ValueValidator extends Validator {

	@Override
	public boolean process(Strategy strategy, Entry switcherInput) {
		switch (strategy.getOperation()) {
		case Entry.EXIST:
			return Arrays.stream(strategy.getValues()).anyMatch(val -> val.equals(switcherInput.getInput()));
		case Entry.NOT_EXIST:
			return Arrays.stream(strategy.getValues()).noneMatch(val -> val.equals(switcherInput.getInput()));
		case Entry.EQUAL:
			return strategy.getValues().length == 1 && strategy.getValues()[0].equals(switcherInput.getInput());
		case Entry.NOT_EQUAL:
			return strategy.getValues().length == 1 && !strategy.getValues()[0].equals(switcherInput.getInput());
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

}
