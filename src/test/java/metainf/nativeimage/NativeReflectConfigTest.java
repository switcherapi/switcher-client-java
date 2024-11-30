package metainf.nativeimage;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeReflectConfigTest {

	private static final Path reflectPath = Path.of("src/main/resources/META-INF/native-image/com.github.switcherapi/switcher-client/reflect-config.json");

	private static String reflectContent;

	@BeforeAll
	static void readReflectConfig() throws Exception {
		reflectContent = new String(Files.readAllBytes(reflectPath));
	}

	@Test
	void shouldReadReflectConfig() {
		assertTrue(reflectContent.contains("com.github.switcherapi.client"));
	}

	@Test
	void shouldParseReflectConfig() {
		ReflectJson[] reflectJson = new Gson().fromJson(reflectContent, ReflectJson[].class);
		assertTrue(reflectJson.length > 0);
	}

	@Test
	void shouldApplyReflection() {
		ReflectJson[] reflectJson = new Gson().fromJson(reflectContent, ReflectJson[].class);

		for (ReflectJson json : reflectJson) {
			assertDoesNotThrow(() -> Class.forName(json.name),
					String.format("Class %s not reachable", json.name));

			if (Objects.nonNull(json.condition)) {
				assertDoesNotThrow(() -> Class.forName(json.condition.typeReachable),
						String.format("Class %s not reachable", json.condition.typeReachable));
			}
		}
	}

	static class ReflectJson {
		String name;
		ReflectCondition condition;
	}

	static class ReflectCondition {
		String typeReachable;
	}
}
