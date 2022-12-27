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

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import org.apache.http.HttpHeaders;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.apache.commons.codec.binary.Base64;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author <a href="mailto:traore_a@outlook.com">Abdoulaye Traore</a>
 */
public class HTTPEventListenerProvider implements EventListenerProvider {

	private final OkHttpClient httpClient = new OkHttpClient();
    private Set<EventType> excludedEvents;
    private Set<OperationType> excludedAdminOperations;
    private Set<String> serverUri;
    private String username;
    private String password;

    private KeycloakSession session;
    
	private EventListenerTransaction tx = new EventListenerTransaction(this::publishAdminEvent, this::publishEvent);

    public HTTPEventListenerProvider(Set<EventType> excludedEvents, Set<OperationType> excludedAdminOperations, Set<String> serverUri, String username, String password, KeycloakSession session) {
        this.excludedEvents = excludedEvents;
        this.excludedAdminOperations = excludedAdminOperations;
        this.serverUri = serverUri;
        this.username = username;
        this.password = password;

		this.session = session;
		this.session.getTransactionManager().enlistAfterCompletion(tx);
    }

    @Override
    public void onEvent(Event event) {
        // Ignore excluded events
        if (excludedEvents != null && excludedEvents.contains(event.getType())) {
            return;
        }
        tx.addEvent(event);
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // Ignore excluded operations
        if (excludedAdminOperations != null && excludedAdminOperations.contains(adminEvent.getOperationType())) {
            return;
        }
        tx.addAdminEvent(adminEvent, includeRepresentation);
    }

    public void publishEvent(Event event) {
        ClientEventNotification notification = ClientEventNotification.create(event);
        String notificationAsString = HTTPEventConfiguration.writeAsJson(notification, false);
        this.sendEvent(notificationAsString);
    }

    public void publishAdminEvent(AdminEvent event, boolean includeRepresentation) {
        AdminEventNotification notification = AdminEventNotification.create(event);
        String notificationAsString = HTTPEventConfiguration.writeAsJson(notification, false);
        this.sendEvent(notificationAsString);
    }

    private void sendEvent(String event) {
        try {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody formBody = RequestBody.create(event, JSON);

            for (String serverUri : this.serverUri) {
                Request request = new Request.Builder()
                        .url(serverUri)
                        .addHeader(HttpHeaders.AUTHORIZATION, getAuthorizationBasicToken())
                        .addHeader("User-Agent", "KeycloakHttp Bot")
                        .post(formBody)
                        .build();

                Response response = httpClient.newCall(request).execute();

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Get response body
                System.out.println(Objects.requireNonNull(response.body()).string());
            }
        } catch(Exception e) {
            System.out.println("An error occured while sending event : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getAuthorizationBasicToken() {
        Base64 base64 = new Base64();
        String valueToEncode = this.username + ":" + this.password;
        byte[] encodedBytes = base64.encode(valueToEncode.getBytes());
        return "Basic " + new String(encodedBytes);
    }


    @Override
    public void close() {
    }

}
