package com.switcherapi.client.service.validators;

import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.Strategy;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

public class NetworkValidator extends Validator {
	
	public static final String CIDR_REGEX = "^(\\d{1,3}\\.){3}\\d{1,3}(/(\\d|[1-2]\\d|3[0-2]))";

	@Override
	public StrategyValidator getType() {
		return StrategyValidator.NETWORK;
	}
	
	@Override
	public boolean process(final Strategy strategy, final Entry switcherInput) {
		switch (strategy.getEntryOperation()) {
		case EXIST:
			return verifyIfAddressExistInNetwork(strategy, switcherInput);
		case NOT_EXIST:
			return !verifyIfAddressExistInNetwork(strategy, switcherInput);
		default:
			throw new SwitcherInvalidOperationException(strategy.getOperation(), strategy.getStrategy());
		}
	}
	
	private boolean verifyIfAddressExistInNetwork(final Strategy strategy, final Entry switcherInput) {
		SubnetInfo subnetInfo;
		for (final String value : strategy.getValues()) {
			if (value.matches(CIDR_REGEX)) {
				subnetInfo = new SubnetUtils(value).getInfo();

				if (subnetInfo.isInRange(switcherInput.getInput())) {
					return true;
				}
			} else {
				if (value.equals(switcherInput.getInput())) {
					return true;
				}
			}
		}
		
		return false;
	}

}
