package com.switcherapi.client.service.local;

import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.client.model.criteria.Config;
import com.switcherapi.client.model.criteria.Domain;
import com.switcherapi.client.model.criteria.Group;
import com.switcherapi.client.model.criteria.Strategy;
import com.switcherapi.client.model.SwitcherResult;
import com.switcherapi.client.service.SwitcherFactory;
import com.switcherapi.client.service.SwitcherValidator;
import com.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Local Switcher Service retain the same main functionalities as the Remote,
 * but instead, runs Switcher criteria evaluation against the snapshot files.
 * 
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientLocalService implements ClientLocal {

	private static final Logger logger = LoggerFactory.getLogger(ClientLocalService.class);

	public static final String DISABLED_DOMAIN = "Domain disabled";
	public static final String DISABLED_GROUP = "Group disabled";
	public static final String DISABLED_CONFIG = "Config disabled";
	public static final String HAS_RELAY = "Config has Relay enabled";
	
	private static final String STRATEGY_FAIL_PATTERN = "Strategy %s does not agree";
	private static final String STRATEGY_FAIL_NO_INPUT_PATTERN = "Strategy %s did not receive any input";

	private static final String LOG_PROCESS_OP_TEMPLATE = "processOperation: configStrategies: {}";

	private final SwitcherValidator validatorService;

	public ClientLocalService(SwitcherValidator validatorService) {
		this.validatorService = validatorService;
	}

	@Override
	public List<String> checkSwitchers(final Set<String> switchers, final Domain domain) {
		List<String> notFound = new ArrayList<>();

		for (final String switcher : switchers) {
			if (Arrays.stream(domain.getGroup()).noneMatch(group ->
					Arrays.stream(group.getConfig()).anyMatch(config -> config.getKey().equals(switcher)))) {
				notFound.add(switcher);
			}
		}

		return notFound;
	}

	@Override
	public SwitcherResult executeCriteria(final SwitcherRequest switcher, final Domain domain) {
		if (!domain.isActivated()) {
			return SwitcherFactory.buildResultDisabled(DISABLED_DOMAIN, switcher);
		}

		Config config;
		for (final Group group : domain.getGroup()) {
			config = findConfigInGroup(group, switcher.getSwitcherKey());

			if (config != null) {
				return getSwitcherResult(switcher, group, config);
			}
		}

		throw new SwitcherKeyNotFoundException(switcher.getSwitcherKey());
	}

	private SwitcherResult getSwitcherResult(SwitcherRequest switcher, Group group, Config config) {
		if (!group.isActivated()) {
			return SwitcherFactory.buildResultDisabled(DISABLED_GROUP, switcher);
		}

		if (!config.isActivated()) {
			return SwitcherFactory.buildResultDisabled(DISABLED_CONFIG, switcher);
		}

		if (config.hasRelayEnabled() && switcher.isRelayRestricted()) {
			return SwitcherFactory.buildResultDisabled(HAS_RELAY, switcher);
		}

		if (ArrayUtils.isNotEmpty(config.getStrategies())) {
			return this.processOperation(config.getStrategies(), switcher.getEntry(), switcher);
		}

		return SwitcherFactory.buildResultEnabled(switcher);
	}

	private Config findConfigInGroup(final Group group, final String switcherKey) {
		return Arrays.stream(group.getConfig())
				.filter(c -> c.getKey().equals(switcherKey))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Process Strategy operations based on the strategy settings
	 * 
	 * @param configStrategies to be processed
	 * @param input            sent by the client
	 * @return SwitcherResult containing the result of the execution
	 * @throws SwitcherException If encountered either invalid input or misconfiguration
	 */
	private SwitcherResult processOperation(final Strategy[] configStrategies, final List<Entry> input,
											final SwitcherRequest switcher) {
		SwitcherUtils.debug(logger, LOG_PROCESS_OP_TEMPLATE, Arrays.toString(configStrategies));

		for (final Strategy strategy : configStrategies) {
			if (!strategy.isActivated()) {
				continue;
			}

			final Entry switcherInput = tryGetSwitcherInput(input, strategy);
			
			if (switcherInput == null) {
				return strategyFailed(switcher, strategy, STRATEGY_FAIL_NO_INPUT_PATTERN);
			}

			if (!validatorService.execute(strategy, switcherInput)) {
				return strategyFailed(switcher, strategy, STRATEGY_FAIL_PATTERN);
			}
		}

		return SwitcherFactory.buildResultEnabled(switcher);
	}
	
	private SwitcherResult strategyFailed(SwitcherRequest switcher, Strategy strategy, String pattern) {
		return SwitcherFactory.buildResultDisabled(String.format(pattern, strategy.getStrategy()), switcher);
	}
	
	private Entry tryGetSwitcherInput(final List<Entry> input, Strategy strategy) {
		if (input == null) {
			return null;
		}
		
		return input.stream()
				.filter(i -> i.getStrategy().equals(strategy.getStrategy()))
				.findFirst()
				.orElse(null);
	}

}
