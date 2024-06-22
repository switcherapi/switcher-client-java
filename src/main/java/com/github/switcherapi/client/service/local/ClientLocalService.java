package com.github.switcherapi.client.service.local;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Config;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Group;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.service.ValidatorService;
import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class ClientLocalService {

	private static final Logger logger = LogManager.getLogger(ClientLocalService.class);

	public static final String DISABLED_DOMAIN = "Domain disabled";
	public static final String DISABLED_GROUP = "Group disabled";
	public static final String DISABLED_CONFIG = "Config disabled";
	
	private static final String STRATEGY_FAIL_PATTERN = "Strategy %s does not agree";
	private static final String STRATEGY_FAIL_NO_INPUT_PATTERN = "Strategy %s did not receive any input";

	private final ValidatorService validatorService;

	public ClientLocalService() {
		this.validatorService = new ValidatorService();
	}

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

	/**
	 * Execute the criteria validation based on the configuration tree. It starts
	 * validating from the top of the node (Domain) ascending to the lower level
	 * (Strategy)
	 * 
	 * @param switcher Configuration switcher to be validated
	 * @param domain   Top level of the configuration tree
	 * @return The criteria result
	 * @throws SwitcherException If encountered either invalid input or misconfiguration
	 */
	public CriteriaResponse executeCriteria(final Switcher switcher, final Domain domain) {
		if (!domain.isActivated()) {
			return CriteriaResponse.buildResultFail(DISABLED_DOMAIN, switcher);
		}

		Config config;
		for (final Group group : domain.getGroup()) {
			config = findConfigInGroup(group, switcher.getSwitcherKey());

			if (config != null) {
				return getCriteriaResponse(switcher, group, config);
			}
		}

		throw new SwitcherKeyNotFoundException(switcher.getSwitcherKey());
	}

	private CriteriaResponse getCriteriaResponse(Switcher switcher, Group group, Config config) {
		if (!group.isActivated()) {
			return CriteriaResponse.buildResultFail(DISABLED_GROUP, switcher);
		}

		if (!config.isActivated()) {
			return CriteriaResponse.buildResultFail(DISABLED_CONFIG, switcher);
		}

		if (ArrayUtils.isNotEmpty(config.getStrategies())) {
			return this.processOperation(config.getStrategies(), switcher.getEntry(), switcher);
		}

		return CriteriaResponse.buildResultSuccess(switcher);
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
	 * @return CriteriaResponse containing the result of the execution
	 * @throws SwitcherException If encountered either invalid input or misconfiguration
	 */
	private CriteriaResponse processOperation(final Strategy[] configStrategies, final List<Entry> input,
			final Switcher switcher) {
		SwitcherUtils.debugSupplier(logger, "configStrategies: {}", Arrays.toString(configStrategies));
		SwitcherUtils.debugSupplier(logger, "input: {}", Arrays.toString(input != null ? input.toArray() : ArrayUtils.EMPTY_STRING_ARRAY));

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

		return CriteriaResponse.buildResultSuccess(switcher);
	}
	
	private CriteriaResponse strategyFailed(Switcher switcher, Strategy strategy, String pattern) {
		return CriteriaResponse.buildResultFail(String.format(pattern, strategy.getStrategy()), switcher);
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
