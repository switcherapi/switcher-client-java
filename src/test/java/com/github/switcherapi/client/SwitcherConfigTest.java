package com.github.switcherapi.client;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static com.github.switcherapi.SwitchersBase.USECASE11;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SwitcherConfigTest {

	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";

	@Test
	void shouldInitializeClientFromSwitcherConfig_Simple() {
		String component = "switcher-client-simple";
		String domain = "switcher-domain-simple";
		SwitchersBase config = buildSwitcherClientConfig(new SwitchersBase(), component, domain);
		config.configureClient();

		assertTrue(SwitchersBase.contextBol(ContextKey.LOCAL_MODE));
		assertEquals(component, SwitchersBase.contextStr(ContextKey.COMPONENT));
		assertEquals(domain, SwitchersBase.contextStr(ContextKey.DOMAIN));
	}

	@Test
	void shouldInitializeClientFromSwitcherConfig_Minimal() {
		SwitchersBase config = buildSwitcherClientConfigMinimal(new SwitchersBase());
		config.configureClient();

		assertTrue(SwitchersBase.contextBol(ContextKey.LOCAL_MODE));
		assertTrue(SwitchersBase.getSwitcher(USECASE11).isItOn());
	}

	private <T extends SwitcherConfig> T buildSwitcherClientConfig(T classConfig, String component,
																   String domain) {
		SwitcherConfig.SnapshotConfig snapshot = new SwitcherConfig.SnapshotConfig();
		snapshot.setLocation(SNAPSHOTS_LOCAL);
		snapshot.setUpdateInterval(null);
		snapshot.setSkipValidation(false);
		snapshot.setAuto(false);

		SwitcherConfig.TruststoreConfig truststore = new SwitcherConfig.TruststoreConfig();
		truststore.setPath(null);
		truststore.setPassword(null);

		classConfig.setContextLocation(SwitchersBase.class.getName());
		classConfig.setUrl("http://localhost:3000");
		classConfig.setApikey("[API-KEY]");
		classConfig.setComponent(component);
		classConfig.setDomain(domain);
		classConfig.setEnvironment("fixture1");
		classConfig.setLocal(true);
		classConfig.setSilent("5m");
		classConfig.setTimeout(3000);
		classConfig.setPoolSize(2);
		classConfig.setSnapshot(snapshot);
		classConfig.setTruststore(truststore);
		return classConfig;
	}

	private <T extends SwitcherConfig> T buildSwitcherClientConfigMinimal(T classConfig) {
		SwitcherConfig.SnapshotConfig snapshot = new SwitcherConfig.SnapshotConfig();
		snapshot.setLocation(SNAPSHOTS_LOCAL);

		classConfig.setContextLocation(SwitchersBase.class.getName());
		classConfig.setEnvironment("fixture1");
		classConfig.setLocal(true);
		classConfig.setSnapshot(snapshot);
		return classConfig;
	}

}
