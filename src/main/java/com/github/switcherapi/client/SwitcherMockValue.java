package com.github.switcherapi.client;

public @interface SwitcherMockValue {

    String key();

    boolean result() default true;

}
