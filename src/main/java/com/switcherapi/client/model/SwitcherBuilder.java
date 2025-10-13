package com.switcherapi.client.model;

import com.switcherapi.client.SwitcherProperties;
import com.switcherapi.client.exception.SwitcherContextException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Builder class that simplifies how input are programmatically wrapped inside the Switcher.
 * It also allows chained calls that makes the code clear.
 * 
 * @author Roger Floriano (petruki)
 */
public abstract class SwitcherBuilder implements Switcher {

	protected final SwitcherProperties properties;

	protected final Map<List<Entry>, SwitcherResult> historyExecution;
	
	protected long delay;

	protected boolean remote;

	protected boolean bypassMetrics;

	protected Boolean restrictRelay;

	protected String defaultResult;
	
	protected List<Entry> entry;

	protected SwitcherBuilder(final SwitcherProperties properties) {
		this.properties = properties;
		this.historyExecution = new HashMap<>();
		this.entry = new ArrayList<>();
		this.delay = 0;
	}

	/**
	 * Clear all entries previously added and history of executions.
	 *
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder flush() {
		this.historyExecution.clear();
		this.entry.clear();
		return this;
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
		if (!this.properties.getBoolean(ContextKey.LOCAL_MODE)) {
			throw new SwitcherContextException("Switcher is not configured to run locally");
		}

		this.remote = remote;
		return this;
	}

	/**
	 * Set the default result when client panics
	 *
	 * @param defaultResult true/false
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder defaultResult(boolean defaultResult) {
		this.defaultResult = String.valueOf(defaultResult);
		return this;
	}

	/**
	 * Allow local snapshots to ignore or require Relay verification.
	 *
	 * @param restrictRelay true to restrict Relay verification
	 * @return {@link SwitcherBuilder}
	 */
	public SwitcherBuilder restrictRelay(boolean restrictRelay) {
		this.restrictRelay = restrictRelay;
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
			entry.removeIf(e -> e.getStrategy().equals(strategy.toString()));
			entry.add(Entry.of(strategy, input));
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

	public boolean isRemote() {
		return remote;
	}

	public boolean isRelayRestricted() {
		return restrictRelay;
	}

	public boolean isRestrictRelaySet() {
		return Objects.nonNull(restrictRelay);
	}

	public String getDefaultResult() {
		return defaultResult;
	}

}
