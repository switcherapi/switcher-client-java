package com.github.switcherapi.client.service.validators;

import java.util.Arrays;
import java.util.Set;

import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.utils.SwitcherUtils;

@ValidatorComponent(type = StrategyValidator.PAYLOAD)
public class PayloadValidator extends Validator {

	@Override
	public boolean process(Strategy strategy, Entry switcherInput) {
		switch (strategy.getEntryOperation()) {
		case HAS_ONE:
			return hasOne(strategy, switcherInput);
		case HAS_ALL:
			return hasAll(strategy, switcherInput);
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}
	
	private boolean hasOne(Strategy strategy, Entry switcherInput) {
		try {
			final Set<String> keySet = SwitcherUtils.payloadReader(switcherInput.getInput(), null);
			return Arrays.stream(strategy.getValues())
				.anyMatch(keySet::contains);
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean hasAll(Strategy strategy, Entry switcherInput) {
		try {
			final Set<String> keySet = SwitcherUtils.payloadReader(switcherInput.getInput(), null);
			return Arrays.stream(strategy.getValues())
				.allMatch(keySet::contains);
		} catch (Exception e) {
			return false;
		}
	}
	
}
