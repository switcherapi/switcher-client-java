package com.github.switcherapi.client.validator;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.service.validators.Validator;

public class InvalidCustom2Validator extends Validator {

	private InvalidCustom2Validator() {}

	@Override
	public boolean process(Strategy strategy, Entry switcherInput)
		throws SwitcherException {
		return true;
	}
	
}