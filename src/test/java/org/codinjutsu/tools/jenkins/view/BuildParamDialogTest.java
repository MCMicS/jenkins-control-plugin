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

import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.logic.JobBuilder;
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

import java.util.Map;

import static org.codinjutsu.tools.jenkins.model.JobParameter.JobParameterType.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BuildParamDialogTest extends UISpecTestCase {

    @Mock
    private JenkinsRequestManager requestManager;

    @Mock
    private BuildParamDialog.RunBuildCallback callbackRun;

    private JenkinsConfiguration configuration;

    private static final Job JOB_WITH_GOOD_PARAMS =
            new JobBuilder()
                    .job("myJob", "blue", "http://dummyserver/jobs/myJob", "false")
                    .health("health-80plus", "0 tests en échec sur un total de 24 tests")
                    .parameter("integrationTest", BooleanParameterDefinition.name(), "true")
                    .parameter("environment", ChoiceParameterDefinition.name(), "development",
                            "development", "integration", "acceptance", "production")
                    .get();

    private static final Job JOB_WITH_UNSUPPORTED_PARAMS =
            new JobBuilder()
                    .job("myJob", "blue", "http://dummyserver/jobs/myJob", "false")
                    .health("health-80plus", "0 tests en échec sur un total de 24 tests")
                    .parameter("run", RunParameterDefinition.name(), "blah")
                    .get();

    private static final Job JOB_WITH_UNKNOWN_PARAMS =
            new JobBuilder()
                    .job("myJob", "blue", "http://dummyserver/jobs/myJob", "false")
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

        assertTrue(uispecDialog.getButton("OK").isEnabled());

    }

    public void testLaunchBuild() throws Exception {
        Window uispecDialog = createUISpecWindow(JOB_WITH_GOOD_PARAMS);

        uispecDialog.getCheckBox("integrationTest").unselect();
        uispecDialog.getComboBox("environment").select("acceptance");

        uispecDialog.getButton("OK").click();

        ArgumentCaptor<Map> paramMap = ArgumentCaptor.forClass(Map.class);
        verify(requestManager, times(1)).runParameterizedBuild(any(Job.class), any(JenkinsConfiguration.class), paramMap.capture());

        Map expectedParamMapValue = paramMap.getValue();
        assertEquals(2, expectedParamMapValue.size());
        assertEquals("false", expectedParamMapValue.get("integrationTest"));
        assertEquals("acceptance", expectedParamMapValue.get("environment"));
    }

    public void testUnsupportedParams() throws Exception {
        Window uispecDialog = createUISpecWindow(JOB_WITH_UNSUPPORTED_PARAMS);

        TextBox runTextBox = uispecDialog.getTextBox(ComponentMatchers.componentLabelFor("run"));
        assertTrue(runTextBox.textEquals("RunParameterDefinition is unsupported."));
        assertTrue(runTextBox.iconEquals(GuiUtil.loadIcon("error.png")));

        assertFalse(uispecDialog.getButton("OK").isEnabled());
    }

    public void testUnknowParams() throws Exception {
        Window uispecDialog = createUISpecWindow(JOB_WITH_UNKNOWN_PARAMS);

        TextBox runTextBox = uispecDialog.getTextBox(ComponentMatchers.componentLabelFor("run"));
        assertTrue(runTextBox.textEquals("Unkown parameter"));
        assertTrue(runTextBox.iconEquals(GuiUtil.loadIcon("error.png")));

        assertFalse(uispecDialog.getButton("OK").isEnabled());
    }

    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        configuration = new JenkinsConfiguration();


    }

    private Window createUISpecWindow(Job job) {
        return new Window(new BuildParamDialog(job, configuration, requestManager, callbackRun));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
}
