package com.github.switcherapi.client.service.validators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;

public abstract class Validator {
	
	protected static final Logger logger = LogManager.getLogger(Validator.class);
	
	public static final String DEBUG_SWITCHER_INPUT = "switcherInput: {}";
	public static final String DEBUG_STRATEGY = "strategy: {}";
	
	public boolean execute(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherException {
		logger.debug(DEBUG_STRATEGY, strategy);
		logger.debug(DEBUG_SWITCHER_INPUT, switcherInput);
		return process(strategy, switcherInput);
	}
	
	public abstract boolean process(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherException;

}
