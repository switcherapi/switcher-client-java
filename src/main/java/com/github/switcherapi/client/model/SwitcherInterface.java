package com.github.switcherapi.client.model;

import com.github.switcherapi.client.model.response.CriteriaResponse;

import java.util.List;
import java.util.Set;

/**
 * Defines minimal contract for Switcher implementations.
 */
public interface SwitcherInterface {

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
