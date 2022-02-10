# keycloak-event-listener-http

A Keycloak SPI that publishes events to an HTTP Webhook.
A combination of  @jessylenne/keycloak-event-listener-http SPI and the keycloak-event-listener-rabbitmq SPI.
Extended by @darrensapalo to [enable building the JAR files from docker images](https://sapalo.dev/2021/06/16/send-keycloak-webhook-events/).

# Build

## Build on your local machine

```
mvn clean install
```

## Build using docker

Alternatively, you can [build the JAR files from a docker image](https://sapalo.dev/2021/06/16/send-keycloak-webhook-events/). You must have `docker` installed.

1. Run `make package-image`.
2. The JAR files should show up on your `mvn-output` folder.

If you encounter the following issue: 
```
open {PATH}/mvn-output/event-listener-http-jar-with-dependencies.jar: permission denied
```

Simply add write permissions to the `mvn-output` folder:

```
sudo chown $USER:$USER mvn-output
```

# Deploy

* Copy target/event-listener-http-jar-with-dependencies.jar to {KEYCLOAK_HOME}/standalone/deployments

# Configuration

## Option 1
* Configure the following env variables :

    - HTTP_EVENT_SERVERURI - default: http://127.0.0.1:8080/webhook
    - HTTP_EVENT_USERNAME - default: keycloak
    - HTTP_EVENT_PASSWORD - default: keycloak

* Restart the keycloak server.
## Option 2
* Edit standalone.xml to configure the Webhook settings. Find the following
  section in the configuration:

```
<subsystem xmlns="urn:jboss:domain:keycloak-server:1.1">
    <web-context>auth</web-context>
```

And add below:

```
<spi name="eventsListener">
    <provider name="mqtt" enabled="true">
        <properties>
            <property name="serverUri" value="http://127.0.0.1:8080/webhook"/>
            <property name="username" value="auth_user"/>
            <property name="password" value="auth_password"/>
        </properties>
    </provider>
</spi>
```

Leave username and password out if the service allows anonymous access.

* Restart the keycloak server.

# Usage
Add/Update a user, your webhook should be called, looks at the keycloak syslog for debug

Request example
```
{
{
  '@class': 'org.softwarefactory.keycloak.providers.events.http.AdminEventNotification',
  id: null,
  time: 1644281802199,
  realmId: 'github',
  authDetails: {
    realmId: 'master',
    clientId: '0caa5a71-c84c-461f-94ee-7b862ad47608',
    userId: '8f8b083b-9cca-45eb-a839-99cd41ae77d9',
    ipAddress: '172.0.0.1'
  },
  resourceType: 'USER',
  operationType: 'UPDATE',
  resourcePath: 'users/d57594cd-7425-492f-a3a7-4576d51ea745',
  representation: '{"id":"d57594cd-7425-492f-a3a7-4576d51ea745","createdTimestamp":1642815331413,"username":"abdoulaye.traore","enabled":true,"totp":false,"emailVerified":true,"firstName":"firstN me","lastName":"lastName","email":"aktraore@github.com","attributes":{},"disableableCredentialTypes":[],"requiredActions":["UPDATE_PASSWORD"],"notBefore":0,"access":{"manageGroupMembership":true,"view":true,"mapRoles":true,"impersonate":true,"manage":true}}',
  error: null,
  resourceTypeAsString: 'USER'
}
}
```
