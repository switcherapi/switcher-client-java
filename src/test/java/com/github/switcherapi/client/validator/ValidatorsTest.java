package com.github.switcherapi.client.validator;

import com.github.switcherapi.client.exception.SwitcherInvalidValidatorException;
import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.service.ValidatorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorsTest {
	
	private final ValidatorService service = new ValidatorService();
	
	@Test
	void shouldRegisterCustomValidator() {
		assertDoesNotThrow(() -> service.registerValidator(CustomValidator.class));
		Strategy strategy = new Strategy();
		strategy.setStrategy("CUSTOM");
		
		assertTrue(service.execute(strategy, null));
	}
	
	@Test
	void shouldNotRegisterCustomValidator() {
		assertThrows(Exception.class,
				() -> service.registerValidator(InvalidCustomValidator.class));
	}

	@Test
	void shouldNotRegisterNotAnnotatedCustomValidator() {
		assertThrows(SwitcherInvalidValidatorException.class,
				() -> service.registerValidator(InvalidCustom2Validator.class));
	}

}
