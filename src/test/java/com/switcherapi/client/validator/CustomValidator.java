package com.switcherapi.client.validator;

import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.Strategy;
import com.switcherapi.client.service.validators.Validator;

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