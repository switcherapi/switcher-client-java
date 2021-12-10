package com.github.switcherapi.client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import com.github.switcherapi.client.model.criteria.Config;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Group;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.Strategy;

class ModelTest {
	
	@Test
	void testModelEntry() throws Exception {
		Entry entry1 = new Entry(Entry.DATE, "2019-12-10");
		Entry entry2 = new Entry(Entry.VALUE, "Value");
		
		assertNotEquals(true, entry1.equals(entry2));
		assertEquals(true, entry1.equals(entry1));
		assertNotNull(entry1.toString());
		assertNotEquals(entry1.hashCode(), entry2.hashCode());
	}
	
	@Test
	void testCriteriaPackage() {
		final Strategy strategy = new Strategy();
		strategy.setActivated(true);
		strategy.setDescription("Description");
		strategy.setOperation("Operation");
		strategy.setStrategy("Strategy");
		String[] strategyValues = new String[] { "Value" };
		strategy.setValues(strategyValues);
		
		assertSame("Description", strategy.getDescription());
		assertSame("Operation", strategy.getOperation());
		assertSame("Strategy", strategy.getStrategy());
		assertSame(strategyValues, strategy.getValues());
		
		final Config config = new Config();
		config.setActivated(true);
		config.setKey("Key");
		config.setDescription("Description");
		String[] configComponents = new String[] { "Component" };
		config.setComponents(configComponents);
		Strategy[] strategies = new Strategy[] { strategy };
		config.setStrategies(strategies);
		
		assertSame("Description", config.getDescription());
		assertSame("Key", config.getKey());
		assertSame(configComponents, config.getComponents());
		assertSame(strategies, config.getStrategies());
		
		final Group group = new Group();
		group.setActivated(true);
		group.setDescription("Description");
		group.setName("Name");
		Config[] configs = new Config[] { config };
		group.setConfig(configs);
		
		assertSame("Description", group.getDescription());
		assertSame("Name", group.getName());
		assertSame(configs, group.getConfig());
		
		final Domain domain = new Domain();
		domain.setActivated(true);
		domain.setDescription("Description");
		domain.setName("Name");
		domain.setVersion(10000000000l);
		Group[] groups = new Group[] { group };
		domain.setGroup(groups);
		
		assertSame("Description", domain.getDescription());
		assertSame("Name", domain.getName());
		assertEquals(10000000000l, domain.getVersion());
		assertSame(groups, domain.getGroup());
		
		final Criteria criteria = new Criteria();
		criteria.setDomain(domain);
		
		final Snapshot snapsot = new Snapshot();
		snapsot.setData(criteria);
		
		assertSame(criteria, snapsot.getData());
	}

}
