package com.switcherapi.client.utils;

import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.client.model.SwitcherResult;
import com.switcherapi.client.remote.dto.CriteriaRequest;
import com.switcherapi.client.remote.dto.CriteriaResponse;

public class Mapper {

	private Mapper() {}

	public static CriteriaRequest mapFrom(final SwitcherRequest switcher) {
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
