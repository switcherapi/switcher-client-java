package metainf.nativeimage;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class NativeResourceConfigTest {

	private static final Path resourcePath = Paths.get("src/main/resources/META-INF/native-image/com.switcherapi/switcher-client/resource-config.json");

	private static String resourceContent;

	@BeforeAll
	static void readReflectConfig() throws Exception {
		resourceContent = new String(Files.readAllBytes(resourcePath));
	}

	@Test
	void shouldReadResourceConfig() {
		assertTrue(resourceContent.contains("resources"));
	}

	@Test
	void shouldParseResourceConfig() {
		ResourceJson resourceJson = new Gson().fromJson(resourceContent, ResourceJson.class);
		assertTrue(resourceJson.resources.includes.length > 0);
	}

	@Test
	void shouldApplyReflectionFromCondition() {
		ResourceJson resourceJson = new Gson().fromJson(resourceContent, ResourceJson.class);

		for (Include include : resourceJson.resources.includes) {
			assertDoesNotThrow(() -> Class.forName(include.condition.typeReachable),
					String.format("Class %s not reachable", include.condition.typeReachable));
		}
	}

	@Test
	void shouldContainPropertiesConfig() {
		ResourceJson resourceJson = new Gson().fromJson(resourceContent, ResourceJson.class);

		boolean hasProperties = Arrays.stream(resourceJson.resources.includes)
				.anyMatch(include -> include.pattern.contains("properties"));

		assertTrue(hasProperties, "Properties not found in resource config");
	}

	@Test
	void shouldContainsSnapshotJsonConfig() {
		ResourceJson resourceJson = new Gson().fromJson(resourceContent, ResourceJson.class);

		boolean hasSnapshotJson = Arrays.stream(resourceJson.resources.includes)
				.anyMatch(include -> include.pattern.contains("snapshots/"));

		assertTrue(hasSnapshotJson, "snapshots/ structure not found in resource config");
	}

	@Test
	void shouldBeValidPropertiesName() {
		ResourceJson resourceJson = new Gson().fromJson(resourceContent, ResourceJson.class);

		for (Include include : resourceJson.resources.includes) {
			if (include.pattern.contains("properties")) {
				assertPattern(include, new String[] {
						"switcherapi.properties",
						"switcherapidev.properties",
						"switcherapi-dev.properties"
				}, true);
			}
		}
	}

	@Test
	void shouldNotBeValidPropertiesName() {
		ResourceJson resourceJson = new Gson().fromJson(resourceContent, ResourceJson.class);

		for (Include include : resourceJson.resources.includes) {
			if (include.pattern.contains("properties")) {
				assertPattern(include, new String[] {
						"switcher.properties",
						"switcher-dev.properties"
				}, false);
			}
		}
	}

	@Test
	void shouldBeValidSnapshotJsonPathName() {
		ResourceJson resourceJson = new Gson().fromJson(resourceContent, ResourceJson.class);

		for (Include include : resourceJson.resources.includes) {
			if (include.pattern.contains("snapshots/")) {
				assertPattern(include, new String[] {
						"snapshots/default.json",
						"snapshots/production.json",
						"snapshots/env-dev.json"
				}, true);
			}
		}
	}

	@Test
	void shouldNotBeValidSnapshotJsonPathName() {
		ResourceJson resourceJson = new Gson().fromJson(resourceContent, ResourceJson.class);

		for (Include include : resourceJson.resources.includes) {
			if (include.pattern.contains("snapshots/")) {
				assertPattern(include, new String[] {
						"default.json",
						"snapshots/default.yaml",
						"snapshot/default.json"
				}, false);
			}
		}
	}

	private void assertPattern(Include include, String[] input, boolean expected) {
		String pattern = include.pattern;

		boolean isValid = false;
		for (String validProperty : input) {
			if (validProperty.matches(pattern)) {
				isValid = true;
				break;
			}
		}

		if (expected) {
			assertTrue(isValid, String.format("Pattern %s is not valid for properties", pattern));
		} else {
			assertFalse(isValid, String.format("Pattern %s is valid for properties", pattern));
		}
	}

	static class ResourceJson {
		Resources resources;
	}

	static class Resources {
		Include[] includes;
	}

	static class Include {
		String pattern;
		Condition condition;
	}

	static class Condition {
		String typeReachable;
	}

}
