package com.github.switcherapi.client.utils;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
import com.github.switcherapi.client.model.ContextKey;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherUtils extends Utils {

	private static final String[] DURATION_UNIT = { "s", "m", "h", "d" };
	private static final String ENV_VARIABLE_PATTERN = "\\$\\{(\\w+)}";
	private static final String ENV_DEFAULT_VARIABLE_PATTERN = "\\$\\{(\\w+):(.+)?}";
	private static final String PAYLOAD_PATTERN = "%s.%s";
	
	public static Date addTimeDuration(final String addValue, final Date date) 
			throws SwitcherInvalidDateTimeArgumentException {
		switch (addValue.charAt(addValue.length() - 1)) {
			case 's':
				return DateUtils.addSeconds(date, Integer.parseInt(addValue.replace(DURATION_UNIT[0], StringUtils.EMPTY)));
			case 'm':
				return DateUtils.addMinutes(date, Integer.parseInt(addValue.replace(DURATION_UNIT[1], StringUtils.EMPTY)));
			case 'h':
				return DateUtils.addHours(date, Integer.parseInt(addValue.replace(DURATION_UNIT[2], StringUtils.EMPTY)));
			case 'd':
				return DateUtils.addDays(date, Integer.parseInt(addValue.replace(DURATION_UNIT[3], StringUtils.EMPTY)));
			default:
				throw new SwitcherInvalidDateTimeArgumentException(addValue);
		}
	}

	public static long getMillis(final String time) {
		switch (time.charAt(time.length() - 1)) {
			case 's':
				return (long) (Double.parseDouble(time.replace(DURATION_UNIT[0], StringUtils.EMPTY)) * 1000L);
			case 'm':
				return (long) (Double.parseDouble(time.replace(DURATION_UNIT[1], StringUtils.EMPTY)) * 60000L);
			default:
				throw new SwitcherInvalidDateTimeArgumentException(time);
		}
	}
	
	public static Set<String> payloadReader(String jsonStr, String prevKey) {
		final JsonElement parser = JsonParser.parseString(jsonStr);
		final JsonObject jsonObject = parser.getAsJsonObject();
		
		final Set<String> keys = new java.util.HashSet<>();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key;
			if (Objects.isNull(prevKey)) {
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

	    final Object[] sBuilder = resolveEnvironmentVariable(value);
	    if (sBuilder[1].equals(Boolean.FALSE) && sBuilder[0].toString().isEmpty()) {
			return value;
		}

	    return sBuilder[0].toString();
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
		logger.debug(message, args);
	}

	/**
	 * Resolve environment variable 'value' and extract its value from either
	 * System environment or default argument.
	 * 
	 * @param value assigned from the properties file
	 * @return Array-pair: Resolved value and a boolean to indicate if it was resolved by var notation ${VAR:VALUE}
	 */
	private static Object[] resolveEnvironmentVariable(final String value) {
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
        	
        	 if (matcher.find() && setWithSystemEnv(matcher, sBuilder)) {
				 // when a value is assigned to variable, e.g. ${PORT:8080}
				 if (Objects.nonNull(matcher.group(2))) {
					 sBuilder.append(matcher.group(2));
					 return new Object[] { sBuilder.toString(), Boolean.TRUE };
				 }

				 // when nothing is assigned to variable, e.g. ${PORT:}
				 if (Objects.nonNull(matcher.group(1))) {
					 return new Object[] { StringUtils.EMPTY, Boolean.TRUE };
				 }
			 }
        }

		return new Object[] { sBuilder.toString(), Boolean.FALSE };
	}

	/**
	 * Get value from System.getenv and append to sBuilder.
	 * 
	 * @param matcher Matches given property name
	 * @param sBuilder value given to property
	 * @return true if System.getenv returns a value
	 */
	private static boolean setWithSystemEnv(Matcher matcher, StringBuilder sBuilder) {
		final String envVarName = matcher.group(1);

		if (Objects.nonNull(envVarName)) {
			sBuilder.append(Optional.ofNullable(System.getenv(envVarName))
					.orElse(StringUtils.EMPTY));
		}
		
		return StringUtils.isEmpty(sBuilder.toString());
	}

}
