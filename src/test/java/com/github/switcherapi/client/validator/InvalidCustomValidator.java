package com.github.switcherapi.client.validator;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.service.validators.Validator;
import com.github.switcherapi.client.service.validators.ValidatorComponent;

@ValidatorComponent(type = "CUSTOM")
public class InvalidCustomValidator extends Validator {
	
	private InvalidCustomValidator() {}

	@Override
	public boolean process(Strategy strategy, Entry switcherInput)
		throws SwitcherException {
		return true;
	}
	
}