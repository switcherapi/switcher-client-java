package com.switcherapi.client.service.validators;

import com.switcherapi.client.exception.SwitcherInvalidNumericFormat;
import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.StrategyConfig;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;

public class NumericValidator extends Validator {

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.NUMERIC;
	}
	
	@Override
	public boolean process(final StrategyConfig strategyConfig, final Entry switcherInput) {
		if (!NumberUtils.isCreatable(switcherInput.getInput()))
			throw new SwitcherInvalidNumericFormat(switcherInput.getInput());
		
		switch (strategyConfig.getEntryOperation()) {
		case EXIST:
			return Arrays.stream(strategyConfig.getValues()).anyMatch(val -> val.equals(switcherInput.getInput()));
		case NOT_EXIST:
			return Arrays.stream(strategyConfig.getValues()).noneMatch(val -> val.equals(switcherInput.getInput()));
		case EQUAL:
			return strategyConfig.getValues().length == 1 && strategyConfig.getValues()[0].equals(switcherInput.getInput());
		case NOT_EQUAL:
			return strategyConfig.getValues().length == 1 && !strategyConfig.getValues()[0].equals(switcherInput.getInput());
		case LOWER:
			if (strategyConfig.getValues().length == 1) {
				final double numericInput = NumberUtils.createNumber(switcherInput.getInput()).doubleValue();
				final double numericValue = NumberUtils.createNumber(strategyConfig.getValues()[0]).doubleValue();
				return numericInput < numericValue;
			}
			break;
		case GREATER:
			if (strategyConfig.getValues().length == 1) {
				final double numericInput = NumberUtils.createNumber(switcherInput.getInput()).doubleValue();
				final double numericValue = NumberUtils.createNumber(strategyConfig.getValues()[0]).doubleValue();
				return numericInput > numericValue;
			}
			break;
		case BETWEEN:
			if (strategyConfig.getValues().length == 2) {
				final double numericInput = NumberUtils.createNumber(switcherInput.getInput()).doubleValue();
				final double numericFirstValue = NumberUtils.createNumber(strategyConfig.getValues()[0]).doubleValue();
				final double numericSecondValue = NumberUtils.createNumber(strategyConfig.getValues()[1]).doubleValue();
				return numericInput >= numericFirstValue && numericFirstValue <= numericSecondValue;
			}
			break;
		default:
			throw new SwitcherInvalidOperationException(strategyConfig.getOperation(), strategyConfig.getStrategy());
		}
		
		throw new SwitcherInvalidOperationException(strategyConfig.getOperation(), strategyConfig.getStrategy());
	}
	
}
