package com.github.switcherapi.client.utils;

import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.SwitcherResult;
import com.github.switcherapi.client.remote.dto.CriteriaRequest;
import com.github.switcherapi.client.remote.dto.CriteriaResponse;

public class Mapper extends Utils {

	public static CriteriaRequest mapFrom(final Switcher switcher) {
		CriteriaRequest request = new CriteriaRequest();
		request.setSwitcherKey(switcher.getSwitcherKey());
		request.setEntry(switcher.getEntry());
		request.setBypassMetric(switcher.isBypassMetrics());
		return request;
	}

	public static SwitcherResult mapFrom(final CriteriaResponse criteriaResponse) {
		SwitcherResult switcherResult = new SwitcherResult();
		switcherResult.setSwitcherKey(criteriaResponse.getSwitcherKey());
		switcherResult.setResult(criteriaResponse.getResult());
		switcherResult.setReason(criteriaResponse.getReason());
		switcherResult.setMetadata(criteriaResponse.getMetadata());
		return switcherResult;
	}
}
