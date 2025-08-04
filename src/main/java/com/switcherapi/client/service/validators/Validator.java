package com.switcherapi.client.service.validators;

import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.utils.SwitcherUtils;
import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.criteria.StrategyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Validator {
	
	protected static final Logger logger = LoggerFactory.getLogger(Validator.class);
	
	public static final String DEBUG_SWITCHER_INPUT = "switcherInput: {}";
	public static final String DEBUG_STRATEGY = "strategy: {}";
	
	public boolean execute(final StrategyConfig strategyConfig, final Entry switcherInput)
			throws SwitcherException {
		SwitcherUtils.debug(logger, DEBUG_STRATEGY, strategyConfig);
		SwitcherUtils.debug(logger, DEBUG_SWITCHER_INPUT, switcherInput);
		return process(strategyConfig, switcherInput);
	}
	
	public abstract boolean process(final StrategyConfig strategyConfig, final Entry switcherInput)
			throws SwitcherException;

	public abstract StrategyValidator getType();

}
