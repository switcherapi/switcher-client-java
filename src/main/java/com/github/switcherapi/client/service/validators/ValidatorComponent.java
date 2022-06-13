package com.github.switcherapi.client.service.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.switcherapi.client.model.StrategyValidator;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatorComponent {
	
	StrategyValidator type();
	
}
