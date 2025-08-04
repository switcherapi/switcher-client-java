package com.switcherapi.client.service;

import com.switcherapi.client.exception.SwitcherInvalidStrategyException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.Strategy;
import com.switcherapi.client.service.validators.*;

import java.util.EnumMap;

public class ValidatorService implements SwitcherValidator {
	
	private final EnumMap<StrategyValidator, Validator> validators;
	
	public ValidatorService() {
		this.validators = new EnumMap<>(StrategyValidator.class);
		this.initializeValidators();
	}
	
	private void initializeValidators() {
		registerValidator(new DateValidator());
		registerValidator(new NetworkValidator());
		registerValidator(new NumericValidator());
		registerValidator(new PayloadValidator());
		registerValidator(new TimeValidator());
		registerValidator(new ValueValidator());
		registerValidator(new RegexValidator());
	}

	@Override
	public void registerValidator(Validator validator) {
		validators.put(validator.getType(), validator);
	}

	@Override
	public boolean execute(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherInvalidStrategyException {
		if (!validators.containsKey(strategy.getStrategyValidator())) {
			throw new SwitcherInvalidStrategyException(strategy.getStrategy());
		}

		return validators.get(strategy.getStrategyValidator()).execute(strategy, switcherInput);
	}

}
