package com.github.switcherapi.client.service.validators;

import com.github.switcherapi.client.utils.SwitcherUtils;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Validator {
	
	protected static final Logger logger = LoggerFactory.getLogger(Validator.class);
	
	public static final String DEBUG_SWITCHER_INPUT = "switcherInput: {}";
	public static final String DEBUG_STRATEGY = "strategy: {}";
	
	public boolean execute(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherException {
		SwitcherUtils.debug(logger, DEBUG_STRATEGY, strategy);
		SwitcherUtils.debug(logger, DEBUG_SWITCHER_INPUT, switcherInput);
		return process(strategy, switcherInput);
	}
	
	public abstract boolean process(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherException;

}
