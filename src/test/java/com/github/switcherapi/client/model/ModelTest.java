package com.github.switcherapi.client.model;

import org.junit.jupiter.api.Test;

import com.github.switcherapi.client.model.criteria.Config;
import com.github.switcherapi.client.model.criteria.Data;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Group;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.Strategy;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
	
	@Test
	void testModelEntry() {
		Entry entry1 = Entry.build(StrategyValidator.DATE, "2019-12-10");
		Entry entry2 = Entry.build(StrategyValidator.VALUE, "Value");
		
		assertNotEquals(true, entry1.equals(entry2));
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
		domain.setVersion(10000000000L);
		Group[] groups = new Group[] { group };
		domain.setGroup(groups);
		
		assertSame("Description", domain.getDescription());
		assertSame("Name", domain.getName());
		assertEquals(10000000000L, domain.getVersion());
		assertSame(groups, domain.getGroup());
		
		final Data data = new Data();
		data.setDomain(domain);
		
		final Snapshot snapshot = new Snapshot();
		snapshot.setData(data);
		
		assertSame(data, snapshot.getData());
	}

}
