package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherContextException extends SwitcherException {
	
	private static final long serialVersionUID = -6340224967205872873L;
	
	public SwitcherContextException(String error) {
		
		super(String.format("Context has errors - %s", error), null);
	} 

}
