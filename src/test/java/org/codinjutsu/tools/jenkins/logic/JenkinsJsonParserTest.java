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

package org.codinjutsu.tools.jenkins.logic;

import org.assertj.core.util.Lists;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.util.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.FAILURE;
import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.SUCCESS;

public class JenkinsJsonParserTest {

    private JenkinsParser jsonParser;

    private AutoCloseable mocks;

    private static View createView(String viewName, String viewUrl) {
        return createView(viewName, viewUrl, false);
    }

    private static View createView(String viewName, String viewUrl, boolean isNested) {
        return View.builder()
                .name(viewName)
                .url(viewUrl)
                .isNested(isNested)
                .build();
    }

    private static View createNestedView(String viewName, String viewUrl) {
        return createView(viewName, viewUrl, true);
    }

    @Test
    public void loadJenkinsWorkSpace() throws Exception {
        Jenkins jenkins = jsonParser.createWorkspace(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadWorkspace.json")));

        List<View> actualViews = jenkins.getViews();

        assertThat(actualViews).containsExactly(createView("Framework", "http://myjenkins/view/Framework/"),
                createView("Tools", "http://myjenkins/view/Tools/"),
                createView("Tous", "http://myjenkins/"));
        assertThat(jenkins.getPrimaryView()).isEqualTo(createView("Tous", "http://myjenkins"));
        //assertThat(jenkins.getPrimaryView()).isEqualToComparingFieldByField(View.createView("Tous", "http://myjenkins"));
    }

    @Test
    public void loadJenkinsWorkSpaceWithNestedViews() throws Exception {
        Jenkins jenkins = jsonParser.createWorkspace(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadWorkspaceWithNestedView.json")));

        List<View> actualViews = jenkins.getViews();

        List<View> expectedViews = new LinkedList<>();
        expectedViews.add(createView("Framework", "http://myjenkins/view/Framework/"));
        View nestedView = View.builder()
                .name("NestedView")
                .url("http://myjenkins/view/NestedView/")
                .isNested(false)
                .subView(createNestedView("FirstSubView", "http://myjenkins/view/NestedView/view/FirstSubView/"))
                .subView(createNestedView("SecondSubView", "http://myjenkins/view/NestedView/view/SecondSubView/"))
                .build();
        expectedViews.add(nestedView);

        expectedViews.add(createView("Tous", "http://myjenkins/"));

        assertThat(actualViews).containsAll(expectedViews);
        assertThat(jenkins.getPrimaryView()).isEqualTo(createView("Tous", "http://myjenkins"));
    }

    @Test
    public void loadClassicView() throws Exception {
        List<Job> actualJobs = jsonParser.createJobs(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadClassicView.json")));

        List<Job> expectedJobs = new LinkedList<>();
        expectedJobs.add(new JobBuilder().job("sql-tools", "blue", "http://myjenkins/job/sql-tools/", true, true)
                .lastBuild("http://myjenkins/job/sql-tools/15/", 15, SUCCESS.getStatus(), false, "2012-04-02_15-26-29", 1477640156281l, 4386421l)
                .health("health-80plus", "0 tests en echec sur un total de 24 tests")
                .availableBuildTypes(EnumSet.of(BuildType.LAST)).get());
        expectedJobs.add(new JobBuilder().job("db-utils", "grey", "http://myjenkins/job/db-utils/", false, true).get());
        expectedJobs.add(new JobBuilder().job("myapp", "red", "http://myjenkins/job/myapp/", false, true)
                .lastBuild("http://myjenkins/job/myapp/12/", 12, FAILURE.getStatus(), true, "2012-04-02_16-26-29", 1477640156281l, 4386421l)
                .health("health-00to19", "24 tests en echec sur un total de 24 tests")
                .parameter("param1", "ChoiceParameterDefinition", "value1", "value1", "value2", "value3")
                .parameter("runIntegrationTest", "BooleanParameterDefinition", null)
                .availableBuildTypes(EnumSet.of(BuildType.LAST))
                .get());
        expectedJobs.add(new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", true, false)
                .lastBuild("http://myjenkins/job/swing-utils/5/", 5, FAILURE.getStatus(), false, "2012-04-02_10-26-29", 1477640156281l, 4386421l)
                .health("health20to39", "0 tests en echec sur un total de 24 tests")
                .parameter("dummyParam", "DummyParameterDefinition", null)
                .availableBuildTypes(EnumSet.of(BuildType.LAST))
                .get());

        assertThat(actualJobs).containsAll(expectedJobs);
    }

    @Test
    public void loadClassicViewWithEmptyBooleans() throws Exception {
        List<Job> actualJobs = jsonParser.createJobs(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManger_loadWithEmptyBooleans.json")));

        List<Job> expectedJobs = new LinkedList<>();
        expectedJobs.add(new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", false, false)
                .lastBuild("http://myjenkins/job/swing-utils/5/", 5, FAILURE.getStatus(), false, "2012-04-02_10-26-29", 1477640156281l, 4386421l)
                .health("health20to39", "0 tests en echec sur un total de 24 tests")
                .parameter("dummyParam", "DummyParameterDefinition", null)
                .availableBuildTypes(EnumSet.of(BuildType.LAST))
                .get());

        assertThat(actualJobs).containsAll(expectedJobs);
    }

    @Test
    public void loadClassicViewWithEmptyBuildDate() throws Exception {
        List<Job> actualJobs = jsonParser.createJobs(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManger_loadWithEmptyDate.json")));

        /*List<Job> expectedJobs = new LinkedList<Job>();
        expectedJobs.add(new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", false, false)
                .lastBuild("http://myjenkins/job/swing-utils/5/", "5", FAILURE.getStatus(), false, "")
                .health("health20to39", "0 tests en echec sur un total de 24 tests")
                .parameter("dummyParam", null, null)
                .get());*/

//        assertThat(actualJobs).containsAll(expectedJobs);; TODO create real object for assertions
    }

    @Test
    public void loadClassicViewWithEmptyBuildDate105() throws Exception {
        List<Job> actualJobs = jsonParser.createJobs(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadDataFor105.json")));

        /*List<Job> expectedJobs = new LinkedList<Job>();
        expectedJobs.add(new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", false, false)
                .lastBuild("http://myjenkins/job/swing-utils/5/", "5", FAILURE.getStatus(), false, "")
                .health("health20to39", "0 tests en echec sur un total de 24 tests")
                .parameter("dummyParam", null, null)
                .get());*/

//        assertThat(actualJobs).containsAll(expectedJobs);; TODO create real object for assertions
    }

    @Test
    public void loadCloudbeesView() throws Exception {
        List<Job> actualJobs = jsonParser.createCloudbeesViewJobs(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadCloudbeesView.json")));

        List<Job> expectedJobs = Arrays.asList(
                new JobBuilder().job("sql-tools", "blue", "http://myjenkins/job/sql-tools/", true, true)
                        .lastBuild("http://myjenkins/job/sql-tools/15/", 15, SUCCESS.getStatus(), false, "2012-04-02_15-26-29", 1477640156281l, 4386421l)
                        .health("health-80plus", "0 tests en echec sur un total de 24 tests")
                        .availableBuildTypes(EnumSet.of(BuildType.LAST)).get(),
                new JobBuilder().job("db-utils", "grey", "http://myjenkins/job/db-utils/", false, true).get(),
                new JobBuilder().job("myapp", "red", "http://myjenkins/job/myapp/", false, true)
                        .lastBuild("http://myjenkins/job/myapp/12/", 12, FAILURE.getStatus(), true, "2012-04-02_16-26-29", 1477640156281l, 4386421l)
                        .health("health-00to19", "24 tests en echec sur un total de 24 tests")
                        .parameter("param1", "ChoiceParameterDefinition", "value1", "value1", "value2", "value3")
                        .parameter("runIntegrationTest", "BooleanParameterDefinition", null)
                        .availableBuildTypes(EnumSet.of(BuildType.LAST))
                        .get(),
                new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", true, false)
                        .lastBuild("http://myjenkins/job/swing-utils/5/", 5, FAILURE.getStatus(), false, "2012-04-02_10-26-29", 1477640156281l, 4386421l)
                        .health("health20to39", "0 tests en echec sur un total de 24 tests")
                        .parameter("dummyParam", "DummyParameterDefinition", null)
                        .availableBuildTypes(EnumSet.of(BuildType.LAST))
                        .get());

        assertThat(actualJobs).containsAll(expectedJobs);
    }

    @Test
    public void parseAllComputer() throws Exception {
        final List<Computer> actualComputer = jsonParser.createComputers(IOUtils.toString(getClass().getResourceAsStream(
                "JsonRequestManager_computer.json")));

        final List<Computer> expectedComputer = new LinkedList<>();
        expectedComputer.add(Computer.builder().displayName("master").description("Jenkins Master-Node")
                .labels(Lists.newArrayList("Jenkins", "master", "windows"))
                .offline(false).build());
        expectedComputer.add(Computer.builder().displayName("Test").description("Simple Test Agent")
                .labels(Lists.newArrayList("Test", "remote"))
                .offline(true).build());

        assertThat(actualComputer).containsExactlyInAnyOrderElementsOf(expectedComputer);
    }

    @Test
    public void parseComputer() {
        final Computer actualComputer = jsonParser.createComputer("{\n" +
                "            \"_class\": \"hudson.model.Hudson$MasterComputer\",\n" +
                "            \"description\": \"Jenkins Master\",\n" +
                "            \"displayName\": \"master\",\n" +
                "            \"offline\": false\n" +
                "        }");

        final Computer expectedComputer = Computer.builder().displayName("master").description("Jenkins Master")
                .offline(false).build();
        assertThat(actualComputer).isEqualTo(expectedComputer);
    }

    @Test
    public void getServerUrl() {
        final String serverUrl = jsonParser.getServerUrl("{\"_class\":\"hudson.model.Hudson\"," +
                "\"nodeName\":\"\"," +
                "\"description\":\"Sample\"," +
                "\"url\":\"http://localhost:8080/jenkins/\"}");
        assertThat(serverUrl).isEqualTo("http://localhost:8080/jenkins/");
    }

    @Test
    public void getServerUrlForNullUrl() {
        final String serverUrl = jsonParser.getServerUrl("{\"_class\":\"hudson.model.Hudson\"," +
                "\"nodeName\":\"\"," +
                "\"description\":\"Sample\"," +
                "\"url\":null}");
        assertThat(serverUrl).isEqualTo("");
    }

    @Test
    public void getServerUrlForNullUrlAndPrimaryView() {
        final String serverUrl = jsonParser.getServerUrl("{\"_class\":\"hudson.model.Hudson\"," +
                "\"nodeName\":\"\"," +
                "\"description\":\"Sample\"," +
                "\"primaryView\":{\"_class\":\"hudson.model.AllView\",\"name\":\"all\",\"url\":\"http://localhost:8080/jenkins/view\"}," +
                "\"url\":null}");
        assertThat(serverUrl).isEqualTo("http://localhost:8080/jenkins/view");
    }

    @Test
    public void testLoadJob() throws Exception {
        Job actualJob = jsonParser.createJob(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJob.json")));
        final Job expectedJob = new JobBuilder()
                .job("config-provider-model", "blue", "http://ci.jenkins-ci.org/job/config-provider-model/", false, true)
                .fullDisplayName("config-provider-model")
                .lastBuild("http://ci.jenkins-ci.org/job/config-provider-model/8/", 8, "SUCCESS", false, "2012-04-02_16-26-29", 1477640156281l, 4386421l)
                .health("health-80plus", "0 tests en echec sur un total de 24 tests")
                .availableBuildTypes(EnumSet.of(BuildType.LAST))
                .get();
        assertThat(actualJob).isEqualTo(expectedJob);
    }

    @Test
    public void testLoadJobWithFullDisplayName() throws Exception {
        Job actualJob = jsonParser.createJob(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJobWithFullDisplayName.json")));
        final Job expectedJob = new JobBuilder()
                .job("config-provider-model", "blue", "http://ci.jenkins-ci.org/job/config-provider-model/", false, true)
                .fullDisplayName("config-provider-model-full")
                .lastBuild("http://ci.jenkins-ci.org/job/config-provider-model/8/", 8, "SUCCESS", false, "2012-04-02_16-26-29", 1477640156281l, 4386421l)
                .health("health-80plus", "0 tests en echec sur un total de 24 tests")
                .availableBuildTypes(EnumSet.of(BuildType.LAST))
                .get();
        assertThat(actualJob).isEqualTo(expectedJob);
    }

    @Test
    public void testLoadJobWithAllLastBuilds() throws Exception {
        Job actualJob = jsonParser.createJob(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJobWithAllLastBuilds.json")));
        final Job expectedJob = new JobBuilder()
                .job("config-provider-model", "blue", "http://ci.jenkins-ci.org/job/config-provider-model/", false, true)
                .fullDisplayName("config-provider-model-full")
                .lastBuild("http://ci.jenkins-ci.org/job/config-provider-model/8/", 8, "SUCCESS", false, "2012-04-02_16-26-29", 1477640156281l, 4386421l)
                .health("health-80plus", "0 tests en echec sur un total de 24 tests")
                .availableBuildTypes(EnumSet.allOf(BuildType.class))
                .get();
        assertThat(actualJob).isEqualTo(expectedJob);
    }

    @Test
    public void oadJobWithLastFailedBuildIsNull() throws Exception {
        Job actualJob = jsonParser.createJob(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJobWithLastFailedBuildIsNull.json")));
        final Job expectedJob = new JobBuilder()
                .job("config-provider-model", "blue", "http://ci.jenkins-ci.org/job/config-provider-model/", false, true)
                .fullDisplayName("config-provider-model-full")
                .lastBuild("http://ci.jenkins-ci.org/job/config-provider-model/8/", 8, "SUCCESS", false, "2012-04-02_16-26-29", 1477640156281l, 4386421l)
                .health("health-80plus", "0 tests en echec sur un total de 24 tests")
                .availableBuildTypes(EnumSet.of(BuildType.LAST, BuildType.LAST_SUCCESSFUL))
                .get();
        assertThat(actualJob).isEqualTo(expectedJob);
    }

    @Test
    public void testLoadJobForJenkins2() throws Exception {
        Job actualJob = jsonParser.createJob(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJob_Jenkins2.json")));
        final Build lastBuild = Build.builder()
                .url("http://localhost:8080/job/Simple%20Jenkins%20Test/25/")
                .number(25)
                .buildDate(new Date(1580631718719L))
                .status(BuildStatusEnum.UNSTABLE)
                .building(false)
                .timestamp(new Date(1580631718719L))
                .duration(39731L)
                .displayName("#25")
                .fullDisplayName("Parent -> Simple Jenkins Test #25")
                .build();
        final Job expected = new JobBuilder()
                .job("Simple Jenkins Test", "yellow", "http://localhost:8080/job/Simple%20Jenkins%20Test/", false, true)
                .lastBuild(lastBuild)
                .health("health-40to59", "Testergebnis: 1 Test von 2 Tests fehlgeschlagen.")
                .displayName("Simple Jenkins Test")
                .fullName("Parent/Simple Jenkins Test")
                .fullDisplayName("Parent -> Simple Jenkins Test")
                .availableBuildTypes(EnumSet.of(BuildType.LAST))
                .get();
        assertThat(actualJob).isEqualTo(expected);
    }

    @Test
    public void testBugWithNullLastBuildAndEmptyHealthReport() throws Exception {
        List<Job> actualJobs = jsonParser.createJobs(IOUtils.toString(getClass().getResourceAsStream("bugWithEmptyHealthReportAndNullLastBuild.json")));


        List<Job> expectedJobs = Arrays.asList(
                new JobBuilder()
                        .job("abris", "blue", "http://jenkins.home.lobach.info:8080/job/abris/", false, true)
                        .lastBuild("http://jenkins.home.lobach.info:8080/job/abris/80/", 80, "SUCCESS", false, "2012-11-04_14-56-10", 1477640156281l, 4386421l)
                        .health("health-20to39", "Clover Coverage: Elements 23% (4292/18940)")
                        .availableBuildTypes(EnumSet.of(BuildType.LAST))
                        .get(),
                new JobBuilder()
                        .job("php-template", "disabled", "http://jenkins.home.lobach.info:8080/job/php-template/", false, false)
                        .get(),
                new JobBuilder()
                        .job("zfImageFilter", "blue", "http://jenkins.home.lobach.info:8080/job/zfImageFilter/", false, true)
                        .lastBuild("http://jenkins.home.lobach.info:8080/job/zfImageFilter/14/", 14, "SUCCESS", false, "2011-10-13_11-16-52", 1477640156281l, 4386421l)
                        .health("health-00to19", "Clover Coverage: Statements 7% (10/136)")
                        .availableBuildTypes(EnumSet.of(BuildType.LAST))
                        .get()

        );

        assertThat(actualJobs).containsAll(expectedJobs);
    }

    @Test
    public void testBugWithManyParameters() throws Exception {
        List<Job> actualJobs = jsonParser.createJobs(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJobManyParameters.json"), "utf-8"));

        List<Job> expectedJobs = Arrays.asList(
                new JobBuilder()
                        .job("DummyProject", "red", "http://localhost:8484/jenkins/job/DummyProject/", false, true)
                        .displayName("Dummy Project")
                        .lastBuild("http://localhost:8484/jenkins/job/DummyProject/26/", 26, "FAILURE", false, "2011-12-01_16-53-48", 1477640156281l, 4386421l)
                        .health("health-00to19", "Stabilité du build: Tous les builds récents ont échoué.")
                        .parameter("runIntegrationTest", "BooleanParameterDefinition", "true")
                        .parameter("environment", "ChoiceParameterDefinition", "itg", "itg", "prp", "prd", "bench")
                        .parameter("tag", "StringParameterDefinition", "")
                        .parameter("pass", "PasswordParameterDefinition", null)
                        .parameter("file", "FileParameterDefinition", null)
                        .parameter("parameterWithNullAsDefaultValue", "TextParameterDefinition", null)
                        .parameter("runner", "RunParameterDefinition", null)
                        .parameter("tag", "ListSubversionTagsParameterDefinition", null)
                        .availableBuildTypes(EnumSet.of(BuildType.LAST))
                        .get()

        );
        assertThat(actualJobs).containsAll(expectedJobs);
    }

    @Test
    public void testLoadJobWithNullAction() throws Exception {
        Job actualJob = jsonParser.createJob(IOUtils.toString(getClass().getResourceAsStream(
                "JsonRequestManager_jobWithNullAction.json")));
        final String buildUrl = "http://localhost/job/test/8/";
        final Job expectedJob = new JobBuilder()
                .job("test", "blue", "http://localhost/job/test/", false, true)
                .lastBuild(buildUrl, 8, "SUCCESS", false, "2012-04-02_16-26-29", 1477640156281l, 4386421l)
                .availableBuildTypes(EnumSet.of(BuildType.LAST))
                .get();
        assertThat(actualJob).isNotNull();
        final Build lastBuild = actualJob.getLastBuild();
        assertThat(lastBuild).isNotNull();
        assertThat(lastBuild.getUrl()).isEqualTo(buildUrl);
        assertThat(lastBuild.getBuildParameterList()).hasSize(1);
        assertThat(lastBuild.getBuildParameterList()).containsExactly(BuildParameter.of("Test Parameter", null,
                buildUrl));
    }

    @Test
    public void testLoadJobExtensibleChoiceParameter() throws Exception {
        Job actualJob = jsonParser.createJob(IOUtils.toString(getClass().getResourceAsStream(
                "JobWithExtensibleChoiceParameter.json")));
        assertThat(actualJob).isNotNull();
        assertThat(actualJob.getParameters()).hasSize(2);
        final JobParameter firstParameter = actualJob.getParameters().get(0);
        final JobParameter secondParameter = actualJob.getParameters().get(1);
        assertThat(firstParameter.getName()).isEqualTo("ExtensibleChoice");
        assertThat(firstParameter.getDefaultValue()).isEqualTo("Test");
        assertThat(secondParameter.getName()).isEqualTo("SimpleChoice");
        assertThat(secondParameter.getDefaultValue()).isEqualTo("Hello");
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        jsonParser = new JenkinsJsonParser();
    }
}
