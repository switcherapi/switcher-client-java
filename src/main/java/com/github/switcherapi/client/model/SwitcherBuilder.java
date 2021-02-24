package com.github.switcherapi.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade builder that simplifies how input are programatically wrapped inside the Switcher.
 * It also allows chained calls that make the code clear.
 * 
 * @author Roger Floriano (petruki)
 */
public class SwitcherBuilder {
	
	protected List<Entry> entry;
	
	private void init() {
		if (entry == null) {
			entry = new ArrayList<>();
		}
	}
	
	/**
	 * Add a validation to the entry stack
	 * 
	 * @param validator name
	 * @param input to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder check(String validator, String input) {
		init();
		entry.add(new Entry(validator, input));
		return this;
	}
	
	/**
	 * Plain text validation. No format required.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkValue(String input) {
		return check(Entry.VALUE, input);
	}
	
	/**
	 * Numeric type validation. It accepts positive/negative and decimal values.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkNumeric(String input) {
		return check(Entry.NUMERIC, input);
	}
	
	/**
	 * This validation accept CIDR (e.g. 10.0.0.0/24) or IPv4 (e.g. 10.0.0.1) formats.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkNetwork(String input) {
		return check(Entry.NETWORK, input);
	}
	
	/**
	 * Regular expression based validation. No format required.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkRegex(String input) {
		return check(Entry.REGEX, input);
	}
	
	/**
	 * This validation accept only HH:mm format input.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkTime(String input) {
		return check(Entry.TIME, input);
	}
	
	/**
	 * Date validation accept both date and time input (e.g. YYYY-MM-DD or YYYY-MM-DDTHH:mm) formats.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkDate(String input) {
		return check(Entry.DATE, input);
	}

}
