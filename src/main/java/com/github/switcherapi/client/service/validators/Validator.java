package com.github.switcherapi.client.service.validators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;

public abstract class Validator {
	
	protected static final Logger logger = LogManager.getLogger(Validator.class);
	
	public static final String DEBUG_SWITCHER_INPUT = "switcherInput: %s";
	public static final String DEBUG_STRATEGY = "strategy: %s";
	
	public boolean execute(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(DEBUG_STRATEGY, strategy));
			logger.debug(String.format(DEBUG_SWITCHER_INPUT, switcherInput));
		}
		
		return process(strategy, switcherInput);
	}
	
	public abstract boolean process(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherException;

}
