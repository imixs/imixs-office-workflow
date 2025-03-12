# Wildfly 29

Docker Image and Deplomynet issues .

## Eclipselink

Eclipselink module must be configured in custom way.
See: https://stackoverflow.com/questions/75720891/getting-java-lang-noclassdeffounderror-javax-xml-parsers-parserconfigurationex

## Microprofile HealthCheck

microprofile/health/HealthCheck is not part of the default configuration. HealthCheck is needed by the Imixs-Workflow Setup Service!

For this reason we added the module `org.wildfly.extension.microprofile.health-smallrye` :

```xml
        <!-- added microprofile health -->
        <extension module="org.wildfly.extension.microprofile.health-smallrye" />
```

and the configuration for the subsystem

```xml
        <subsystem xmlns="urn:wildfly:microprofile-health-smallrye:3.0" security-enabled="false"
            empty-liveness-checks-status="${env.MP_HEALTH_EMPTY_LIVENESS_CHECKS_STATUS:UP}"
            empty-readiness-checks-status="${env.MP_HEALTH_EMPTY_READINESS_CHECKS_STATUS:UP}"
            empty-startup-checks-status="${env.MP_HEALTH_EMPTY_STARTUP_CHECKS_STATUS:UP}" />
```
