/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.softwarefactory.keycloak.providers.events.http;


import org.keycloak.Config.Scope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:traore_a@outlook.com">Abdoulaye Traore</a>
 */
public class HTTPEventConfiguration {

	private static final String PREFIX_CONFIGURATION = "HTTP_EVENT_";
	private String serverUri;
	private String username;
	private String password;

	public static final ObjectMapper httpEventConfigurationObjectMapper = new ObjectMapper();
	
	public static HTTPEventConfiguration createFromScope(Scope config) {
		HTTPEventConfiguration configuration = new HTTPEventConfiguration();
		
		configuration.serverUri = resolveConfigVar(config, "serverUri", "http://127.0.0.1:8080/webhook");
		configuration.username = resolveConfigVar(config, "username", "keycloak");
		configuration.password = resolveConfigVar(config, "password", "keycloak");

		return configuration;
		
	}

	private static Set<String> resolveConfigVar(Scope config, String variableName) {
		String envVariables = System.getenv(PREFIX_CONFIGURATION + variableName.toUpperCase());
		if (envVariables != null) {
			return Arrays.stream(envVariables.split(","))
					.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}
	
	private static String resolveConfigVar(Scope config, String variableName, String defaultValue) {
		String configVariable = resolveConfigVarIfConfigExists(config, variableName);
		String envVariable = System.getenv(PREFIX_CONFIGURATION + variableName.toUpperCase());
		if (configVariable != null) {
			return configVariable;
		}
		if (envVariable != null) {
			return envVariable;
		}
		return defaultValue;
	}

	private static String resolveConfigVarIfConfigExists(Scope config, String variableName) {
		if (config != null) {
			return config.get(variableName);
		}
		return null;
	}

	public static String writeAsJson(Object object, boolean isPretty) {
		String messageAsJson = "unparsable";
		try {
			if(isPretty) {
				messageAsJson = HTTPEventConfiguration.httpEventConfigurationObjectMapper
						.writerWithDefaultPrettyPrinter().writeValueAsString(object);
			} else {
				messageAsJson = HTTPEventConfiguration.httpEventConfigurationObjectMapper
						.writeValueAsString(object);
			}
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return messageAsJson;
	}
	
	
	
	public String getServerUri() {
		return serverUri;
	}
	public void setServerUri(String serverUri) {
		this.serverUri = serverUri;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	

}
