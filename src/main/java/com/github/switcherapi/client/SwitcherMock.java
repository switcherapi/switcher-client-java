package com.github.switcherapi.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Annotate test cases with the Switcher Key and the expected result.
 * 
 * <p>
 * <b>Requires JUnit 5 Jupiter @ParameterizedTest</b>
 * 
 * @author Roger Floriano (petruki)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ArgumentsSource(SwitcherMockExtension.class)
@ExtendWith(SwitcherMockExtension.class)
@ParameterizedTest
public @interface SwitcherMock {

	String key() default "";

	boolean result() default true;

	SwitcherMockValue[] switchers() default {};

}
