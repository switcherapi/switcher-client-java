package com.github.petruki.switcher.client;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.petruki.switcher.client.domain.SwitcherDomainTest;
import com.github.petruki.switcher.client.utils.SwitcherUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({
        SwitcherOfflineTest.class,
        SwitcherOnlineTest.class,
        SwitcherFactoryTest.class,
        SwitcherUtilsTest.class,
        SwitcherDomainTest.class,
        SwitcherBypassTest.class })
public class AllTests {
	
}
