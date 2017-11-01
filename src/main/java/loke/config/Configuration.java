package loke.config;

import loke.model.Admin;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private boolean dryRun;
    private String accessKey;
    private String secretAccessKey;
    private String region;
    private String host;
    private int port;
    private String stagingDir;
    private String userOwnerRegExp;
    private String fromEmailAddress;
    private String toEmailDomainName;
    private double showAccountThreshold;
    private List<Admin> admins;

    public boolean isDryRun() {
        return dryRun;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public String getRegion() {
        return region;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getStagingDir() {
        return stagingDir;
    }

    public String getUserOwnerRegExp() {
        return userOwnerRegExp;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public String getToEmailDomainName() {
        return toEmailDomainName;
    }

    public double getShowAccountThreshold() {
        return showAccountThreshold;
    }

    public List<Admin> getAdmins() {
        if (this.admins != null) {
            return admins;
        }
        return new ArrayList<>();
    }
}
