package com.switcherapi.client.validator;

import com.switcherapi.client.model.criteria.Strategy;
import com.switcherapi.client.service.ValidatorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorsTest {
	
	private final ValidatorService service = new ValidatorService();
	
	@Test
	void shouldRegisterCustomValidator() {
		assertDoesNotThrow(() -> service.registerValidator(new CustomValidator()));
		Strategy strategy = new Strategy(
				"CUSTOM",
				"INVALID",
				"Custom Validator Test",
				true,
				new String[] { "Value1", "Value2" }
		);
		
		assertTrue(service.execute(strategy, null));
	}

}
