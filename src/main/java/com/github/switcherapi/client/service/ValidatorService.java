package com.github.switcherapi.client.service;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidStrategyException;
import com.github.switcherapi.client.exception.SwitcherInvalidValidatorException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.service.validators.*;

import java.util.EnumMap;

public class ValidatorService {
	
	private final EnumMap<StrategyValidator, Validator> validators;
	
	public ValidatorService() {
		this.validators = new EnumMap<>(StrategyValidator.class);
		this.initializeValidators();
	}
	
	private void initializeValidators() {
		registerValidator(DateValidator.class);
		registerValidator(NetworkValidator.class);
		registerValidator(NumericValidator.class);
		registerValidator(PayloadValidator.class);
		registerValidator(TimeValidator.class);
		registerValidator(ValueValidator.class);
		registerValidator(RegexValidatorV8.getPlatformValidator());
	}

	private StrategyValidator getStrategyValidator(Class<? extends Validator> validatorClass) {
		if (!validatorClass.isAnnotationPresent(ValidatorComponent.class)) {
			throw new SwitcherInvalidValidatorException(validatorClass.getName());
		}

		return validatorClass.getAnnotation(ValidatorComponent.class).type();
	}

	public void registerValidator(Class<? extends Validator> validatorClass) {
		try {
			validators.put(getStrategyValidator(validatorClass), validatorClass.getConstructor().newInstance());
		} catch (SwitcherInvalidValidatorException e) {
			throw e;
		} catch (Exception e) {
			throw new SwitcherException(e.getMessage(), e);
		}
	}
	
	public boolean execute(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherInvalidStrategyException {
		if (!validators.containsKey(strategy.getStrategyValidator())) {
			throw new SwitcherInvalidStrategyException(strategy.getStrategy());
		}

		return validators.get(strategy.getStrategyValidator()).execute(strategy, switcherInput);
	}

}
