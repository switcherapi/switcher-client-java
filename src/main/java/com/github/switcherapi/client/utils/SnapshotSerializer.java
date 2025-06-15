package com.github.switcherapi.client.utils;

import com.github.switcherapi.client.model.criteria.Config;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Group;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Objects;

class SnapshotSerializer implements JsonSerializer<Domain> {

	@Override
	public JsonElement serialize(Domain domain, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();

		jsonObject.add(Field.NAME.value(), context.serialize(domain.getName()));
		jsonObject.add(Field.DESCRIPTION.value(), context.serialize(domain.getDescription()));
		jsonObject.add(Field.ACTIVATED.value(), context.serialize(domain.isActivated()));
		jsonObject.add(Field.VERSION.value(), context.serialize(domain.getVersion()));
		jsonObject.add(Field.GROUP.value(), serializeGroup(domain, context));

		return jsonObject;
	}

	private JsonArray serializeGroup(Domain domain, JsonSerializationContext context) {
		JsonArray groupArray = new JsonArray();

		if (Objects.nonNull(domain.getGroup())) {
			for (Group group : domain.getGroup()) {
				JsonObject groupObject = new JsonObject();
				groupObject.add(Field.NAME.value(), context.serialize(group.getName()));
				groupObject.add(Field.DESCRIPTION.value(), context.serialize(group.getDescription()));
				groupObject.add(Field.ACTIVATED.value(), context.serialize(group.isActivated()));
				groupObject.add(Field.CONFIG.value(), serializeConfig(group, context));
				groupArray.add(groupObject);
			}
		}

		return groupArray;
	}

	private JsonArray serializeConfig(Group group, JsonSerializationContext context) {
		JsonArray configArray = new JsonArray();

		if (Objects.nonNull(group.getConfig())) {
			for (Config config : group.getConfig()) {
				JsonObject configObject = new JsonObject();
				configObject.add(Field.KEY.value(), context.serialize(config.getKey()));
				configObject.add(Field.DESCRIPTION.value(), context.serialize(config.getDescription()));
				configObject.add(Field.ACTIVATED.value(), context.serialize(config.isActivated()));
				configObject.add(Field.STRATEGIES.value(), serializeStrategies(config, context));
				configObject.add(Field.RELAY.value(), context.serialize(config.getRelay()));
				configObject.add(Field.COMPONENTS.value(), context.serialize(config.getComponents()));

				configArray.add(configObject);
			}
		}

		return configArray;
	}

	private JsonArray serializeStrategies(Config config, JsonSerializationContext context) {
		JsonArray strategiesArray = new JsonArray();

		if (Objects.nonNull(config.getStrategies())) {
			for (Strategy strategy : config.getStrategies()) {
				JsonObject strategyObject = new JsonObject();
				strategyObject.add(Field.STRATEGY.value(), context.serialize(strategy.getStrategy()));
				strategyObject.add(Field.OPERATION.value(), context.serialize(strategy.getOperation()));
				strategyObject.add(Field.DESCRIPTION.value(), context.serialize(strategy.getDescription()));
				strategyObject.add(Field.ACTIVATED.value(), context.serialize(strategy.isActivated()));
				strategyObject.add(Field.VALUES.value(), context.serialize(strategy.getValues()));
				strategiesArray.add(strategyObject);
			}
		}

		return strategiesArray;
	}

	private enum Field {
		NAME("name"),
		DESCRIPTION("description"),
		ACTIVATED("activated"),
		VERSION("version"),
		GROUP("group"),
		CONFIG("config"),
		KEY("key"),
		COMPONENTS("components"),
		STRATEGIES("strategies"),
		RELAY("relay"),
		STRATEGY("strategy"),
		OPERATION("operation"),
		VALUES("values");

		private final String value;

		Field(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}
	}

}