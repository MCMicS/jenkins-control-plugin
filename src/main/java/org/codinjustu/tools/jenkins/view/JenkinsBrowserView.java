package org.codinjustu.tools.jenkins.view;

import org.codinjustu.tools.jenkins.model.Jenkins;
import org.codinjustu.tools.jenkins.model.Job;
import org.codinjustu.tools.jenkins.model.View;

public interface JenkinsBrowserView {

    void initModel(Jenkins jenkins);


    void setSelectedView(View view);


    void fillJobTree(Jenkins jenkins);


    View getSelectedJenkinsView();


    Job getSelectedJob();


    void showErrorDialog(String errorMessage, String content);
}
