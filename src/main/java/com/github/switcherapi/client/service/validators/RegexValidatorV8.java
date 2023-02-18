package com.github.switcherapi.client.service.validators;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.exception.SwitcherValidatorException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.criteria.Strategy;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Regex Validator for applications running using Java 1.8
 * 'String.matches' has been improved since Java 9, and it is expected be ReDoS-safe
 * <p>
 * This implementation allow you to define the sweet-spot process timing when using Regex Strategies
 *
 * @author Roger Floriano (petruki)
 * @since 2023-02-18
 */
@ValidatorComponent(type = StrategyValidator.REGEX)
public class RegexValidatorV8 extends Validator {

	private static final Logger logger = LogManager.getLogger(RegexValidatorV8.class);

	private static final String DELIMITER_REGEX = "\\b%s\\b";
	private final Set<Pair<String, String>> blackList;
	private final TimedMatch timedMatch;

	public RegexValidatorV8() {
		timedMatch = new TimedMatch();
		blackList = new HashSet<>();
	}

	@Override
	public boolean process(Strategy strategy, Entry switcherInput) throws SwitcherInvalidOperationException {
		try {
			switch (strategy.getEntryOperation()) {
				case EXIST:
					return Arrays.stream(strategy.getValues()).anyMatch(val -> {
						try {
							return timedMatch(switcherInput.getInput(), val);
						} catch (TimeoutException | SwitcherValidatorException e) {
							logger.error(e);
							return false;
						}
					});
				case NOT_EXIST:
					return Arrays.stream(strategy.getValues()).noneMatch(val -> {
						try {
							return timedMatch(switcherInput.getInput(), val);
						} catch (TimeoutException | SwitcherValidatorException e) {
							logger.error(e);
							return true;
						}
					});
				case EQUAL:
					return strategy.getValues().length == 1
							&& timedMatch(switcherInput.getInput(), String.format(DELIMITER_REGEX, strategy.getValues()[0]));
				case NOT_EQUAL:
					return strategy.getValues().length == 1
							&& !timedMatch(switcherInput.getInput(), String.format(DELIMITER_REGEX, strategy.getValues()[0]));
				default:
					throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
			}
		} catch (TimeoutException | SwitcherValidatorException e) {
			logger.error(e);
			return false;
		}
	}

	private boolean timedMatch(final String input, final String regex) throws TimeoutException {
		if (isBlackListed(input, regex))
			throw new SwitcherValidatorException(input, regex);

		timedMatch.init(input, regex);
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<Boolean> future = executor.submit(timedMatch);

		try {
			return future.get(Integer.parseInt(SwitcherContextBase.contextStr(ContextKey.REGEX_TIMEOUT)),
					TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			addBlackList(input, regex);
			future.cancel(true);
			throw new TimeoutException();
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			throw new SwitcherValidatorException(input, regex);
		} finally {
			executor.shutdownNow();
		}
	}

	private boolean isBlackListed(final String input, final String regex) {
		return blackList.stream().anyMatch(bl ->
				regex.equals(bl.getRight()) && bl.getLeft().toLowerCase().contains(input.toLowerCase()));
	}

	private void addBlackList(final String input, final String regex) {
		blackList.add(Pair.of(input, regex));
	}

	static final class TimedMatch implements Callable<Boolean> {
		private String input;
		private String regex;

		public void init(String input, String regex) {
			this.input = input;
			this.regex = regex;
		}

		@Override
		public Boolean call() {
			return input.matches(regex);
		}
	}

}