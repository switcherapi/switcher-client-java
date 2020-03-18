package com.github.petruki.switcher.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.petruki.switcher.client.SwitcherFactory;
import com.github.petruki.switcher.client.exception.SwitcherFactoryContextException;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class SwitcherFactoryTest {
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void offlineShouldReturnException_contextNotInitialized() throws Exception {
		SwitcherFactory.getSwitcher("USECASE11");
	}

}
