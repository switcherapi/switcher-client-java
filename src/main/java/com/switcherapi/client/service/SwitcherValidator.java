package com.switcherapi.client.service;

import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.criteria.Strategy;
import com.switcherapi.client.service.validators.Validator;

/**
 * @author Roger Floriano (petruki)
 * @since 2024-11-10
 */
public interface SwitcherValidator {

	/**
	 * Register a validator to be used during the criteria validation
	 *
	 * @param validator Validator to be registered
	 */
	void registerValidator(Validator validator);

	/**
	 * Execute the criteria validation based on the configuration tree. It starts
	 * validating from the top of the node (Domain) ascending to the lower level
	 * (Strategy)
	 *
	 * @param strategy       Configuration switcher to be validated
	 * @param switcherInput  Input to be validated
	 * @return The criteria result
	 */
	boolean execute(final Strategy strategy, final Entry switcherInput);

}
