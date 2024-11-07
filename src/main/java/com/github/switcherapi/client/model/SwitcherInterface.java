package com.github.switcherapi.client.model;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.response.CriteriaResponse;

import java.util.List;
import java.util.Set;

/**
 * Defines minimal contract for Switcher implementations.
 */
public interface SwitcherInterface {

	/**
	 * This method builds the Switcher object.<br>
	 * Uses to isolate Switcher creation from the execution.<br>
	 *
	 * For example:
	 * <pre>
	 * Switcher switcher = SwitcherContext
	 * 	.getSwitcher(MY_SWITCHER)
	 * 	.remote(true)
	 * 	.throttle(1000)
	 * 	.checkValue("value")
	 * 	.build();
	 * </pre>
	 *
	 * @return {@link Switcher}
	 */
	Switcher build();

	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 *
	 * @param entry input object
	 * @return {@link Switcher}
	 */
	Switcher prepareEntry(final List<Entry> entry);

	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 *
	 * @param entry input object
	 * @param add if false, the list will be cleaned and the entry provided will be the only input for this Switcher.
	 * @return {@link Switcher}
	 */
	Switcher prepareEntry(final Entry entry, final boolean add);

	/**
	 * It adds an input to the list of inputs.
	 * <br>Under the table it calls {@link #prepareEntry(Entry, boolean)} passing true to the second argument.
	 *
	 * @param entry input object
	 * @return {@link Switcher}
	 */
	Switcher prepareEntry(final Entry entry);

	/**
	 * Execute criteria based on a given switcher key provided via {@link SwitcherContext#getSwitcher(String)}.
	 * <br>The detailed result is available in list of {@link CriteriaResponse}.
	 *
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	boolean isItOn() throws SwitcherException;

	/**
	 * Execute criteria based on a given switcher key provided via {@link SwitcherContext#getSwitcher(String)}.
	 * <br>The detailed result is available in list of {@link CriteriaResponse}.
	 *
	 * @return {@link CriteriaResponse}
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	CriteriaResponse submit() throws SwitcherException;

	/**
	 * Execute the criteria evaluation.
	 *
	 * @return the criteria response
	 */
	CriteriaResponse executeCriteria();

	/**
	 * Get the history of executions.
	 *
	 * @return the history of executions
	 */
	Set<CriteriaResponse> getHistoryExecution();

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
	 * Get the delay for the next execution when using async call
	 *
	 * @return the delay for the next execution
	 */
	long getDelay();
}
