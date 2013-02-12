/*
 * Copyright (c) 2012 David Boissier
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
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.JobBuilder;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uispec4j.ComboBox;
import org.uispec4j.TextBox;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;
import java.util.Map;

import static org.codinjutsu.tools.jenkins.model.JobParameter.JobParameterType.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BuildParamDialogTest extends UISpecTestCase {

    @Mock
    private RequestManager requestManager;

    @Mock
    private BuildParamDialog.RunBuildCallback callbackRun;

    private JenkinsAppSettings configuration;

    private static final Job JOB_WITH_GOOD_PARAMS =
            new JobBuilder()
                    .job("myJob", "blue", "http://dummyserver/jobs/myJob", "false", "true")
                    .health("health-80plus", "0 tests en échec sur un total de 24 tests")
                    .parameter("integrationTest", BooleanParameterDefinition.name(), "true")
                    .parameter("environment", ChoiceParameterDefinition.name(), "development",
                            "development", "integration", "acceptance", "production")
                    .parameter("message", StringParameterDefinition.name(), "")
                    .get();

    private static final Job JOB_WITH_UNSUPPORTED_PARAMS =
            new JobBuilder()
                    .job("myJob", "blue", "http://dummyserver/jobs/myJob", "false", "true")
                    .health("health-80plus", "0 tests en échec sur un total de 24 tests")
                    .parameter("run", RunParameterDefinition.name(), "blah")
                    .get();

    private static final Job JOB_WITH_UNKNOWN_PARAMS =
            new JobBuilder()
                    .job("myJob", "blue", "http://dummyserver/jobs/myJob", "false", "true")
                    .health("health-80plus", "0 tests en échec sur un total de 24 tests")
                    .parameter("run", null, "blah")
                    .get();

    public void testDisplay() throws Exception {
        Window uispecDialog = createUISpecWindow(JOB_WITH_GOOD_PARAMS);


        assertEquals("This build requires parameters", uispecDialog.getTitle());

        assertTrue(uispecDialog.getCheckBox("integrationTest").isSelected());

        ComboBox envCombo = uispecDialog.getComboBox("environment");
        assertTrue(envCombo.contains("development", "integration", "acceptance", "production"));
        assertTrue(envCombo.selectionEquals("development"));
        assertTrue(StringUtils.isEmpty(uispecDialog.findSwingComponent(JTextField.class).getText()));

        assertTrue(uispecDialog.getButton("OK").isEnabled());

    }

    public void testLaunchBuild() throws Exception {
        Window uispecDialog = createUISpecWindow(JOB_WITH_GOOD_PARAMS);

        uispecDialog.getCheckBox("integrationTest").unselect();
        uispecDialog.getComboBox("environment").select("acceptance");

        uispecDialog.getButton("OK").click();

        ArgumentCaptor<Map> paramMap = ArgumentCaptor.forClass(Map.class);
        verify(requestManager, times(1)).runParameterizedBuild(any(Job.class), any(JenkinsAppSettings.class), paramMap.capture());

        Map expectedParamMapValue = paramMap.getValue();
        assertEquals(3, expectedParamMapValue.size());
        assertEquals("false", expectedParamMapValue.get("integrationTest"));
        assertEquals("acceptance", expectedParamMapValue.get("environment"));
        assertEquals("", expectedParamMapValue.get("message"));
    }

    public void testUnsupportedParams() throws Exception {
        Window uispecDialog = createUISpecWindow(JOB_WITH_UNSUPPORTED_PARAMS);

        TextBox runLabel = uispecDialog.getTextBox(ComponentMatchers.componentLabelFor("run"));
        assertTrue(runLabel.textEquals("RunParameterDefinition is unsupported."));
        assertIconEquals("error.png", runLabel);

        assertFalse(uispecDialog.getButton("OK").isEnabled());
    }

    public void testUnknowParams() throws Exception {
        Window uispecDialog = createUISpecWindow(JOB_WITH_UNKNOWN_PARAMS);

        TextBox runLabel = uispecDialog.getTextBox(ComponentMatchers.componentLabelFor("run"));
        assertTrue(runLabel.textEquals("Unkown parameter"));
        assertIconEquals("error.png", runLabel);

        assertFalse(uispecDialog.getButton("OK").isEnabled());
    }

    private void assertIconEquals(String expectedIconFilename, TextBox actualLabel) {
        assertEquals(GuiUtil.loadIcon(expectedIconFilename).toString(), ((JLabel) actualLabel.getAwtComponent()).getIcon().toString());
    }

    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        configuration = new JenkinsAppSettings();


    }

    private Window createUISpecWindow(Job job) {
        return new Window(new BuildParamDialog(job, configuration, requestManager, callbackRun));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
}
