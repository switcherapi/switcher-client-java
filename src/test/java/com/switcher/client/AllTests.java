package com.switcher.client;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.switcher.client.domain.SwitcherDomainTest;
import com.switcher.client.utils.SwitcherUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({
        SwitcherOfflineTest.class,
        SwitcherOnlineTest.class,
        SwitcherFactoryTest.class,
        SwitcherUtilsTest.class,
        SwitcherDomainTest.class})
public class AllTests {
	
}
