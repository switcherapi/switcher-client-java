package com.github.switcherapi.client.test;

public @interface SwitcherTestValue {

    String key();

    boolean result() default true;

}
