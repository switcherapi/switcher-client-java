package com.github.petruki.switcher.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.petruki.switcher.client.SwitcherFactory;
import com.github.petruki.switcher.client.exception.SwitcherFactoryContextException;

@RunWith(PowerMockRunner.class)
public class SwitcherFactoryTest {
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void offlineShouldReturnException_contextNotInitialized() throws Exception {
		SwitcherFactory.getSwitcher("USECASE11");
	}

}
