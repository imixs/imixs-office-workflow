package org.imixs.workflow.office.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * Helper CDI Bean to provide version information used in the footer page
 * layout/extra.xhtml
 */
@Named("buildInfo")
@ApplicationScoped
public class BuildInfoBean {

    private static final Logger logger = Logger.getLogger(BuildInfoBean.class.getName());

    private String version;
    private String timestamp;
    private String officeVersion;
    private String martyVersion;
    private String workflowVersion;
    private String name;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("build.properties")) {
            if (input != null) {
                props.load(input);
                this.version = props.getProperty("build.version", "unknown");
                this.timestamp = props.getProperty("build.timestamp", "unknown");
                this.officeVersion = props.getProperty("build.office.version", "");
                this.martyVersion = props.getProperty("build.marty.version", "");
                this.workflowVersion = props.getProperty("build.workflow.version", "unknown");
                this.name = props.getProperty("build.name", "unknown");
            }
        } catch (IOException e) {
            logger.warning("Could not load build.properties: " + e.getMessage());
        }

    }

    // Getters
    public String getVersion() {
        return version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getOfficeVersion() {
        return officeVersion;
    }

    public String getMartyVersion() {
        return martyVersion;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getName() {
        return name;
    }
}
