package com.github.switcherapi.client.test;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.Arrays;

class SwitcherTestTemplate implements TestTemplateInvocationContext {

    private static final String DISPLAY_NAME_TEMPLATE = "With %s as %s";

    private static final String WHEN_TEMPLATE = "when %s = %s";

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
                            .map(value -> concatWhens(String.format(DISPLAY_NAME_TEMPLATE, value.key(), inverted != value.result()), value.when()))
                            .toArray()));
        }

        String displayName = String.format(DISPLAY_NAME_TEMPLATE, switcherTest.key(), inverted != switcherTest.result());
        return concatWhens(displayName, switcherTest.when());
    }

    private String concatWhens(String displayName, SwitcherTestWhen[] switcherTestWhens) {
        if (ArrayUtils.isNotEmpty(switcherTestWhens)) {
            return displayName + " - " + Arrays.toString(
                    Arrays.stream(switcherTestWhens)
                            .map(when -> String.format(WHEN_TEMPLATE, when.strategy(), Arrays.toString(when.input())))
                            .toArray());
        }
        return displayName;
    }
}