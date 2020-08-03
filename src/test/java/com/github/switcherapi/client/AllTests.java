package com.github.switcherapi.client;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.switcherapi.client.model.ModelTest;
import com.github.switcherapi.client.utils.SnapshotLoaderTest;
import com.github.switcherapi.client.utils.SnapshotWatcherTest;
import com.github.switcherapi.client.utils.SwitcherUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({
        SwitcherOfflineTest.class,
        SwitcherOnlineTest.class,
        SwitcherFactoryTest.class,
        SwitcherUtilsTest.class,
        SwitcherBypassTest.class,
        SnapshotLoaderTest.class,
        SnapshotWatcherTest.class,
        ModelTest.class
})
public class AllTests {
	
}
