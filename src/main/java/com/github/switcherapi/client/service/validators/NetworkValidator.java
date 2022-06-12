package com.github.switcherapi.client.service.validators;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.criteria.Strategy;

@ValidatorComponent(type = Entry.NETWORK)
public class NetworkValidator extends Validator {
	
	public static final String CIDR_REGEX = "^(\\d{1,3}\\.){3}\\d{1,3}(\\/(\\d|[1-2]\\d|3[0-2]))";
	
	@Override
	public boolean process(final Strategy strategy, final Entry switcherInput) {
		switch (strategy.getOperation()) {
		case Entry.EXIST:
			return verifyIfAddressExistInNetwork(strategy, switcherInput);
		case Entry.NOT_EXIST:
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
