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

import com.intellij.openapi.project.Project;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.core.matcher.JTextComponentMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.DialogFixture;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.JobBuilder;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.Job;
import org.jetbrains.annotations.NonNls;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@Ignore("make Test to run headless")
public class BuildParamDialogTest {

    @Mock
    private RequestManager requestManager;

    @Mock
    private BuildParamDialog.RunBuildCallback callbackRun;

    private JenkinsAppSettings configuration;

    private static final Job JOB_WITH_GOOD_PARAMS =
            new JobBuilder()
                    .job("myJob", "blue", "http://dummyserver/jobs/myJob", false, true)
                    .health("health-80plus", "0 tests en échec sur un total de 24 tests")
                    .parameter("integrationTest", BuildInJobParameter.BooleanParameterDefinition.getType(), "true")
                    .parameter("environment", BuildInJobParameter.ChoiceParameterDefinition.getType(), "development",
                            "development", "integration", "acceptance", "production")
                    .parameter("messages", BuildInJobParameter.StringParameterDefinition.getType(), "")
                    .get();

    private static final Job JOB_WITH_UNSUPPORTED_PARAMS =
            new JobBuilder()
                    .job("myJob", "blue", "http://dummyserver/jobs/myJob", false, true)
                    .health("health-80plus", "0 tests en échec sur un total de 24 tests")
                    .parameter("run", BuildInJobParameter.RunParameterDefinition.getType(), "blah")
                    .get();

    @NonNls
    private static final String TESTPARAMETER_TYPE = "TestParameter";
    private static final Job JOB_WITH_UNKNOWN_PARAMS =
            new JobBuilder()
                    .job("myJob", "blue", "http://dummyserver/jobs/myJob", false, true)
                    .health("health-80plus", "0 tests en échec sur un total de 24 tests")
                    .parameter("run", TESTPARAMETER_TYPE, "blah")
                    .get();

    private DialogFixture dialogFixture;

    private AutoCloseable mocks;

    @After
    public void tearDown() throws Exception {
        dialogFixture.cleanUp();
        mocks.close();
    }

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        configuration = new JenkinsAppSettings();
    }

    private void createDialog(final Job job) {
        BuildParamDialog buildParamDialog = GuiActionRunner.execute(new GuiQuery<BuildParamDialog>() {
            protected BuildParamDialog executeInEDT() {
                Project project = mock(Project.class);
                return new BuildParamDialog(project, job, configuration, requestManager, callbackRun);
            }
        });
        dialogFixture = new DialogFixture(BasicRobot.robotWithCurrentAwtHierarchy(), "BuildParamDialog");
        dialogFixture.show();
    }

    @Test
    @Ignore(value = "Issue in maven compilation :(")
    public void displaySimpleJob() throws Exception {
        createDialog(JOB_WITH_GOOD_PARAMS);


//        assertEquals("This build requires parameters", uispecDialog.getTitle());
        dialogFixture.checkBox("integrationTest").requireSelected();

        String[] contents = dialogFixture.comboBox()
                .requireItemCount(4)
                .requireSelection("development")
                .contents();

        assertArrayEquals(new String[]{"development", "integration", "acceptance", "production"}, contents);

        dialogFixture.textBox(JTextComponentMatcher.any()).requireText("");

        dialogFixture.button(JButtonMatcher.withText("OK")).requireEnabled();

    }

    @Test
    @Ignore(value = "Intellij component dependency, need to mock it")
    public void testLaunchBuild() throws Exception {
        createDialog(JOB_WITH_GOOD_PARAMS);

        dialogFixture.checkBox("integrationTest").uncheck();
        dialogFixture.comboBox("environment").selectItem("acceptance");

        dialogFixture.button(JButtonMatcher.withText("OK")).click();

        ArgumentCaptor<Map> paramMap = ArgumentCaptor.forClass(Map.class);
        verify(requestManager, times(1)).runBuild(any(Job.class), any(JenkinsAppSettings.class), paramMap.capture());

        Map expectedParamMapValue = paramMap.getValue();
        assertEquals(3, expectedParamMapValue.size());
        assertEquals("false", expectedParamMapValue.get("integrationTest"));
        assertEquals("acceptance", expectedParamMapValue.get("environment"));
        assertEquals("", expectedParamMapValue.get("messages"));
    }

    @Test
    public void unsupportedParams() throws Exception {
        createDialog(JOB_WITH_UNSUPPORTED_PARAMS);

        dialogFixture.label(JLabelMatcher.withName("run"))
                .requireText("RunParameterDefinition is unsupported.");

        dialogFixture.button(JButtonMatcher.withText("OK")).requireDisabled();
    }

    @Test
    public void unknowParams() throws Exception {
        createDialog(JOB_WITH_UNKNOWN_PARAMS);

        dialogFixture.label(JLabelMatcher.withName("run"))
                .requireText("Unknown parameter");

        dialogFixture.button(JButtonMatcher.withText("OK")).requireDisabled();
    }
}
