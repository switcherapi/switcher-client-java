package com.github.switcherapi.client.model;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherException;

import java.util.List;

/**
 * The API that handles the switcher operations.
 *
 * <ul>
 *     <li>Switcher creation</li>
 *     <li>Switcher execution</li>
 *     <li>Switcher get input/output</li>
 * </ul>
 *
 *  For example:
 *  <pre>
 *  Switcher switcher = SwitcherContext
 *  	.getSwitcher(MY_SWITCHER)
 *  	.remote(true)
 *  	.throttle(1000)
 *  	.checkValue("value");
 * 	</pre>
 */
public interface Switcher {

	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 *
	 * @param entry input object
	 * @return instance of SwitcherInterface
	 */
	Switcher prepareEntry(final List<Entry> entry);

	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 *
	 * @param entry input object
	 * @param add if false, the list will be cleaned and the entry provided will be the only input for this Switcher.
	 * @return instance of SwitcherInterface
	 */
	Switcher prepareEntry(final Entry entry, final boolean add);

	/**
	 * It adds an input to the list of inputs.
	 * <br>Under the table it calls {@link #prepareEntry(Entry, boolean)} passing true to the second argument.
	 *
	 * @param entry input object
	 * @return instance of SwitcherInterface
	 */
	Switcher prepareEntry(final Entry entry);

	/**
	 * Execute criteria based on a given switcher key provided via {@link SwitcherContext#getSwitcher(String)}.
	 * <br>The detailed result is available in list of {@link SwitcherResult}.
	 *
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	boolean isItOn() throws SwitcherException;

	/**
	 * Execute criteria based on a given switcher key provided via {@link SwitcherContext#getSwitcher(String)}.
	 * <br>The detailed result is available in list of {@link SwitcherResult}.
	 *
	 * @return {@link SwitcherResult}
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	SwitcherResult submit() throws SwitcherException;

	/**
	 * Execute the criteria evaluation.
	 *
	 * @return the switcher result
	 */
	SwitcherResult executeCriteria();

	/**
	 * Update the history of executions.
	 *
	 * @param response the response to be updated
	 */
	void updateHistoryExecution(SwitcherResult response);

	/**
	 * Get the key of the switcher.
	 *
	 * @return the key of the switcher
	 */
	String getSwitcherKey();

	/**
	 * Get the entry input list for the switcher.
	 *
	 * @return the entry of the switcher
	 */
	List<Entry> getEntry();

	/**
	 * Get the last execution result of the switcher.
	 *
	 * @return the last SwitcherResult, otherwise null if no executions were made
	 */
	SwitcherResult getLastExecutionResult();

}
