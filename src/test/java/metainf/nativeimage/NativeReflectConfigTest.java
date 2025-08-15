package metainf.nativeimage;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeReflectConfigTest {

	private static final Path reflectPath = Path.of("src/main/resources/META-INF/native-image/com.switcherapi/switcher-client/reflect-config.json");

	private static String reflectContent;

	@BeforeAll
	static void readReflectConfig() throws Exception {
		reflectContent = new String(Files.readAllBytes(reflectPath));
	}

	@Test
	void shouldReadReflectConfig() {
		assertTrue(reflectContent.contains("com.switcherapi.client"));
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
			assertConstructorsReachable(json);

			if (Objects.nonNull(json.condition)) {
				assertDoesNotThrow(() -> Class.forName(json.condition.typeReachable),
						String.format("Class %s not reachable", json.condition.typeReachable));
			}
		}
	}

	private void assertConstructorsReachable(ReflectJson json) {
		if (Objects.nonNull(json.methods)) {
			for (ReflectMethod method : json.methods) {
				if ("<init>".equals(method.name)) {
					assertDoesNotThrow(() -> Class.forName(json.name).getDeclaredConstructor(getArgClasses(method.parameterTypes)),
							String.format("Constructor (%s) not reachable in class %s",
									String.join(", ", method.parameterTypes), json.name));
				}
			}
		}
	}

	private Class<?>[] getArgClasses(String[] parameterTypes) throws ClassNotFoundException {
		if (parameterTypes == null || parameterTypes.length == 0) {
			return new Class<?>[0];
		}

		Class<?>[] classes = new Class<?>[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			String paramType = parameterTypes[i];
			if (paramType.endsWith("[]")) {
				String baseType = paramType.substring(0, paramType.length() - 2);
				Class<?> baseClass = getPrimitiveOrClass(baseType);
				classes[i] = Array.newInstance(baseClass, 0).getClass();
			} else {
				classes[i] = getPrimitiveOrClass(paramType);
			}
		}

		return classes;
	}

	private Class<?> getPrimitiveOrClass(String typeName) throws ClassNotFoundException {
		switch (typeName) {
			case "boolean": return boolean.class;
			case "byte": return byte.class;
			case "char": return char.class;
			case "short": return short.class;
			case "int": return int.class;
			case "long": return long.class;
			case "float": return float.class;
			case "double": return double.class;
			case "void": return void.class;
			default: return Class.forName(typeName);
		}
	}

	static class ReflectJson {
		String name;
		ReflectCondition condition;
		ReflectMethod[] methods;
	}

	static class ReflectCondition {
		String typeReachable;
	}

	static class ReflectMethod {
		String name;
		String[] parameterTypes;
	}
}
