package com.github.switcherapi.client;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.model.ContextKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwitcherContextBuilderDefaultsTest {

    @Test
    void shouldLoadDefault_environment() {
        //given
        SwitchersBase.configure(ContextBuilder.builder(true).environment(null));

        //test
        assertEquals(SwitcherProperties.DEFAULT_ENV, SwitchersBase.contextStr(ContextKey.ENVIRONMENT));
    }

    @Test
    void shouldLoadDefault_regexTimeout() {
        //given
        SwitchersBase.configure(ContextBuilder.builder(true).regexTimeout(""));

        //test
        assertEquals(SwitcherProperties.DEFAULT_REGEX_TIMEOUT, SwitchersBase.contextStr(ContextKey.REGEX_TIMEOUT));
    }

}
