package com.github.switcherapi.client.service;

import java.util.HashMap;
import java.util.Map;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidStrategyException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.service.validators.Validator;
import com.github.switcherapi.client.service.validators.ValidatorComponent;
import com.github.switcherapi.client.utils.SwitcherClassLoader;

public class ValidatorService {
	
	private final Map<String, Validator> validators;
	
	public ValidatorService() {
		this.validators = new HashMap<>();
		this.initializeValidators();
	}
	
	private void initializeValidators() {
		final SwitcherClassLoader<Validator> classLoader = new SwitcherClassLoader<>();
		classLoader.findClassesByType(Validator.class).forEach(this::registerValidator);
	}

	public void registerValidator(final Class<? extends Validator> validatorClass) {
		if (validatorClass.isAnnotationPresent(ValidatorComponent.class)) {
			try {
				validators.put(validatorClass.getAnnotation(
						ValidatorComponent.class).type(), validatorClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SwitcherException(e.getMessage(), e);
			}
		}
	}
	
	public boolean execute(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherInvalidStrategyException {
		if (validators.containsKey(strategy.getStrategy()))
			return validators.get(strategy.getStrategy()).execute(strategy, switcherInput);

		throw new SwitcherInvalidStrategyException(strategy.getStrategy());
	}

}
