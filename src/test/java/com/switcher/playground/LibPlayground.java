package com.switcher.playground;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.switcher.client.SwitcherFactory;
import com.switcher.client.domain.Switcher;
import com.switcher.client.utils.SwitcherContextParam;

public class LibPlayground {
	
	final static Logger logger = Logger.getLogger(LibPlayground.class);
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources/";
	
	public LibPlayground() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, "http://localhost:3000/criteria");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$S2Wj/wG/Rfs3ij0xFbtgveDtyUAjML1/TOOhocDg5dhOaU73CEXfK");
		properties.put(SwitcherContextParam.DOMAIN, "currency-api");
		properties.put(SwitcherContextParam.COMPONENT, "Android");
		properties.put(SwitcherContextParam.ENVIRONMENT, "default");
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		properties.put(SwitcherContextParam.SILENT_MODE, false);
		properties.put(SwitcherContextParam.RETRY_AFTER, "5s");

		SwitcherFactory.buildContext(properties, true);
		try {
			Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
			logger.info(switcher.isItOn());
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public static void main(String[] args) {
		new LibPlayground();
	}
}