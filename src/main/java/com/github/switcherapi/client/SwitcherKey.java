package com.github.switcherapi.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines Switchers Keys that can be used.
 * 
 * <p>
 * The best way to define a Switcher Key in your Context class is: 
 * <br>
 * public static final String SWITCHER_KEY = "SWITCHER_KEY"
 * 
 * <p>
 * The attribute name is used to be sent to the API and its value
 * is used to work with {@link SwitcherMock}
 * 
 * @author Roger Floriano (petruki)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SwitcherKey {
}
