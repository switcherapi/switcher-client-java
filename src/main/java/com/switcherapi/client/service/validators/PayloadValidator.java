package com.switcherapi.client.service.validators;

import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.StrategyConfig;
import com.switcherapi.client.utils.SwitcherUtils;

import java.util.Arrays;
import java.util.Set;

public class PayloadValidator extends Validator {

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.PAYLOAD;
	}

	@Override
	public boolean process(StrategyConfig strategyConfig, Entry switcherInput) {
		switch (strategyConfig.getEntryOperation()) {
		case HAS_ONE:
			return hasOne(strategyConfig, switcherInput);
		case HAS_ALL:
			return hasAll(strategyConfig, switcherInput);
		default:
			throw new SwitcherInvalidOperationException(strategyConfig.getOperation(), strategyConfig.getStrategy());
		}
	}
	
	private boolean hasOne(StrategyConfig strategyConfig, Entry switcherInput) {
		try {
			final Set<String> keySet = SwitcherUtils.payloadReader(switcherInput.getInput(), null);
			return Arrays.stream(strategyConfig.getValues())
				.anyMatch(keySet::contains);
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean hasAll(StrategyConfig strategyConfig, Entry switcherInput) {
		try {
			final Set<String> keySet = SwitcherUtils.payloadReader(switcherInput.getInput(), null);
			return Arrays.stream(strategyConfig.getValues())
				.allMatch(keySet::contains);
		} catch (Exception e) {
			return false;
		}
	}
	
}
