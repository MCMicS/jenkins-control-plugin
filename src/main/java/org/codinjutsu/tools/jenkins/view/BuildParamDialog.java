package org.codinjutsu.tools.jenkins.view;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.view.util.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildParamDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel contentPanel;

    private final Job job;
    private final JenkinsConfiguration configuration;
    private final JenkinsRequestManager jenkinsManager;
    private Map<JobParameter, JComponent> inputFieldByParameterMap = new HashMap<JobParameter, JComponent>();

    public BuildParamDialog(Job job, JenkinsConfiguration configuration, JenkinsRequestManager jenkinsManager) {
        this.job = job;
        this.configuration = configuration;
        this.jenkinsManager = jenkinsManager;

        addParameterInputs();
        setTitle("This build requires parameters");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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

    private void addParameterInputs() { //TODO revoir le layout :((((
        contentPanel.setLayout(new SpringLayout());
        List<JobParameter> parameters = job.getParameters();


        for (JobParameter jobParameter : parameters) {
            JLabel label = new JLabel(jobParameter.getName() + ":", JLabel.TRAILING);
            contentPanel.add(label);
            JComponent inputField = createInputField(jobParameter);
            label.setLabelFor(inputField);
            contentPanel.add(inputField);
        }

        SpringUtilities.makeCompactGrid(contentPanel,
                parameters.size(), 2,
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad


    }

    private JComponent createInputField(JobParameter jobParameter) {//TODO transformer en visiteur
        String defaultValue = jobParameter.getDefaultValue();
        JobParameter.JobParameterType jobParameterType = jobParameter.getJobParameterType();

        if (JobParameter.JobParameterType.ChoiceParameterDefinition.equals(jobParameterType)) {
            JComboBox comboBox = new JComboBox(jobParameter.getValues().toArray());
            if (StringUtils.isNotEmpty(defaultValue)) {
                comboBox.setSelectedItem(defaultValue);
            }
            inputFieldByParameterMap.put(jobParameter, comboBox);
            return comboBox;
        } else if (JobParameter.JobParameterType.BooleanParameterDefinition.equals(jobParameterType)) {
            JCheckBox checkBox = new JCheckBox();
            if (Boolean.TRUE.equals(Boolean.valueOf(defaultValue))) {
                checkBox.setSelected(true);
            }
            inputFieldByParameterMap.put(jobParameter, checkBox);
            return checkBox;
        } else if (JobParameter.JobParameterType.StringParameterDefinition.equals(jobParameterType)) {
            JTextField textField = new JTextField();
            if (StringUtils.isNotEmpty(defaultValue)) {
                textField.setText(defaultValue);
            }
            inputFieldByParameterMap.put(jobParameter, textField);
            return textField;
        } else {
            return new JLabel("Unsupported ParameterDefinitionType : " + jobParameterType.name());
        }
    }

    private void onOK() {
        try {
            jenkinsManager.runParameterizedBuild(job, configuration, getParamValueMap());
            dispose();
        } catch (Exception e) {
            //TODO add feedback panel
        }
        //ajouter notification ici
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

    private void onCancel() {//TODO corriger la notification
        dispose();
    }

    public static void showDialog(final Job job, final JenkinsConfiguration configuration, final JenkinsRequestManager jenkinsManager) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BuildParamDialog dialog = new BuildParamDialog(job, configuration, jenkinsManager);
                dialog.setLocationRelativeTo(null);
                dialog.setPreferredSize(new Dimension(300, 200));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }
}
