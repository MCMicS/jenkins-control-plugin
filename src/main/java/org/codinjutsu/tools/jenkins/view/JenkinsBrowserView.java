package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;

public interface JenkinsBrowserView {

    void initModel(Jenkins jenkins);


    void setSelectedView(View view);


    void fillJobTree(Jenkins jenkins);


    View getSelectedJenkinsView();


    Job getSelectedJob();


    void showErrorDialog(String errorMessage, String content);
}
