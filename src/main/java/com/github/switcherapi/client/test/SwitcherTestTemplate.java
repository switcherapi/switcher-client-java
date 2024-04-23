package com.github.switcherapi.client.test;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.Arrays;

class SwitcherTestTemplate implements TestTemplateInvocationContext {

    private static final String DISPLAY_NAME_TEMPLATE = "With %s as %s";

    private final SwitcherTest switcherTest;

    private boolean inverted;

    SwitcherTestTemplate(SwitcherTest switcherTest, boolean inverted) {
        this.switcherTest = switcherTest;
        this.inverted = inverted;
    }

    SwitcherTestTemplate(SwitcherTest switcherTest) {
        this.switcherTest = switcherTest;
    }

    @Override
    public String getDisplayName(int invocationIndex) {
        SwitcherTestValue[] switcherTestValues = switcherTest.switchers();

        if (ArrayUtils.isNotEmpty(switcherTestValues)) {
            return String.join(", ", Arrays.toString(
                    Arrays.stream(switcherTestValues)
                            .map(value -> String.format(DISPLAY_NAME_TEMPLATE, value.key(), inverted != value.result()))
                            .toArray()));
        }

        return String.format(DISPLAY_NAME_TEMPLATE, switcherTest.key(), inverted != switcherTest.result());
    }
}