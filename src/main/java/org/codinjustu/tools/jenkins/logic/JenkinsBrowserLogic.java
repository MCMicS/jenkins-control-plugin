package org.codinjustu.tools.jenkins.logic;

import org.apache.log4j.Logger;
import org.codinjustu.tools.jenkins.JenkinsConfiguration;
import org.codinjustu.tools.jenkins.model.Build;
import org.codinjustu.tools.jenkins.model.Jenkins;
import org.codinjustu.tools.jenkins.model.Job;
import org.codinjustu.tools.jenkins.model.View;
import org.codinjustu.tools.jenkins.view.JenkinsBrowserView;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

//TODO gérer les exception JDOM et IO autrements
abstract class JenkinsBrowserLogic<V extends JenkinsBrowserView> {

    private static final Logger LOG = Logger.getLogger(JenkinsBrowserLogic.class);

    private static final int MILLISECONDS = 1000;
    private static final int MINUTES = 60 * MILLISECONDS;

    private final V view;
    private final JenkinsConfiguration configuration;
    private final JenkinsRequestManager jenkinsRequestManager;

    private Jenkins jenkins;
    private final Map<String, Build> currentBuildMap = new HashMap<String, Build>();
    private Timer jobRefreshTimer;
    private Timer rssRefreshTimer;

    JenkinsBrowserLogic(JenkinsConfiguration configuration,
                        JenkinsRequestManager jenkinsRequestManager,
                        V view) {
        this.configuration = configuration;
        this.jenkinsRequestManager = jenkinsRequestManager;
        this.view = view;
    }


    public void init() {
        initGui();
        reloadConfiguration();
    }


    protected abstract void initGui();


    public void reloadConfiguration() {
        loadJenkinsWorkspace();
        initTimers();
    }


    void loadJenkinsWorkspace() {
        if (configuration.isServerUrlSet()) {
            try {

                jenkins = jenkinsRequestManager.loadJenkinsWorkspace(configuration);
                view.initModel(jenkins);
                String preferredView = configuration.getPreferredView();
                View jenkinsView = findView(preferredView);
                if (jenkinsView != null) {
                    this.view.setSelectedView(jenkinsView);
                } else {
                    this.view.setSelectedView(jenkins.getPrimaryView());
                }
//              addLatestBuilds(jenkinsRequestManager.loadJenkinsRssLatestBuilds(configuration));
            } catch (JDOMException domEx) {
                String errorMessage = buildServerErrorMessage(domEx);
                LOG.error(errorMessage, domEx);
                showErrorDialog(errorMessage, "Error during parsing workspace");
            } catch (Exception ex) {
                LOG.error(buildServerErrorMessage(ex), ex);
                displayConnectionErrorMsg();
            }
        } else {
            displayConnectionErrorMsg();
        }
    }


    public void loadSelectedView() {

        try {
            View jenkinsView = getSelectedJenkinsView();
            if (jenkinsView != null) {
                List<Job> jobList = jenkinsRequestManager.loadJenkinsView(jenkinsView.getUrl());
                jenkins.setJobs(jobList);
                this.view.fillJobTree(jenkins);
            } else {
                loadJenkinsWorkspace();
            }
        } catch (JDOMException domEx) {
            String errorMessage = buildServerErrorMessage(domEx);
            LOG.error(errorMessage, domEx);
            showErrorDialog(errorMessage, "Error during parsing View Data");
        } catch (IOException ioEx) {
            LOG.error(buildServerErrorMessage(ioEx), ioEx);
            displayConnectionErrorMsg();
        }
    }

    public void loadSelectedJob() {
        try {
            Job job = getSelectedJob();
            Job updatedJob = jenkinsRequestManager.loadJob(job.getUrl());
            job.updateContentWith(updatedJob);
        } catch (JDOMException domEx) {
            String errorMessage = buildServerErrorMessage(domEx);
            LOG.error(errorMessage, domEx);
            showErrorDialog(errorMessage, "Error during parsing View Data");
        } catch (IOException ioEx) {
            LOG.error(buildServerErrorMessage(ioEx), ioEx);
            displayConnectionErrorMsg();
        }
    }


    void initTimers() {
        if (jobRefreshTimer != null) {
            jobRefreshTimer.cancel();
        }

        if (rssRefreshTimer != null) {
            rssRefreshTimer.cancel();
        }

        if (configuration.isEnableJobAutoRefresh()) {
            jobRefreshTimer = new Timer();
            jobRefreshTimer.schedule(new JobRefreshTimerTask(),
                    MINUTES,
                    configuration.getJobRefreshPeriod() * MINUTES);
        }

        if (configuration.isEnableRssAutoRefresh()) {
            rssRefreshTimer = new Timer();
            rssRefreshTimer.schedule(new RssRefreshTimerTask(),
                    MINUTES,
                    configuration.getRssRefreshPeriod() * MINUTES);
        }
    }


    private View findView(String preferredView) {
        List<View> viewList = jenkins.getViews();
        for (View jenkinsView : viewList) {
            String viewName = jenkinsView.getName();
            if (viewName.equals(preferredView)) {
                return jenkinsView;
            }
        }
        return jenkins.getPrimaryView();
    }


    protected abstract void displayConnectionErrorMsg();


    protected abstract void showErrorDialog(String errorMessage, String title);


    public void refreshLatestCompletedBuilds() {
        try {
            if (jenkins != null && !jenkins.getJobs().isEmpty()) {
                Map<String, Build> latestBuild = jenkinsRequestManager.loadJenkinsRssLatestBuilds(
                        configuration);
                displayFinishedBuilds(addLatestBuilds(latestBuild));
            }
        } catch (JDOMException domEx) {
            String errorMessage = buildServerErrorMessage(domEx);
            LOG.error(errorMessage, domEx);
            showErrorDialog(errorMessage, "Error during parsing Rss Data");
        } catch (IOException ioEx) {
            LOG.error(buildServerErrorMessage(ioEx), ioEx);
            ioEx.printStackTrace();
        }
    }


    protected abstract void displayFinishedBuilds(Map<String, Build> latestBuilds);

    public abstract void cleanRssEntries();


    Map<String, Build> addLatestBuilds(Map<String, Build> latestBuildMap) {
        Map<String, Build> newBuildMap = new HashMap<String, Build>();
        for (Entry<String, Build> entry : latestBuildMap.entrySet()) {
            String jobName = entry.getKey();
            Build newBuild = entry.getValue();
            Build currentBuild = currentBuildMap.get(jobName);
            if (!currentBuildMap.containsKey(jobName) || newBuild.isDisplayable(currentBuild)) {
                currentBuildMap.put(jobName, newBuild);
                newBuildMap.put(jobName, newBuild);
            }
        }

        return newBuildMap;
    }


    String buildServerErrorMessage(Exception ex) {
        return "Server Url=" + configuration.getServerUrl() + "\n" + ex.getMessage();
    }


    public View getSelectedJenkinsView() {
        return view.getSelectedJenkinsView();
    }


    public Job getSelectedJob() {
        return view.getSelectedJob();
    }


    public V getView() {
        return view;
    }


    public JenkinsRequestManager getJenkinsManager() {
        return jenkinsRequestManager;
    }

    private class JobRefreshTimerTask extends TimerTask {

        @Override
        public void run() {
            loadSelectedView();
        }
    }

    private class RssRefreshTimerTask extends TimerTask {

        @Override
        public void run() {
            refreshLatestCompletedBuilds();
        }
    }
}
