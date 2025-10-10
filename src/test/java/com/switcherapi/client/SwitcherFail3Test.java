package com.switcherapi.client;

import com.switcherapi.client.exception.SwitcherContextException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SwitcherFail3Test {

	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";

	@Test
	void shouldNotRegisterSwitcher_nullKey() {
		//given
		TestCaseNull.configure(ContextBuilder.builder()
				.context(TestCaseNull.class.getName())
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.local(true));

		//test
		Exception exception = assertThrows(SwitcherContextException.class, TestCaseNull::initializeClient);
		assertEquals("Something went wrong: Context has errors - Error retrieving Switcher Key value from field NULL_KEY",
				exception.getMessage());
	}

	@Test
	void shouldNotRegisterSwitcher_emptyKey() {
		//given
		TestCaseEmpty.configure(ContextBuilder.builder()
				.context(TestCaseEmpty.class.getName())
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.local(true));

		//test
		Exception exception = assertThrows(SwitcherContextException.class, TestCaseEmpty::initializeClient);
		assertEquals("Something went wrong: Context has errors - One or more Switcher Keys are empty",
				exception.getMessage());
	}

	static class TestCaseNull extends SwitcherContextBase {
		@SwitcherKey
		public static final String NULL_KEY = null;
	}

	static class TestCaseEmpty extends SwitcherContextBase {
		@SwitcherKey
		public static final String EMPTY_KEY = "";
	}

}
