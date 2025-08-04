package com.switcherapi.client.test;

import com.switcherapi.client.model.SwitcherResult;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * This extension implements test template, before and after routines to mock Switcher results and
 * reset after its conclusion
 * 
 * @author Roger Floriano (petruki)
 */
class SwitcherTestExtension implements TestTemplateInvocationContextProvider,
		AfterTestExecutionCallback, BeforeTestExecutionCallback {

	private static final String STORE_KEYS = "mock.keys";
	private static final String STORE_KEY = "mock.key";

	private boolean abTest;

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return context.getRequiredTestMethod().isAnnotationPresent(SwitcherTest.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		SwitcherTest switcherTest = context.getRequiredTestMethod().getAnnotation(SwitcherTest.class);

		if (switcherTest.abTest()) {
			return Stream.of(
					new SwitcherTestTemplate(switcherTest, true),
					new SwitcherTestTemplate(switcherTest));
		}

		final SwitcherTestTemplate template = new SwitcherTestTemplate(switcherTest);
		return Stream.of(template);
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		SwitcherTest switcherTest = context.getRequiredTestMethod().getAnnotation(SwitcherTest.class);

		if (switcherTest.abTest()) {
			abTest = !abTest;
		}

		if (ArrayUtils.isNotEmpty(switcherTest.switchers())) {
			mockMultipleSwitchers(context, switcherTest, abTest);
		} else {
			mockSingleSwitcher(context, switcherTest, abTest);
		}
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
		Store store = getStore(context);
		String[] keys = store.remove(STORE_KEYS, String[].class);

		if (ArrayUtils.isNotEmpty(keys)) {
			for (String keyStored : keys) {
				SwitcherBypass.forget(keyStored);
			}
		} else {
			String switcherKey = store.remove(STORE_KEY, String.class);
			SwitcherBypass.forget(switcherKey);
		}
	}

	private void mockMultipleSwitchers(ExtensionContext context, SwitcherTest switcherTest, boolean inverted) {
		String[] keys = Arrays.stream(switcherTest.switchers())
				.map(SwitcherTestValue::key)
				.toArray(String[]::new);

		for (SwitcherTestValue value : switcherTest.switchers()) {
			SwitcherResult switcherResult = SwitcherBypass.assume(value.key(), inverted != value.result(), value.metadata());
			applySwitcherTestWhen(switcherResult, value.when());
		}

		getStore(context).put(STORE_KEYS, keys);
	}

	private void mockSingleSwitcher(ExtensionContext context, SwitcherTest switcherTest, boolean inverted) {
		SwitcherResult switcherResult = SwitcherBypass.assume(switcherTest.key(), inverted != switcherTest.result(), switcherTest.metadata());
		applySwitcherTestWhen(switcherResult, switcherTest.when());
		getStore(context).put(STORE_KEY, switcherTest.key());
	}

	private void applySwitcherTestWhen(SwitcherResult switcherResult, SwitcherTestWhen[] whens) {
		for (SwitcherTestWhen when : whens) {
			switcherResult.when(when.strategy(), Arrays.asList(when.input()));
		}
	}
	
	private Store getStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context));
    }

}
