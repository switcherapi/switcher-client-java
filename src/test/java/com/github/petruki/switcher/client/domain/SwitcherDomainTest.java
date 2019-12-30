package com.github.petruki.switcher.client.domain;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.petruki.switcher.client.domain.Entry;
import com.github.petruki.switcher.client.domain.criteria.Domain;
import com.github.petruki.switcher.client.utils.SnapshotLoader;

@RunWith(PowerMockRunner.class)
public class SwitcherDomainTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources/";
	
	@Test
	public void shouldLoadDomainFromSnapshot() throws Exception {
		final Domain domain = SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		assertNotNull(domain);
		assertNotNull(domain.toString());
	}
	
	@Test
	public void offlineShouldReturnTrue() throws Exception {
		Entry entry1 = new Entry(Entry.DATE, "2019-12-10");
		Entry entry2 = new Entry(Entry.VALUE, "Value");
		
		assertTrue(!entry1.equals(entry2));
		assertTrue(entry1.equals(entry1));
		assertNotNull(entry1.toString());
		assertNotEquals(entry1.hashCode(), entry2.hashCode());
	}

}
