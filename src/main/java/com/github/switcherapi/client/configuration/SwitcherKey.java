package com.github.switcherapi.client.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines Switchers Keys that can be used.
 * 
 * @author Roger Floriano (petruki)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SwitcherKey {
}
