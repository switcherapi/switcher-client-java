package com.github.switcherapi.client.service.validators;

import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;

import com.github.switcherapi.client.exception.SwitcherInvalidNumericFormat;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.criteria.Strategy;

@ValidatorComponent(type = StrategyValidator.NUMERIC)
public class NumericValidator extends Validator {
	
	@Override
	public boolean process(final Strategy strategy, final Entry switcherInput) {
		if (!NumberUtils.isCreatable(switcherInput.getInput()))
			throw new SwitcherInvalidNumericFormat(switcherInput.getInput());
		
		switch (strategy.getEntryOperation()) {
		case EXIST:
			return Arrays.stream(strategy.getValues()).anyMatch(val -> val.equals(switcherInput.getInput()));
		case NOT_EXIST:
			return Arrays.stream(strategy.getValues()).noneMatch(val -> val.equals(switcherInput.getInput()));
		case EQUAL:
			return strategy.getValues().length == 1 && strategy.getValues()[0].equals(switcherInput.getInput());
		case NOT_EQUAL:
			return strategy.getValues().length == 1 && !strategy.getValues()[0].equals(switcherInput.getInput());
		case LOWER:
			if (strategy.getValues().length == 1) {
				final double numericInput = NumberUtils.createNumber(switcherInput.getInput()).doubleValue();
				final double numericValue = NumberUtils.createNumber(strategy.getValues()[0]).doubleValue();
				return numericInput < numericValue;
			}
			break;
		case GREATER:
			if (strategy.getValues().length == 1) {
				final double numericInput = NumberUtils.createNumber(switcherInput.getInput()).doubleValue();
				final double numericValue = NumberUtils.createNumber(strategy.getValues()[0]).doubleValue();
				return numericInput > numericValue;
			}
			break;
		case BETWEEN:
			if (strategy.getValues().length == 2) {
				final double numericInput = NumberUtils.createNumber(switcherInput.getInput()).doubleValue();
				final double numericFirstValue = NumberUtils.createNumber(strategy.getValues()[0]).doubleValue();
				final double numericSecondValue = NumberUtils.createNumber(strategy.getValues()[1]).doubleValue();
				return numericInput >= numericFirstValue && numericFirstValue <= numericSecondValue;
			}
			break;
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
		
		throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
	}
	
}
