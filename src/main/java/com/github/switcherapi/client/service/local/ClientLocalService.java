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
	private static final String CRITERIA_SUCCESS = "Success";

	private final ValidatorService validatorService;

	public ClientLocalService() {
		this.validatorService = new ValidatorService();
	}

	public List<String> checkSwitchers(final Set<String> switchers, final Domain domain) {
		List<String> notFound = new ArrayList<>();

		boolean found;
		for (final String switcher : switchers) {
			found = false;
			for (final Group group : domain.getGroup()) {
				if (Arrays.stream(group.getConfig()).anyMatch(config -> config.getKey().equals(switcher))) {
					found = true;
					break;
				}
			}

			if (!found)
				notFound.add(switcher);
		}

		return notFound;
	}

	/**
	 * Execute the criteria validation based on the configuration three. It starts
	 * validating from the top of the node (Domain) ascending to the lower level
	 * (Strategy)
	 * 
	 * @param switcher Configuration switcher to be validated
	 * @param domain   Top level of the configuration three
	 * @return The criteria result
	 * @throws SwitcherException If encountered either invalid input or misconfiguration
	 */
	public CriteriaResponse executeCriteria(final Switcher switcher, final Domain domain) {
		if (!domain.isActivated()) {
			return new CriteriaResponse(false, DISABLED_DOMAIN, switcher);
		}

		Config configFound = null;
		for (final Group group : domain.getGroup()) {
			// validate in which group the switcher is configured
			configFound = Arrays.stream(group.getConfig())
					.filter(config -> config.getKey().equals(switcher.getSwitcherKey()))
					.findFirst()
					.orElse(null);

			if (configFound != null) {

				if (!group.isActivated()) {
					return new CriteriaResponse(false, DISABLED_GROUP, switcher);
				}

				if (!configFound.isActivated()) {
					return new CriteriaResponse(false, DISABLED_CONFIG, switcher);
				}

				if (ArrayUtils.isNotEmpty(configFound.getStrategies())) {
					return this.processOperation(configFound.getStrategies(), switcher.getEntry(), switcher);
				}

				break;
			}
		}

		if (configFound == null) {
			throw new SwitcherKeyNotFoundException(switcher.getSwitcherKey());
		}

		return new CriteriaResponse(true, CRITERIA_SUCCESS, switcher);
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
		SwitcherUtils.debug(logger, "configStrategies: {}", () -> Arrays.toString(configStrategies));
		SwitcherUtils.debug(logger, "input: {}", () -> Arrays.toString(input != null ? input.toArray() : ArrayUtils.EMPTY_STRING_ARRAY));

		boolean result;
		for (final Strategy strategy : configStrategies) {
			if (!strategy.isActivated())
				continue;

			final Entry switcherInput = tryGetSwitcherInput(input, strategy);
			
			if (switcherInput == null)
				return strategyFailed(switcher, strategy, STRATEGY_FAIL_NO_INPUT_PATTERN);

			result = validatorService.execute(strategy, switcherInput);
			if (!result) {
				return strategyFailed(switcher, strategy, STRATEGY_FAIL_PATTERN);
			}
		}

		return new CriteriaResponse(true, CRITERIA_SUCCESS, switcher);
	}
	
	private CriteriaResponse strategyFailed(Switcher switcher, Strategy strategy, String pattern) {
		return new CriteriaResponse(false, String.format(pattern, strategy.getStrategy()),
				switcher);
	}
	
	private Entry tryGetSwitcherInput(final List<Entry> input, Strategy strategy) {
		if (input == null)
			return null;
		
		return input.stream()
				.filter(i -> i.getStrategy().equals(strategy.getStrategy()))
				.findFirst()
				.orElse(null);
	}

}
