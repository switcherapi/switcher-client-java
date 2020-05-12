package com.github.petruki.switcher.client;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.petruki.switcher.client.model.ModelTest;
import com.github.petruki.switcher.client.utils.SnapshotLoaderTest;
import com.github.petruki.switcher.client.utils.SwitcherUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({
        SwitcherOfflineTest.class,
        SwitcherOnlineTest.class,
        SwitcherFactoryTest.class,
        SwitcherUtilsTest.class,
        SwitcherBypassTest.class,
        SnapshotLoaderTest.class,
        ModelTest.class
})
public class AllTests {
	
}
