package com.github.switcherapi.client;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This extension implements a Parameterized Test that can mock Switcher results and
 * reset after its conclusion
 * 
 * @author Roger Floriano (petruki)
 */
class SwitcherMockExtension implements AfterTestExecutionCallback,
	ArgumentsProvider, AnnotationConsumer<SwitcherMock> {

	private static final String STORE_KEYS = "store_keys";
	private static final String STORE_KEY = "store_key";
	
	private String key;
	
	private boolean result;

	private SwitcherMockValue[] switchers;

	@Override
	public void accept(SwitcherMock switcherTester) {
		this.key = switcherTester.key();
		this.result = switcherTester.result();
		this.switchers = switcherTester.switchers();
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		if (ArrayUtils.isNotEmpty(switchers)) {
			return provideMultipleArguments(context);
		}

		return provideSingleArgument(context);
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
		Optional<ExtensionContext> parent = context.getParent();
		if (parent.isPresent()) {
			Store store = getStore(parent.get());
			String[] keys = store.remove(STORE_KEYS, String[].class);

			if (ArrayUtils.isNotEmpty(keys)) {
				for (String keyStored : keys) {
					SwitcherExecutor.forget(keyStored);
				}
			} else {
				String switcherKey = store.remove(STORE_KEY, String.class);
				SwitcherExecutor.forget(switcherKey);
			}
		}
	}

	private Stream<? extends Arguments> provideMultipleArguments(ExtensionContext context) {
		String[] keys = Arrays.stream(switchers)
				.map(SwitcherMockValue::key)
				.toArray(String[]::new);

		for (SwitcherMockValue value : switchers) {
			SwitcherExecutor.assume(value.key(), value.result());
		}

		getStore(context).put(STORE_KEYS, keys);
		return Stream.of(Arguments.of(keys, null));
	}

	private Stream<? extends Arguments> provideSingleArgument(ExtensionContext context) {
		SwitcherExecutor.assume(key, result);
		getStore(context).put(STORE_KEY, key);

		return Stream.of(Arguments.of(key, null));
	}
	
	private Store getStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context));
    }

}
