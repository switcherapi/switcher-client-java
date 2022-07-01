package com.github.switcherapi.client.model;

import java.util.ArrayList;
import java.util.List;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.response.CriteriaResponse;

/**
 * Builder class that simplifies how input are programmatically wrapped inside the Switcher.
 * It also allows chained calls that makes the code clear.
 * 
 * @author Roger Floriano (petruki)
 */
public abstract class SwitcherBuilder {
	
	protected long delay = 0;
	
	protected List<Entry> entry;
	
	protected SwitcherBuilder() {
		entry = new ArrayList<>();
	}
	
	/**
	 * Skip API calls given a delay time
	 * 
	 * @param delay time in milliseconds for the next call
	 * @return switcher itself
	 */
	public SwitcherBuilder throttle(long delay) {
		this.delay = delay;
		return this;
	}
	
	/**
	 * Add a validation to the entry stack
	 * 
	 * @param strategy validator
	 * @param input to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder check(StrategyValidator strategy, String input) {
		entry.add(Entry.build(strategy, input));
		return this;
	}
	
	/**
	 * Plain text validation. No format required.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkValue(String input) {
		return check(StrategyValidator.VALUE, input);
	}
	
	/**
	 * Numeric type validation. It accepts positive/negative and decimal values.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkNumeric(String input) {
		return check(StrategyValidator.NUMERIC, input);
	}
	
	/**
	 * This validation accept CIDR (e.g. 10.0.0.0/24) or IPv4 (e.g. 10.0.0.1) formats.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkNetwork(String input) {
		return check(StrategyValidator.NETWORK, input);
	}
	
	/**
	 * Regular expression based validation. No format required.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkRegex(String input) {
		return check(StrategyValidator.REGEX, input);
	}
	
	/**
	 * This validation accept only HH:mm format input.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkTime(String input) {
		return check(StrategyValidator.TIME, input);
	}
	
	/**
	 * Date validation accept both date and time input (e.g. YYYY-MM-DD or YYYY-MM-DDTHH:mm) formats.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkDate(String input) {
		return check(StrategyValidator.DATE, input);
	}
	
	/**
	 * Validates JSON keys from a given payload
	 * 
	 * @param input JSON payload
	 * @return switcher itself
	 */
	public SwitcherBuilder checkPayload(String input) {
		return check(StrategyValidator.PAYLOAD, input);
	}
	
	/**
	 * Creates a Switcher given the configuration assigned via {@link SwitcherBuilder}
	 * 
	 * @return {@link Switcher}
	 */
	public abstract Switcher build();
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 * 
	 * @param entry input object
	 * @return {@link Switcher}
	 */
	public abstract Switcher prepareEntry(final List<Entry> entry);
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 * 
	 * @param entry input object
	 * @param add if false, the list will be cleaned and the entry provided will be the only input for this Switcher.
	 * @return {@link Switcher}
	 */
	public abstract Switcher prepareEntry(final Entry entry, final boolean add);
	
	/**
	 * It adds an input to the list of inputs.
	 * <br>Under the table it calls {@link #prepareEntry(Entry, boolean)} passing true to the second argument.
	 * 
	 * @param entry input object
	 * @return {@link Switcher}
	 */
	public abstract Switcher prepareEntry(final Entry entry);
	
	/**
	 * Convenient method to send all the information necessary to run the criteria.
	 * 
	 * @param entry input object
	 * @param add if false, the list will be cleaned and the entry provided will be the only input for this Switcher.
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public abstract boolean isItOn(final Entry entry, final boolean add) 
			throws SwitcherException;
	
	/**
	 * This method is going to invoke the criteria overwriting the existing input if it was added earlier.
	 * 
	 * @param entry input object
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public abstract boolean isItOn(final List<Entry> entry) throws SwitcherException;
	
	/**
	 * Execute criteria based on a given switcher key provided via {@link SwitcherContext#getSwitcher(String)}.
	 * <br>The detailed result is available in list of {@link CriteriaResponse}.
	 * 
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public abstract boolean isItOn() throws SwitcherException;

}
