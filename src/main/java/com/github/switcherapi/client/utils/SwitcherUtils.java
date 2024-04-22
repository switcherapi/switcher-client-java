package com.github.switcherapi.client.utils;

import com.github.switcherapi.client.SwitcherExecutor;
import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.service.WorkerName;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherUtils {
	
	private static final Logger logger = LogManager.getLogger(SwitcherUtils.class);
	
	private static final String LOG_DATE = "date: {}";
	
	private static final String LOG_TME = "time: {}";
	
	private static final String LOG_ADDVALUE = "addValue: {}";
	
	/**
	 * [0] = s (seconds) [1] = m (minutes) [2] = h (hours) [3] = d (days)
	 */
	private static final String[] DURATION = { "s", "m", "h", "d" };
	
	private static final String FULL_DATE_REGEX = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";
	
	private static final String ENV_VARIABLE_PATTERN = "\\$\\{(\\w+)}";
	
	private static final String ENV_DEFAULT_VARIABLE_PATTERN = "\\$\\{(\\w+):(.+)}";
	
	private static final String PAYLOAD_PATTERN = "%s.%s";
	
	private static SnapshotWatcher watcher;

	private static ExecutorService executorService;
	
	private SwitcherUtils() {}
	
	public static Date addTimeDuration(final String addValue, final Date date) 
			throws SwitcherInvalidDateTimeArgumentException {
		SwitcherUtils.debug(logger, LOG_ADDVALUE, addValue);
		SwitcherUtils.debug(logger, LOG_DATE, date);
		
		if (addValue.endsWith(DURATION[0])) {
			return DateUtils.addSeconds(date, Integer.parseInt(addValue.replace(DURATION[0], StringUtils.EMPTY)));
		} else if (addValue.endsWith(DURATION[1])) {
			return DateUtils.addMinutes(date, Integer.parseInt(addValue.replace(DURATION[1], StringUtils.EMPTY)));
		} else if (addValue.endsWith(DURATION[2])) {
			return DateUtils.addHours(date, Integer.parseInt(addValue.replace(DURATION[2], StringUtils.EMPTY)));
		} else if (addValue.endsWith(DURATION[3])) {
			return DateUtils.addDays(date, Integer.parseInt(addValue.replace(DURATION[3], StringUtils.EMPTY)));
		}
		
		throw new SwitcherInvalidDateTimeArgumentException(addValue);
	}

	public static long getMillis(final String time) {
		SwitcherUtils.debug(logger, LOG_TME, time);

		if (time.endsWith(DURATION[0])) {
			return (long) (Double.parseDouble(time.replace(DURATION[0], StringUtils.EMPTY)) * 1000L);
		} else if (time.endsWith(DURATION[1])) {
			return (long) (Double.parseDouble(time.replace(DURATION[1], StringUtils.EMPTY)) * 60000L);
		}

		throw new SwitcherInvalidDateTimeArgumentException(time);
	}
	
	public static String getFullDate(final String date) {
		SwitcherUtils.debug(logger, LOG_DATE, date);
		
		final String time = RegExUtils.removePattern(date, FULL_DATE_REGEX).trim();
		return getFullTime(date, time);
	}

	public static String getFullTime(final String date, final String time) {
		SwitcherUtils.debug(logger, LOG_DATE, date);
		SwitcherUtils.debug(logger, LOG_TME, time);
		
		if (StringUtils.isBlank(time)) {
			return String.format("%s 00:00:00", date);
		} else if (time.split(":").length == 1) {
			return String.format("%s %s:00:00", date.split(" ")[0], time);
		} else if (time.split(":").length == 2) {
			return String.format("%s %s:00", date.split(" ")[0], time);
		}

		return date;
	}
	
	public static Set<String> payloadReader(String jsonStr, String prevKey) {
		final JsonElement parser = JsonParser.parseString(jsonStr);
		final JsonObject jsonObject = parser.getAsJsonObject();
		
		final Set<String> keys = Sets.newHashSet();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key;
			if (prevKey == null) {
				key = entry.getKey();
			} else {
				key = String.format(PAYLOAD_PATTERN, prevKey, entry.getKey());
			}
			
			keys.add(key);
			if (entry.getValue().isJsonObject()) {
				keys.addAll(payloadReader(entry.getValue().toString(), key));
			} else if (entry.getValue().isJsonArray() && 
					entry.getValue().getAsJsonArray().get(0).isJsonObject()) {
				entry.getValue().getAsJsonArray()
					.forEach(eValue -> keys.addAll(payloadReader(eValue.toString(), key)));
			}
		}
		
		return keys;
	}
	
	/**
	 * Initialize instance of SnapshotWatcher to run in the background.
	 * 
	 * @param executorInstance of a Remote or Local Switcher
	 * @param handler to notify snapshot change events
	 */
	public static void watchSnapshot(final SwitcherExecutor executorInstance, SnapshotEventHandler handler) {
		if (watcher == null)
			watcher = new SnapshotWatcher(executorInstance, handler);

		initExecutorService();
		executorService.submit(watcher);
	}
	
	/**
	 * If an instance of SnapshotWatcher is available, this operation will force it to terminate
	 * and indicates to GC that the instance should be wiped from the memory.
	 */
	public static void stopWatchingSnapshot() {
		if (executorService != null) {
			executorService.shutdownNow();
		}

		if (watcher != null) {
			watcher.terminate();
			watcher = null;
		}
	}
	
	/**
	 * Resolve properties from switcherapi.properties file.
	 * It reads environment values when using the following notation: ${VALUE} or
	 * ${VALUE:DEFAULT_VALUE} in case a default value is provided.
	 * <p>
	 * Two different RE were used here to mitigate catastrophic backtracking situations.
	 * 
	 * @param key reads values from {@link ContextKey#getParam()}
	 * @param prop from properties file
	 * @return resolved value
	 */
	public static String resolveProperties(String key, Properties prop) {
		final String value = prop.getProperty(key);
		if (StringUtils.isBlank(value)) {
	        return null;
	    }

	    final StringBuilder sBuilder = resolveEnvironmentVariable(value);
	    if (sBuilder.toString().isEmpty())
	    	return value;
	       
	    return sBuilder.toString();
	}

	/**
	 * Log debug message if logger is enabled.
	 * Use this method to avoid resource waste when logger is disabled.
	 *
	 * @param logger class logger
	 * @param message to be logged
	 * @param args arguments to be replaced in the message
	 */
	public static void debug(Logger logger, String message, Object... args) {
		if (logger.isDebugEnabled()) {
			logger.debug(message, args);
		}
	}

	/**
	 * Log debug message if logger is enabled.
	 * Use this method to avoid resource waste when logger is disabled.
	 *
	 * @param logger class logger
	 * @param message to be logged
	 * @param paramSuppliers parameters to be replaced in the message
	 */
	public static void debug(Logger logger, String message, Supplier<?> paramSuppliers) {
		if (logger.isDebugEnabled()) {
			logger.debug(message, paramSuppliers);
		}
	}

	/**
	 * Resolve environment variable 'value'and extract its value from either
	 * System environment or default argument.
	 * 
	 * @param value assigned from the properties file
	 * @return Resolved value
	 */
	private static StringBuilder resolveEnvironmentVariable(final String value) {
		Pattern pattern = Pattern.compile(ENV_VARIABLE_PATTERN);
	    Matcher matcher = pattern.matcher(value);
	    StringBuilder sBuilder = new StringBuilder();
	    
	    if (matcher.find()) {
	        if (setWithSystemEnv(matcher, sBuilder)) {
				throw new SwitcherContextException(String.format("Property %s not defined", value));
			}
	    } else {
        	pattern = Pattern.compile(ENV_DEFAULT_VARIABLE_PATTERN);
        	matcher = pattern.matcher(value);
        	
        	 if (matcher.find() &&
					 setWithSystemEnv(matcher, sBuilder) && matcher.group(2) != null) {
				 sBuilder.append(matcher.group(2));
			 }
        }
		return sBuilder;
	}

	/**
	 * Get value from System.getenv and append to sBuilder.
	 * 
	 * @param matcher Matches given property name
	 * @param sBuilder value given to property
	 * @return true if System.getenv returns a value
	 */
	private static boolean setWithSystemEnv(Matcher matcher, StringBuilder sBuilder) {
		if (matcher.group(1) != null) {
			String envVarName = matcher.group(1);
			String envVarValue = System.getenv(envVarName);
			sBuilder.append(null == envVarValue ? StringUtils.EMPTY : envVarValue);		
		}
		
		return StringUtils.isEmpty(sBuilder.toString());
	}

	/**
	 * Configure Executor Service for Snapshot Watch Worker
	 */
	private static void initExecutorService() {
		executorService = Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r);
			thread.setName(WorkerName.SNAPSHOT_WATCH_WORKER.toString());
			thread.setDaemon(true);
			return thread;
		});
	}

}
