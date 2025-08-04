package com.switcherapi.client;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.model.ContextKey;
import org.junit.jupiter.api.Test;

import static com.switcherapi.client.remote.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SwitcherContextBuilderDefaultsTest {

    @Test
    void shouldLoadDefault_environment() {
        //given
        SwitchersBase.configure(ContextBuilder.builder(true).environment(null));

        //test
        assertEquals(DEFAULT_ENV, SwitchersBase.contextStr(ContextKey.ENVIRONMENT));
    }

    @Test
    void shouldLoadDefaults() {
        //given
        SwitchersBase.configure(ContextBuilder.builder(true));

        //test
        assertEquals(DEFAULT_REGEX_TIMEOUT, SwitchersBase.contextInt(ContextKey.REGEX_TIMEOUT));
        assertEquals(DEFAULT_TIMEOUT, SwitchersBase.contextInt(ContextKey.TIMEOUT_MS));
        assertEquals(DEFAULT_ENV, SwitchersBase.contextStr(ContextKey.ENVIRONMENT));
    }

}
