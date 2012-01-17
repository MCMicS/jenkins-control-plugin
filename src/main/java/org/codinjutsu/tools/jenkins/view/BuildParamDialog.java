/*
 * Copyright (c) 2011 David Boissier
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

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.util.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class BuildParamDialog extends JDialog {
    private static final Color LIGHT_RED_BACKGROUND = new Color(230, 150, 150);
    private static final Color RED_BORDER = new Color(220, 0, 0);
    private static final String MISSING_NAME_LABEL = "<Missing Name>";
    private static final Icon ERROR_ICON = GuiUtil.loadIcon("error.png");
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel contentPanel;
    private JLabel feedbackLabel;

    private boolean hasError = false;

    private final Job job;
    private final JenkinsConfiguration configuration;
    private final JenkinsRequestManager jenkinsManager;
    private final RunBuildCallback runBuildCallback;
    private final Map<JobParameter, JComponent> inputFieldByParameterMap = new HashMap<JobParameter, JComponent>();

    private static final Set<JobParameter.JobParameterType> USUPPORTED_PARAM_TYPE = new HashSet<JobParameter.JobParameterType>();

    static {
        USUPPORTED_PARAM_TYPE.add(JobParameter.JobParameterType.FileParameterDefinition);
        USUPPORTED_PARAM_TYPE.add(JobParameter.JobParameterType.TextParameterDefinition);
        USUPPORTED_PARAM_TYPE.add(JobParameter.JobParameterType.RunParameterDefinition);
        USUPPORTED_PARAM_TYPE.add(JobParameter.JobParameterType.ListSubversionTagsParameterDefinition);
    }

    BuildParamDialog(Job job, JenkinsConfiguration configuration, JenkinsRequestManager jenkinsManager, RunBuildCallback runBuildCallback) {
        this.job = job;
        this.configuration = configuration;
        this.jenkinsManager = jenkinsManager;
        this.runBuildCallback = runBuildCallback;

        contentPanel.setName("contentPanel");

        addParameterInputs();
        setTitle("This build requires parameters");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        feedbackLabel.setOpaque(true);

        registerListeners();
    }

    public static void showDialog(final Job job, final JenkinsConfiguration configuration, final JenkinsRequestManager jenkinsManager, final RunBuildCallback runBuildCallback) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BuildParamDialog dialog = new BuildParamDialog(job, configuration, jenkinsManager, runBuildCallback);
                dialog.setLocationRelativeTo(null);
                dialog.setMaximumSize(new Dimension(300, 200));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

    private void addParameterInputs() {
        contentPanel.setLayout(new SpringLayout());
        List<JobParameter> parameters = job.getParameters();

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

            inputFieldByParameterMap.put(jobParameter, inputField);
        }

        SpringUtilities.makeCompactGrid(contentPanel,
                parameters.size(), 2,
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
            text = "Unkown parameter";
        }
        JLabel label = new JLabel(text);
        label.setIcon(ERROR_ICON);
        return label;
    }

    private void onOK() {
        try {
//            checkInputValues();
            jenkinsManager.runParameterizedBuild(job, configuration, getParamValueMap());
            dispose();
            runBuildCallback.notifyOnOk(job);
        } catch (ConfigurationException confEx) {
            setErrorOnFeedbackPanel(confEx.getMessage());
        } catch (Exception ex) {
            runBuildCallback.notifyOnError(job, ex);
        }

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
        JComboBox comboBox = new JComboBox(jobParameter.getValues().toArray());
        if (StringUtils.isNotEmpty(defaultValue)) {
            comboBox.setSelectedItem(defaultValue);
        }
        return comboBox;
    }

//    private void checkInputValues() throws Exception {
//
//        NotNullValidator notNullValidator = new NotNullValidator();
//        for (Map.Entry<JobParameter, JComponent> componentByJobParameterEntry : inputFieldByParameterMap.entrySet()) {
//            JComponent component = componentByJobParameterEntry.getValue();
//            if (component instanceof JTextField) {
//                notNullValidator.validate((JTextField) component);
//            }
//        }
//    }

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

    private void setErrorOnFeedbackPanel(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setBackground(LIGHT_RED_BACKGROUND);
        feedbackLabel.setBorder(BorderFactory.createLineBorder(RED_BORDER));
    }

    public interface RunBuildCallback {

        void notifyOnOk(Job job);

        void notifyOnError(Job job, Exception ex);
    }
}
