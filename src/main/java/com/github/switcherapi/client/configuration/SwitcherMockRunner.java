package com.github.switcherapi.client.configuration;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import com.github.switcherapi.client.factory.SwitcherExecutor;
import com.github.switcherapi.client.model.Switcher;

/**
 * This runner implements a Parameterized Test that can mock the Switcher result and
 * reset after its conclusion
 * 
 * @author Roger Floriano (petruki)
 */
public class SwitcherMockRunner implements AfterTestExecutionCallback, 
	ArgumentsProvider, AnnotationConsumer<SwitcherMock> {
	
	private String key;
	
	private boolean result;

	@Override
	public void accept(SwitcherMock switcherTester) {
		this.key = switcherTester.key();
		this.result = switcherTester.result();
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) 
			throws Exception {
		SwitcherExecutor.assume(key, result);
		getStore(context).put(Switcher.KEY, key);
		return Stream.of(Arguments.of(key, null));
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
        String key = getStore(context.getParent().get()).remove(Switcher.KEY, String.class);
		SwitcherExecutor.forget(key);
	}
	
	private Store getStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context));
    }

}
