package com.github.petruki.switcher.client.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.petruki.switcher.client.model.criteria.Config;
import com.github.petruki.switcher.client.model.criteria.Criteria;
import com.github.petruki.switcher.client.model.criteria.Domain;
import com.github.petruki.switcher.client.model.criteria.Group;
import com.github.petruki.switcher.client.model.criteria.Snapshot;
import com.github.petruki.switcher.client.model.criteria.Strategy;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class ModelTest {
	
	@Test
	public void testModelEntry() throws Exception {
		Entry entry1 = new Entry(Entry.DATE, "2019-12-10");
		Entry entry2 = new Entry(Entry.VALUE, "Value");
		
		assertTrue(!entry1.equals(entry2));
		assertTrue(entry1.equals(entry1));
		assertNotNull(entry1.toString());
		assertNotEquals(entry1.hashCode(), entry2.hashCode());
	}
	
	@Test
	public void testCriteriaPackage() {
		final Strategy strategy = new Strategy();
		strategy.setActivated(true);
		strategy.setDescription("Description");
		strategy.setOperation("Operation");
		strategy.setStrategy("Strategy");
		String[] strategyValues = new String[] { "Value" };
		strategy.setValues(strategyValues);
		
		assertSame(strategy.getDescription(), "Description");
		assertSame(strategy.getOperation(), "Operation");
		assertSame(strategy.getStrategy(), "Strategy");
		assertSame(strategy.getValues(), strategyValues);
		
		final Config config = new Config();
		config.setActivated(true);
		config.setKey("Key");
		config.setDescription("Description");
		String[] configComponents = new String[] { "Component" };
		config.setComponents(configComponents);
		Strategy[] strategies = new Strategy[] { strategy };
		config.setStrategies(strategies);
		
		assertSame(config.getDescription(), "Description");
		assertSame(config.getKey(), "Key");
		assertSame(config.getComponents(), configComponents);
		assertSame(config.getStrategies(), strategies);
		
		final Group group = new Group();
		group.setActivated(true);
		group.setDescription("Description");
		group.setName("Name");
		Config[] configs = new Config[] { config };
		group.setConfig(configs);
		
		assertSame(group.getDescription(), "Description");
		assertSame(group.getName(), "Name");
		assertSame(group.getConfig(), configs);
		
		final Domain domain = new Domain();
		domain.setActivated(true);
		domain.setDescription("Description");
		domain.setName("Name");
		domain.setVersion(10000000000l);
		Group[] groups = new Group[] { group };
		domain.setGroup(groups);
		
		assertSame(domain.getDescription(), "Description");
		assertSame(domain.getName(), "Name");
		assertEquals(domain.getVersion(), 10000000000l);
		assertSame(domain.getGroup(), groups);
		
		final Criteria criteria = new Criteria();
		criteria.setDomain(domain);
		
		final Snapshot snapsot = new Snapshot();
		snapsot.setData(criteria);
		
		assertSame(snapsot.getData(), criteria);
	}

}
