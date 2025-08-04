package com.switcherapi.client.remote;

public final class Constants {

	public static final String DEFAULT_ENV = "default";
	public static final Integer DEFAULT_REGEX_TIMEOUT = 3000;
	public static final int DEFAULT_TIMEOUT = 3000;
	public static final int DEFAULT_POOL_SIZE = 2;
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_APIKEY = "switcher-api-key";
	public static final String TOKEN_TEXT = "Bearer %s";

	public static final String QUERY =
			"{\"query\":\"{ domain(name: \\\"%s\\\", environment: \\\"%s\\\", _component: \\\"%s\\\") { " +
					"name version description activated " +
					"group { name description activated " +
					"config { key description activated " +
					"strategies { strategy activated operation values } " +
					"relay { type activated } " +
					"components } } } }\"}";

	private Constants() {}
}
