package com.github.switcherapi.playground;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.SwitcherFactory;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.utils.SwitcherContextParam;

public class LibPlayground {
	
	final static Logger logger = LogManager.getLogger(LibPlayground.class);
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	public LibPlayground() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, "http://localhost:3000");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$e6EUI0358sk5QBAZlxrBF.Eg5jwhLSRfoKLvCAcbctCnKMoqcM5Fi");
		properties.put(SwitcherContextParam.DOMAIN, "My Domain");
		properties.put(SwitcherContextParam.COMPONENT, "CustomerAPI");
		properties.put(SwitcherContextParam.ENVIRONMENT, "default");
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL);
		properties.put(SwitcherContextParam.SNAPSHOT_AUTO_LOAD, true);
		properties.put(SwitcherContextParam.SILENT_MODE, false);
		properties.put(SwitcherContextParam.RETRY_AFTER, "5s");

		try {
			SwitcherFactory.buildContext(properties, true);
			Switcher switcher = SwitcherFactory.getSwitcher("FEATURE01");
			logger.info(switcher.isItOn());
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public static void main(String[] args) {
		new LibPlayground();
	}
}