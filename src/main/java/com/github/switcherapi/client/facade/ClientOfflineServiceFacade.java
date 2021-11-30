package com.github.switcherapi.client.facade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidNumericFormat;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.github.switcherapi.client.exception.SwitcherInvalidStrategyException;
import com.github.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.exception.SwitcherNoInputReceivedException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Config;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Group;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.utils.SwitcherUtils;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientOfflineServiceFacade {
	
	private static final Logger logger = LogManager.getLogger(ClientOfflineServiceFacade.class);
	
	private static final String DEBUG_SWITCHER_INPUT = "switcherInput: %s";
	private static final String DEBUG_STRATEGY = "strategy: %s";
	
	public static final String DATE_REGEX = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";
	public static final String CIDR_REGEX = "^([0-9]{1,3}\\.){3}[0-9]{1,3}(\\/([0-9]|[1-2][0-9]|3[0-2]))";
	public static final String DELIMITER_REGEX = "\\b%s\\b";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DISABLED_DOMAIN = "Domain disabled";
	public static final String DISABLED_GROUP = "Group disabled";
	public static final String DISABLED_CONFIG = "Config disabled";

	private static ClientOfflineServiceFacade instance;

	private ClientOfflineServiceFacade() {}

	public static ClientOfflineServiceFacade getInstance() {
		
		if (instance == null) {
			instance = new ClientOfflineServiceFacade();
		}
		return instance;
	}
	
	public List<String> checkSwitchers(final Set<String> switchers, final Domain domain) {
		List<String> notFound = new ArrayList<>();
		
		boolean found = false;
		for (String switcher : switchers) {
			found = false;
			for (final Group group : domain.getGroup()) {
				if (Arrays.stream(group.getConfig())
					.anyMatch(config -> config.getKey().equals(switcher))) 
				{
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
	 * Execute the criteria validation based on the configuration three.
	 * It starts validating from the top of the node (Domain) ascending to the lower level (Strategy)
	 * 
	 * @param switcher Configuration switcher to be validate
	 * @param domain Top level of the configuration three
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

		return new CriteriaResponse(true, "Success", switcher);
	}

	/**
	 * Validate a found strategy based on both input and its configuration
	 * 
	 * @param configStrategies Strategies registered inside a Switcher component
	 * @param input Input sent by the client
	 * @return CristeriaResponse containing the result of the validation
	 * @throws SwitcherException If encountered either invalid input or misconfiguration
	 */
	private CriteriaResponse processOperation(
			final Strategy[] configStrategies, final List<Entry> input, final Switcher switcher) {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("configStrategies: %s", Arrays.toString(configStrategies)));
			logger.debug(String.format("input: %s", Arrays.toString(input != null ? input.toArray() : ArrayUtils.EMPTY_STRING_ARRAY)));
		}
		
		boolean result = true;
		for (final Strategy strategy : configStrategies) {
			if (!strategy.isActivated())
				continue;

			final Entry switcherInput = input != null ? 
					input.stream()
						.filter(i -> i.getStrategy().equals(strategy.getStrategy()))
						.findFirst()
						.orElse(null) : null;

			if (switcherInput == null)
				throw new SwitcherNoInputReceivedException(strategy.getStrategy());
			
			result = selectCaseOperation(result, strategy, switcherInput);
			if (!result) {
				return new CriteriaResponse(
						false, String.format("Strategy %s does not agree", strategy.getStrategy()), switcher);
			}
		}

		return new CriteriaResponse(result, "Success", switcher);
	}

	private boolean selectCaseOperation(boolean result, final Strategy strategy,
			final Entry switcherInput) {
		switch (strategy.getStrategy()) {
		case Entry.VALUE:
			result = this.processValue(strategy, switcherInput);
			break;
		case Entry.NUMERIC:
			result = this.processNumeric(strategy, switcherInput);
			break;
		case Entry.NETWORK:
			result = this.processNetwork(strategy, switcherInput);
			break;
		case Entry.DATE:
			result = this.processDate(strategy, switcherInput);
			break;
		case Entry.TIME:
			result = this.processTime(strategy, switcherInput);
			break;
		case Entry.REGEX:
			result = this.processRegex(strategy, switcherInput);
			break;
		default:
			throw new SwitcherInvalidStrategyException(strategy.getStrategy());
		}
		return result;
	}

	private boolean processNetwork(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherInvalidOperationException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(DEBUG_STRATEGY, strategy));
			logger.debug(String.format(DEBUG_SWITCHER_INPUT, switcherInput));
		}

		switch (strategy.getOperation()) {
		case Entry.EXIST:
			return verifyIfAddressExistInNetwork(strategy, switcherInput);
		case Entry.NOT_EXIST:
			return !verifyIfAddressExistInNetwork(strategy, switcherInput);
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

	private boolean verifyIfAddressExistInNetwork(final Strategy strategy, final Entry switcherInput) {
		SubnetInfo subnetInfo;
		for (final String value : strategy.getValues()) {
			if (value.matches(CIDR_REGEX)) {
				subnetInfo = new SubnetUtils(value).getInfo();

				if (subnetInfo.isInRange(switcherInput.getInput())) {
					return true;
				}
			} else {
				if (value.equals(switcherInput.getInput())) {
					return true;
				}
			}
		}
		
		return false;
	}

	private boolean processValue(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherInvalidOperationException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(DEBUG_STRATEGY, strategy));
			logger.debug(String.format(DEBUG_SWITCHER_INPUT, switcherInput));
		}
		
		switch (strategy.getOperation()) {
		case Entry.EXIST:
			return Arrays.stream(strategy.getValues()).anyMatch(val -> val.equals(switcherInput.getInput()));
		case Entry.NOT_EXIST:
			return Arrays.stream(strategy.getValues()).noneMatch(val -> val.equals(switcherInput.getInput()));
		case Entry.EQUAL:
			return strategy.getValues().length == 1 && strategy.getValues()[0].equals(switcherInput.getInput());
		case Entry.NOT_EQUAL:
			return strategy.getValues().length == 1 && !strategy.getValues()[0].equals(switcherInput.getInput());
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}
	
	private boolean processNumeric(final Strategy strategy, final Entry switcherInput) {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(DEBUG_STRATEGY, strategy));
			logger.debug(String.format(DEBUG_SWITCHER_INPUT, switcherInput));
		}
		
		if (!NumberUtils.isCreatable(switcherInput.getInput()))
			throw new SwitcherInvalidNumericFormat(switcherInput.getInput());
		
		switch (strategy.getOperation()) {
		case Entry.EXIST:
			return Arrays.stream(strategy.getValues()).anyMatch(val -> val.equals(switcherInput.getInput()));
		case Entry.NOT_EXIST:
			return Arrays.stream(strategy.getValues()).noneMatch(val -> val.equals(switcherInput.getInput()));
		case Entry.EQUAL:
			return strategy.getValues().length == 1 && strategy.getValues()[0].equals(switcherInput.getInput());
		case Entry.NOT_EQUAL:
			return strategy.getValues().length == 1 && !strategy.getValues()[0].equals(switcherInput.getInput());
		case Entry.LOWER:
			if (strategy.getValues().length == 1) {
				final double numericInput = NumberUtils.createNumber(switcherInput.getInput()).doubleValue();
				final double numericValue = NumberUtils.createNumber(strategy.getValues()[0]).doubleValue();
				return numericInput < numericValue;
			}
			break;
		case Entry.GREATER:
			if (strategy.getValues().length == 1) {
				final double numericInput = NumberUtils.createNumber(switcherInput.getInput()).doubleValue();
				final double numericValue = NumberUtils.createNumber(strategy.getValues()[0]).doubleValue();
				return numericInput > numericValue;
			}
			break;
		case Entry.BETWEEN:
			if (strategy.getValues().length == 2) {
				final double numericInput = NumberUtils.createNumber(switcherInput.getInput()).doubleValue();
				final double numericFirstValue = NumberUtils.createNumber(strategy.getValues()[0]).doubleValue();
				final double numericSecondValue = NumberUtils.createNumber(strategy.getValues()[1]).doubleValue();
				return numericInput >= numericFirstValue && numericFirstValue <= numericSecondValue;
			}
			break;
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
		
		throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
	}

	private boolean processDate(final Strategy strategy, final Entry switcherInput)
			throws SwitcherInvalidOperationException, SwitcherInvalidTimeFormat, SwitcherInvalidOperationInputException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(DEBUG_STRATEGY, strategy));
			logger.debug(String.format(DEBUG_SWITCHER_INPUT, switcherInput));
		}
		
		try {
			return selectDateOperationCase(strategy, switcherInput);
		} catch (ParseException e) {
			logger.error(e);
			throw new SwitcherInvalidTimeFormat(strategy.getStrategy(), e);
		}

	}

	private boolean selectDateOperationCase(final Strategy strategy,
			final Entry switcherInput) throws ParseException {
		Date stgDate, stgDate2, inputDate;
		
		switch (strategy.getOperation()) {
		case Entry.LOWER:
			stgDate = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(SwitcherUtils.getFullDate(switcherInput.getInput()), DATE_FORMAT);

			return inputDate.before(stgDate);
		case Entry.GREATER:
			stgDate = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(SwitcherUtils.getFullDate(switcherInput.getInput()), DATE_FORMAT);

			return inputDate.after(stgDate);
		case Entry.BETWEEN:
			if (strategy.getValues().length == 2) {
				stgDate = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[0]), DATE_FORMAT);
				stgDate2 = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[1]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(SwitcherUtils.getFullDate(switcherInput.getInput()), DATE_FORMAT);

				return inputDate.after(stgDate) && inputDate.before(stgDate2);
			}
			
			throw new SwitcherInvalidOperationInputException(Entry.BETWEEN);
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

	private boolean processTime(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherInvalidOperationException, SwitcherInvalidTimeFormat, SwitcherInvalidOperationInputException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(DEBUG_STRATEGY, strategy));
			logger.debug(String.format(DEBUG_SWITCHER_INPUT, switcherInput));
		}
		
		try {
			final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			final String today = format.format(new Date());
			return selectTimeOperationCase(strategy, switcherInput, today);
		} catch (ParseException e) {
			logger.error(e);
			throw new SwitcherInvalidTimeFormat(strategy.getStrategy(), e);
		}

	}

	private boolean selectTimeOperationCase(final Strategy strategy,
			final Entry switcherInput, final String today) throws ParseException {
		Date stgDate, stgDate2, inputDate;
		
		switch (strategy.getOperation()) {
		case Entry.LOWER:
			stgDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

			return inputDate.before(stgDate);
		case Entry.GREATER:
			stgDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
			inputDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

			return inputDate.after(stgDate);
		case Entry.BETWEEN:
			if (strategy.getValues().length == 2) {
				stgDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
				stgDate2 = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[1]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

				return inputDate.after(stgDate) && inputDate.before(stgDate2);
			}
			
			throw new SwitcherInvalidOperationInputException(Entry.BETWEEN);
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}
	
	private boolean processRegex(final Strategy strategy, final Entry switcherInput) 
			throws SwitcherInvalidOperationException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format(DEBUG_STRATEGY, strategy));
			logger.debug(String.format(DEBUG_SWITCHER_INPUT, switcherInput));
		}
		
		switch (strategy.getOperation()) {
		case Entry.EXIST:
			return Arrays.stream(strategy.getValues()).anyMatch(val -> switcherInput.getInput().matches(val));
		case Entry.NOT_EXIST:
			return Arrays.stream(strategy.getValues()).noneMatch(val -> switcherInput.getInput().matches(val));
		case Entry.EQUAL:
			return strategy.getValues().length == 1 && 
				switcherInput.getInput().matches(String.format(DELIMITER_REGEX, strategy.getValues()[0]));
		case Entry.NOT_EQUAL:
			return strategy.getValues().length == 1 && 
				!switcherInput.getInput().matches(String.format(DELIMITER_REGEX, strategy.getValues()[0]));
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}

}
