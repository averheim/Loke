package loke.config;

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

    public Configuration() {
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStagingDir() {
        return stagingDir;
    }

    public void setStagingDir(String stagingDir) {
        this.stagingDir = stagingDir;
    }

    public String getUserOwnerRegExp() {
        return userOwnerRegExp;
    }

    public void setUserOwnerRegExp(String userOwnerRegExp) {
        this.userOwnerRegExp = userOwnerRegExp;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public void setFromEmailAddress(String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }

    public String getToEmailDomainName() {
        return toEmailDomainName;
    }

    public void setToEmailDomainName(String toEmailDomainName) {
        this.toEmailDomainName = toEmailDomainName;
    }
}
