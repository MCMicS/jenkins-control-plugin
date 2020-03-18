/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.view;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JobTracker;
import org.codinjutsu.tools.jenkins.TraceableBuildJob;
import org.codinjutsu.tools.jenkins.TraceableBuildJobFactory;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.util.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BuildParamDialog extends JDialog {
    private static final Logger logger = Logger.getLogger(BuildParamDialog.class);
    private static final String MISSING_NAME_LABEL = "<Missing Name>";
    private static final Icon ERROR_ICON = AllIcons.General.BalloonError;
    /**
     * com.intellij.icons.AllIcons.General#NotificationError
     * com.intellij.icons.AllIcons.RunConfigurations#ToolbarError
     * com.intellij.icons.AllIcons.General#BalloonError
     * AllIcons.RunConfigurations.ToolbarError
     */
    private static final Icon WARNING_ICON = AllIcons.General.BalloonWarning;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel contentPanel;

    private boolean hasError = false;

    private final Job job;
    private final JenkinsAppSettings configuration;
    private final RequestManager requestManager;
    private final RunBuildCallback runBuildCallback;
    private final Map<JobParameter, JComponent> inputFieldByParameterMap = new HashMap<>();

// UNSUPPORTED PARAMETERS
//        FileParameterDefinition
//        TextParameterDefinition
//        RunParameterDefinition
//        ListSubversionTagsParameterDefinition

    BuildParamDialog(Job job, JenkinsAppSettings configuration, RequestManager requestManager, RunBuildCallback runBuildCallback) {
        this.job = job;
        this.configuration = configuration;
        this.requestManager = requestManager;
        this.runBuildCallback = runBuildCallback;

        contentPanel.setName("contentPanel");

        addParameterInputs();
        setTitle("This build requires parameters");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        registerListeners();
    }

    public static void showDialog(final Job job, final JenkinsAppSettings configuration, final RequestManager requestManager,
            final RunBuildCallback runBuildCallback) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BuildParamDialog dialog = new BuildParamDialog(job, configuration, requestManager, runBuildCallback);
                dialog.setLocationRelativeTo(null);
                dialog.setSize(dialog.getPreferredSize());
                dialog.setMaximumSize(new Dimension(300, 200));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

    private void addParameterInputs() {
        contentPanel.setLayout(new SpringLayout());
        List<JobParameter> parameters = job.getParameters();

        int rows = parameters.size();
        for (JobParameter jobParameter : parameters) {
            JComponent inputField = createInputField(jobParameter);

            String name = jobParameter.getName();
            inputField.setName(name);

            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.TRAILING);
            label.setLabelFor(inputField);

            if (StringUtils.isEmpty(name)) {
                name = MISSING_NAME_LABEL;
                label.setIcon(ERROR_ICON);
                hasError = true;
            }
            label.setText(name + ":");

            contentPanel.add(label);
            contentPanel.add(inputField);

            String description = jobParameter.getDescription();
            if (StringUtils.isNotEmpty(description)) {
                JLabel placeHolder = new JLabel("", JLabel.CENTER);
                JTextPane descText = new JTextPane();
                descText.setText(description);

                contentPanel.add(placeHolder);
                contentPanel.add(descText);
                rows++;
            }

            inputFieldByParameterMap.put(jobParameter, inputField);
        }

        SpringUtilities.makeCompactGrid(contentPanel,
                rows, 2,
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad

        if (hasError) {
            buttonOK.setEnabled(false);
        }
    }

    private void registerListeners() {
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private JComponent createInputField(JobParameter jobParameter) {//TODO add wrapper

        JobParameter.JobParameterType jobParameterType = jobParameter.getJobParameterType();
        String defaultValue = jobParameter.getDefaultValue();
        JComponent inputField;

        if (JobParameter.JobParameterType.ChoiceParameterDefinition.equals(jobParameterType)) {
            inputField = createComboBox(jobParameter, defaultValue);
        } else if (JobParameter.JobParameterType.BooleanParameterDefinition.equals(jobParameterType)) {
            inputField = createCheckBox(defaultValue);
        } else if (JobParameter.JobParameterType.StringParameterDefinition.equals(jobParameterType)) {
            inputField = createTextField(defaultValue);
        } else if (JobParameter.JobParameterType.PasswordParameterDefinition.equals(jobParameterType)) {
            inputField = createPasswordField(defaultValue);
        } else {
            inputField = createErrorLabel(jobParameterType);
            hasError = true;
        }
        return inputField;
    }

    private JLabel createErrorLabel(JobParameter.JobParameterType jobParameterType) {
        String text;
        if (jobParameterType != null) {
            text = jobParameterType.name() + " is unsupported.";
        } else {
            text = "Unknown parameter";
        }
        JLabel label = new JLabel(text);
        label.setIcon(ERROR_ICON);
        return label;
    }

    private void onOK() {
        final Map<String, String> paramValueMap = getParamValueMap();

        new SwingWorker<Void, Void>() { //FIXME don't use swing worker
            @Override
            protected Void doInBackground() throws Exception {
                TraceableBuildJob buildJob = TraceableBuildJobFactory.newBuildJob(job, configuration, paramValueMap, requestManager);
                JobTracker.getInstance().registerJob(buildJob);
                requestManager.runParameterizedBuild(job, configuration, paramValueMap);
                return null;
            }

            @Override
            protected void done() {
                dispose();
                try {
                    get();
                    runBuildCallback.notifyOnOk(job);
                } catch (InterruptedException e) {
                    logger.log(Level.WARN, "Exception occured while...", e);
                } catch (ExecutionException e) {
                    runBuildCallback.notifyOnError(job, e);
                    logger.log(Level.WARN, "Exception occured while trying to invoke build", e);
                }

            }
        }.execute();
    }

    private void onCancel() {
        dispose();
    }

    private JPasswordField createPasswordField(String defaultValue) {
        JPasswordField passwordField = new JPasswordField();
        if (StringUtils.isNotEmpty(defaultValue)) {
            passwordField.setText(defaultValue);
        }
        return passwordField;
    }

    private JTextField createTextField(String defaultValue) {
        JTextField textField = new JTextField();
        if (StringUtils.isNotEmpty(defaultValue)) {
            textField.setText(defaultValue);
        }
        return textField;
    }

    private JCheckBox createCheckBox(String defaultValue) {
        JCheckBox checkBox = new JCheckBox();
        if (Boolean.TRUE.equals(Boolean.valueOf(defaultValue))) {
            checkBox.setSelected(true);
        }
        return checkBox;
    }

    private JComboBox createComboBox(JobParameter jobParameter, String defaultValue) {
        ComboBox comboBox = new ComboBox(jobParameter.getValues().toArray());
        if (StringUtils.isNotEmpty(defaultValue)) {
            comboBox.setSelectedItem(defaultValue);
        }
        return comboBox;
    }

    private Map<String, String> getParamValueMap() {//TODO transformer en visiteur
        HashMap<String, String> valueByNameMap = new HashMap<String, String>();
        for (Map.Entry<JobParameter, JComponent> inputFieldByParameter : inputFieldByParameterMap.entrySet()) {
            JobParameter jobParameter = inputFieldByParameter.getKey();
            String name = jobParameter.getName();
            JobParameter.JobParameterType jobParameterType = jobParameter.getJobParameterType();

            JComponent inputField = inputFieldByParameter.getValue();

            if (JobParameter.JobParameterType.ChoiceParameterDefinition.equals(jobParameterType)) {
                JComboBox comboBox = (JComboBox) inputField;
                valueByNameMap.put(name, String.valueOf(comboBox.getSelectedItem()));
            } else if (JobParameter.JobParameterType.BooleanParameterDefinition.equals(jobParameterType)) {
                JCheckBox checkBox = (JCheckBox) inputField;
                valueByNameMap.put(name, Boolean.toString(checkBox.isSelected()));
            } else if (JobParameter.JobParameterType.StringParameterDefinition.equals(jobParameterType)) {
                JTextField textField = (JTextField) inputField;
                valueByNameMap.put(name, textField.getText());
            }
        }
        return valueByNameMap;
    }

    public interface RunBuildCallback {

        void notifyOnOk(Job job);

        void notifyOnError(Job job, Exception ex);
    }
}
