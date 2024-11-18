package com.github.switcherapi.client.validator;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.service.validators.Validator;

public class CustomValidator extends Validator {

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.INVALID;
	}

	@Override
	public boolean process(Strategy strategy, Entry switcherInput)
		throws SwitcherException {
		return true;
	}
	
}