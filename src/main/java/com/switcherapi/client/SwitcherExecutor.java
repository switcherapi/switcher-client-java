package com.switcherapi.client;

import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.client.model.SwitcherResult;
import com.switcherapi.client.model.criteria.Domain;

import java.util.Set;

/**
 * An Executor provides the API to handle Remote and Local operations that
 * should be available for both Services implementations.
 * 
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public interface SwitcherExecutor {

	/**
	 * Execute criteria based on the Switcher configuration
	 * 
	 * @param switcher to be evaluated
	 * @return Criteria response containing the evaluation details
	 */
	SwitcherResult executeCriteria(final SwitcherRequest switcher);
	
	/**
	 * Check the snapshot versions against the Remote configuration.
	 * 
	 * @return True if snapshot is up-to-date
	 */
	boolean checkSnapshotVersion();
	
	/**
	 * Retrieve updated snapshot from the remote API
	 */
	void updateSnapshot();
	
	/**
	 * Check set of Switchers if they are properly configured.
	 * 
	 * @param switchers To be validated
	 */
	void checkSwitchers(final Set<String> switchers);

	/**
	 * Retrieve local snapshot version
	 *
	 * @return snapshot version
	 */
	long getSnapshotVersion();

	/**
	 * Retrieve the Domain object from the current snapshot
	 *
	 * @return Domain object
	 */
	Domain getDomain();

	/**
	 * Set the Domain object from the current snapshot
	 *
	 * @param domain to be set
	 */
	void setDomain(Domain domain);

	/**
	 * Retrieve the Switcher properties configured for the executor
	 *
	 * @return SwitcherProperties object
	 */
	SwitcherProperties getSwitcherProperties();

}
