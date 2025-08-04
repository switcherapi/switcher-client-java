package com.switcherapi.client.validator;

import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.StrategyConfig;
import com.switcherapi.client.service.validators.Validator;

public class CustomValidator extends Validator {

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.INVALID;
	}

	@Override
	public boolean process(StrategyConfig strategyConfig, Entry switcherInput)
		throws SwitcherException {
		return true;
	}
	
}