package com.github.switcherapi.client.test;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate test cases with the Switcher Key and the expected result.
 * 
 * <p>
 * <b>Requires JUnit 5 Jupiter</b>
 * 
 * @author Roger Floriano (petruki)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(SwitcherTestExtension.class)
@TestTemplate
public @interface SwitcherTest {

	String key() default "";

	boolean result() default true;

	String metadata() default "";

	boolean abTest() default false;

	SwitcherTestValue[] switchers() default {};

}
