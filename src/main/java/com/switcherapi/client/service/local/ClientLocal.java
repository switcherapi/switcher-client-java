package com.switcherapi.client.service.local;

import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.client.model.criteria.Domain;
import com.switcherapi.client.model.SwitcherResult;

import java.util.List;
import java.util.Set;

/**
 * @author Roger Floriano (petruki)
 * @since 2024-11-10
 */
public interface ClientLocal {


	/**
	 * Check if the switchers are valid and if they are properly annotated with @SwitcherKey
	 *
	 * @param switchers List of switchers to be checked
	 * @param domain   Top level of the configuration tree
	 * @return List of invalid switchers
	 */
	List<String> checkSwitchers(final Set<String> switchers, final Domain domain);

	/**
	 * Execute the criteria validation based on the configuration tree. It starts
	 * validating from the top of the node (Domain) ascending to the lower level
	 * (Strategy)
	 *
	 * @param switcher Configuration switcher to be validated
	 * @param domain   Top level of the configuration tree
	 * @return The criteria result
	 * @throws SwitcherException If encountered either invalid input or misconfiguration
	 */
	SwitcherResult executeCriteria(final SwitcherRequest switcher, final Domain domain);

}
