package com.github.switcherapi.client.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.switcherapi.client.exception.SwitcherException;

public class SwitcherClassLoader<T> {
	
	private static final String CLASS_PACKAGE_PATTERN = "%s.%s";

	/**
	 * Find all classes by type under the same package
	 * 
	 * @param type Class reference for type return and package location
	 * @return Set of non-annotation classes under the same package
	 */
	public Set<Class<? extends T>> findClassesByType(final Class<T> type) {
		final String packageName = type.getPackage().getName();
		InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		return reader.lines()
				.filter(line -> line.endsWith(".class"))
				.map(line -> getClass(line, packageName, type))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private Class<? extends T> getClass(String className, String packageName, Class<T> type) {
		try {
			final Class<?> clazz = Class.forName(getClassName(packageName, className));
			if (!clazz.isAnnotation() && type.getName().equals(clazz.getGenericSuperclass().getTypeName()))
				return clazz.asSubclass(type);
		} catch (ClassNotFoundException e) {
			throw new SwitcherException(e.getMessage(), e);
		}

		return null;
	}
	
	private String getClassName(String packageName, String className) {
		return String.format(CLASS_PACKAGE_PATTERN, 
				packageName, className.substring(0, className.lastIndexOf('.')));
	}

}
