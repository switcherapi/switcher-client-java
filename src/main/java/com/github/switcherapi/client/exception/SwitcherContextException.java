package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherContextException extends SwitcherException {
	
	public SwitcherContextException(String error) {
		super(String.format("Context has errors - %s", error), null);
	} 

}
