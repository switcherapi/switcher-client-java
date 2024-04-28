package com.github.switcherapi.client.model;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class that simplifies how input are programmatically wrapped inside the Switcher.
 * It also allows chained calls that makes the code clear.
 * 
 * @author Roger Floriano (petruki)
 */
public abstract class SwitcherBuilder {
	
	protected long delay;

	protected boolean remote;

	protected boolean bypassMetrics;
	
	protected List<Entry> entry;
	
	protected SwitcherBuilder() {
		this.entry = new ArrayList<>();
		this.delay = 0;
	}
	
	/**
	 * Skip API calls given a delay time
	 * 
	 * @param delay time in milliseconds for the next call
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder throttle(long delay) {
		this.delay = delay;
		return this;
	}

	/**
	 * Force Switcher to resolve remotely when true
	 *
	 * @param remote true to force remote resolution
	 * @return {@link SwitcherBuilder}
	 *
	 * @throws SwitcherContextException if Switcher is not configured to run locally using local mode
	 */
	public SwitcherBuilder remote(boolean remote) {
		if (!SwitcherContextBase.contextBol(ContextKey.LOCAL_MODE)) {
			throw new SwitcherContextException("Switcher is not configured to run locally");
		}

		this.remote = remote;
		return this;
	}
	
	/**
	 * Add a validation to the entry stack
	 * 
	 * @param strategy validator
	 * @param input to be evaluated
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder check(StrategyValidator strategy, String input) {
		if (StringUtils.isNotBlank(input)) {
			entry.add(Entry.build(strategy, input));
		}
		
		return this;
	}
	
	/**
	 * Plain text validation. No format required.
	 * 
	 * @param input value to be evaluated
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder checkValue(String input) {
		return check(StrategyValidator.VALUE, input);
	}
	
	/**
	 * Numeric type validation. It accepts positive/negative and decimal values.
	 * 
	 * @param input value to be evaluated
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder checkNumeric(String input) {
		return check(StrategyValidator.NUMERIC, input);
	}
	
	/**
	 * This validation accept CIDR (e.g. 10.0.0.0/24) or IPv4 (e.g. 10.0.0.1) formats.
	 * 
	 * @param input value to be evaluated
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder checkNetwork(String input) {
		return check(StrategyValidator.NETWORK, input);
	}
	
	/**
	 * Regular expression based validation. No format required.
	 * 
	 * @param input value to be evaluated
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder checkRegex(String input) {
		return check(StrategyValidator.REGEX, input);
	}
	
	/**
	 * This validation accept only HH:mm format input.
	 * 
	 * @param input value to be evaluated
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder checkTime(String input) {
		return check(StrategyValidator.TIME, input);
	}
	
	/**
	 * Date validation accept both date and time input (e.g. YYYY-MM-DD or YYYY-MM-DDTHH:mm) formats.
	 * 
	 * @param input value to be evaluated
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder checkDate(String input) {
		return check(StrategyValidator.DATE, input);
	}
	
	/**
	 * Validates JSON keys from a given payload
	 * 
	 * @param input JSON payload
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder checkPayload(String input) {
		return check(StrategyValidator.PAYLOAD, input);
	}

	/**
	 * Force Switcher to bypass metrics
	 *
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder bypassMetrics() {
		this.bypassMetrics = true;
		return this;
	}

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
	 * Execute criteria based on a given switcher key provided via {@link SwitcherContext#getSwitcher(String)}.
	 * <br>The detailed result is available in list of {@link CriteriaResponse}.
	 * 
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public abstract boolean isItOn() throws SwitcherException;

	/**
	 * Execute criteria based on a given switcher key provided via {@link SwitcherContext#getSwitcher(String)}.
	 * <br>The detailed result is available in list of {@link CriteriaResponse}.
	 *
	 * @return {@link CriteriaResponse}
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public abstract CriteriaResponse submit() throws SwitcherException;

	public boolean isRemote() {
		return remote;
	}
}
