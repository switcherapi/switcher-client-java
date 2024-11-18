package com.github.switcherapi.client.validator;

import com.github.switcherapi.client.model.criteria.Strategy;
import com.github.switcherapi.client.service.ValidatorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorsTest {
	
	private final ValidatorService service = new ValidatorService();
	
	@Test
	void shouldRegisterCustomValidator() {
		assertDoesNotThrow(() -> service.registerValidator(new CustomValidator()));
		Strategy strategy = new Strategy();
		strategy.setStrategy("CUSTOM");
		
		assertTrue(service.execute(strategy, null));
	}

}
