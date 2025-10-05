package com.switcherapi.client.model;

import com.switcherapi.client.model.criteria.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
	
	@Test
	void testModelEntry() {
		Entry entry1 = Entry.of(StrategyValidator.DATE, "2019-12-10");
		Entry entry2 = Entry.of(StrategyValidator.VALUE, "Value");
		
		assertNotEquals(true, entry1.equals(entry2));
		assertNotNull(entry1.toString());
		assertNotEquals(entry1.hashCode(), entry2.hashCode());
	}
	
	@Test
	void testCriteriaPackage() {
		String[] strategyValues = new String[] { "Value" };
		final StrategyConfig strategyConfig = new StrategyConfig(
				"Strategy",
				"Operation",
				"Description",
				true,
				strategyValues
		);
		
		assertSame("Description", strategyConfig.getDescription());
		assertSame("Operation", strategyConfig.getOperation());
		assertSame("Strategy", strategyConfig.getStrategy());
		assertSame(strategyValues, strategyConfig.getValues());

		StrategyConfig[] strategies = new StrategyConfig[] {strategyConfig};
		String[] configComponents = new String[] { "Component" };
		Relay relay = new Relay(RelayType.NOTIFICATION.name(), false);
		final Config config = new Config(
				"Key",
				"Description",
				true,
				strategies,
				configComponents,
				relay
		);
		
		assertSame("Description", config.getDescription());
		assertSame("Key", config.getKey());
		assertSame(configComponents, config.getComponents());
		assertSame(strategies, config.getStrategies());

		Config[] configs = new Config[] { config };
		final Group group = new Group(
				"Name",
				"Description",
				true,
				configs
		);
		
		assertSame("Description", group.getDescription());
		assertSame("Name", group.getName());
		assertSame(configs, group.getConfig());

		Group[] groups = new Group[] { group };
		final Domain domain = new Domain(
				"Name",
				"Description",
				true,
				10000000000L,
				groups
		);
		
		assertSame("Description", domain.getDescription());
		assertSame("Name", domain.getName());
		assertEquals(10000000000L, domain.getVersion());
		assertSame(groups, domain.getGroup());
	}

}
