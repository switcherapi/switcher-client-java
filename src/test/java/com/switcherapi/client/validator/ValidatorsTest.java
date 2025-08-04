package com.switcherapi.client.validator;

import com.switcherapi.client.model.criteria.StrategyConfig;
import com.switcherapi.client.service.ValidatorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorsTest {
	
	private final ValidatorService service = new ValidatorService();
	
	@Test
	void shouldRegisterCustomValidator() {
		assertDoesNotThrow(() -> service.registerValidator(new CustomValidator()));
		StrategyConfig strategyConfig = new StrategyConfig(
				"CUSTOM",
				"INVALID",
				"Custom Validator Test",
				true,
				new String[] { "Value1", "Value2" }
		);
		
		assertTrue(service.execute(strategyConfig, null));
	}

}
