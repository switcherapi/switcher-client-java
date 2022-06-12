package com.github.switcherapi.client.validator;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.service.ValidatorService;

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
		assertThrows(Exception.class, () -> service.registerValidator(InvalidCustomValidator.class));
	}

}
