package com.switcherapi.client.test;

import com.switcherapi.client.model.SwitcherResult;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * SwitcherBypass is a helper class to manipulate the result of a given Switcher key.<br>
 * It can be used to simulate a result for a specific key, useful for testing purposes.
 */
public class SwitcherBypass {

	private static final Map<String, SwitcherResult> bypass = new HashMap<>();

	private SwitcherBypass() {}

	/**
	 * It manipulates the result of a given Switcher key.
	 *
	 * @param key name of the key that you want to change the result
	 * @param expectedResult that will be returned when performing isItOn
	 * @return SwitcherResult with the manipulated result
	 */
	public static SwitcherResult assume(final String key, boolean expectedResult) {
		return assume(key, expectedResult, null);
	}

	/**
	 * It manipulates the result of a given Switcher key.
	 *
	 * @param key name of the key that you want to change the result
	 * @param metadata additional information about the assumption (JSON)
	 * @param expectedResult that will be returned when performing isItOn
	 * @return SwitcherResult with the manipulated result
	 */
	public static SwitcherResult assume(final String key, boolean expectedResult, String metadata) {
		SwitcherResult switcherResult =  new SwitcherResult();
		switcherResult.setResult(expectedResult);
		switcherResult.setReason("Switcher bypassed");

		if (StringUtils.isNotBlank(metadata)) {
			switcherResult.setMetadata(new Gson().fromJson(metadata, Object.class));
		}

		bypass.put(key, switcherResult);
		return switcherResult;
	}

	/**
	 * It will clean up any result manipulation added before by invoking {@link SwitcherBypass#assume(String, boolean)}
	 *
	 * @param key name of the key you want to remove
	 */
	public static void forget(final String key) {
		bypass.remove(key);
	}

	public static Map<String, SwitcherResult> getBypass() {
		return bypass;
	}

}
