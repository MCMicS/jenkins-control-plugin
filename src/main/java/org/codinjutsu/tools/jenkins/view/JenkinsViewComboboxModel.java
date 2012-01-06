package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.model.View;

import javax.swing.*;
import java.util.List;

class JenkinsViewComboboxModel extends DefaultComboBoxModel {


    public JenkinsViewComboboxModel(List<View> views) {
        super(views.toArray());
    }

    @Override
    public void setSelectedItem(Object obj) {
        if (obj == null) return;

        View view = (View) obj;
        if (!view.hasNestedView()) {
            super.setSelectedItem(obj);
        }
    }
}
