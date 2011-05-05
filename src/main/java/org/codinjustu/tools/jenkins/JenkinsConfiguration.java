package org.codinjustu.tools.jenkins;

public class JenkinsConfiguration {

    public static final String DEFAULT_JENKINS_SERVER_URL = "http://dummyjenkinsserver";
    public static final int DEFAULT_BUILD_DELAY = 0;
    public static final int RESET_VALUE = 0;

    private String serverUrl = DEFAULT_JENKINS_SERVER_URL;
    private int delay = DEFAULT_BUILD_DELAY;
    private int jobRefreshPeriod = RESET_VALUE;

    private int rssRefreshPeriod = RESET_VALUE;

    private boolean enableJobAutoRefresh = false;

    private boolean enableRssAutoRefresh = false;

    private String preferredView = "";


    public String getServerUrl() {
        return serverUrl;
    }


    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    public boolean isServerUrlSet() {
        return serverUrl != null && serverUrl.length() != 0 &&
                !JenkinsConfiguration.DEFAULT_JENKINS_SERVER_URL.equals(serverUrl);
    }


    public int getBuildDelay() {
        return delay;
    }


    public void setDelay(int delay) {
        this.delay = delay;
    }


    public int getJobRefreshPeriod() {
        return jobRefreshPeriod;
    }


    public void setJobRefreshPeriod(int jobRefreshPeriod) {
        this.jobRefreshPeriod = jobRefreshPeriod;
    }


    public int getRssRefreshPeriod() {
        return rssRefreshPeriod;
    }


    public void setRssRefreshPeriod(int rssRefreshPeriod) {
        this.rssRefreshPeriod = rssRefreshPeriod;
    }


    public boolean isEnableJobAutoRefresh() {
        return enableJobAutoRefresh;
    }


    public void setEnableJobAutoRefresh(boolean enableJobAutoRefresh) {
        this.enableJobAutoRefresh = enableJobAutoRefresh;
    }


    public boolean isEnableRssAutoRefresh() {
        return enableRssAutoRefresh;
    }


    public void setEnableRssAutoRefresh(boolean enableRssAutoRefresh) {
        this.enableRssAutoRefresh = enableRssAutoRefresh;
    }


    public String getPreferredView() {
        return preferredView;
    }


    public void setPreferredView(String preferredView) {
        this.preferredView = preferredView;
    }
}
