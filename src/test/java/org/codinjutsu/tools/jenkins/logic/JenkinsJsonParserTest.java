package org.codinjutsu.tools.jenkins.logic;

import org.apache.commons.io.IOUtils;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.FAILURE;
import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.SUCCESS;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class JenkinsJsonParserTest {

    private JenkinsJsonParser jsonParser;

    @Test
    public void loadJenkinsWorkSpace() throws Exception {
        Jenkins jenkins = jsonParser.createWorkspace(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadWorkspace.json")), "http://myjenkins");

        List<View> actualViews = jenkins.getViews();

        assertReflectionEquals(
                asList(View.createView("Framework", "http://myjenkins/view/Framework/"),
                        View.createView("Tools", "http://myjenkins/view/Tools/"),
                        View.createView("Tous", "http://myjenkins/")),
                actualViews);

        assertReflectionEquals(View.createView("Tous", "http://myjenkins"), jenkins.getPrimaryView());
    }

    @Test
    public void loadJenkinsWorkSpaceWithNestedViews() throws Exception {
        Jenkins jenkins = jsonParser.createWorkspace(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadWorkspaceWithNestedView.json")), "http://myjenkins");

        List<View> actualViews = jenkins.getViews();

        List<View> expectedViews = new LinkedList<View>();
        expectedViews.add(View.createView("Framework", "http://myjenkins/view/Framework/"));
        View nestedView = View.createView("NestedView", "http://myjenkins/view/NestedView/");

        nestedView.addSubView(View.createNestedView("FirstSubView", "http://myjenkins/view/NestedView/view/FirstSubView/"));
        nestedView.addSubView(View.createNestedView("SecondSubView", "http://myjenkins/view/NestedView/view/SecondSubView/"));
        expectedViews.add(nestedView);

        expectedViews.add(View.createView("Tous", "http://myjenkins/"));

        assertReflectionEquals(expectedViews, actualViews);

        assertReflectionEquals(View.createView("Tous", "http://myjenkins"), jenkins.getPrimaryView());
    }


    @Test
    public void loadClassicView() throws Exception {
        List<Job> actualJobs = jsonParser.createViewJobs(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadClassicView.json")));

        List<Job> expectedJobs = new LinkedList<Job>();
        expectedJobs.add(new JobBuilder().job("sql-tools", "blue", "http://myjenkins/job/sql-tools/", "true", "true")
                .lastBuild("http://myjenkins/job/sql-tools/15/", "15", SUCCESS.getStatus(), "false", "2012-04-02_15-26-29")
                .health("health-80plus", "0 tests en echec sur un total de 24 tests").get());
        expectedJobs.add(new JobBuilder().job("db-utils", "grey", "http://myjenkins/job/db-utils/", "false", "true").get());
        expectedJobs.add(new JobBuilder().job("myapp", "red", "http://myjenkins/job/myapp/", "false", "true")
                .lastBuild("http://myjenkins/job/myapp/12/", "12", FAILURE.getStatus(), "true", "2012-04-02_16-26-29")
                .health("health-00to19", "24 tests en echec sur un total de 24 tests")
                .parameter("param1", "ChoiceParameterDefinition", "value1", "value1", "value2", "value3")
                .parameter("runIntegrationTest", "BooleanParameterDefinition", null)
                .get());
        expectedJobs.add(new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", "true", "false")
                .lastBuild("http://myjenkins/job/swing-utils/5/", "5", FAILURE.getStatus(), "false", "2012-04-02_10-26-29")
                .health("health20to39", "0 tests en echec sur un total de 24 tests")
                .parameter("dummyParam", null, null)
                .get());

        assertReflectionEquals(expectedJobs, actualJobs);
    }

    @Test
    public void loadCloudbeesView() throws Exception {
        List<Job> actualJobs = jsonParser.createCloudbeesViewJobs(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadCloudbeesView.json")));

        List<Job> expectedJobs = Arrays.asList(
                new JobBuilder().job("sql-tools", "blue", "http://myjenkins/job/sql-tools/", "true", "true")
                        .lastBuild("http://myjenkins/job/sql-tools/15/", "15", SUCCESS.getStatus(), "false", "2012-04-02_15-26-29")
                        .health("health-80plus", "0 tests en echec sur un total de 24 tests").get(),
                new JobBuilder().job("db-utils", "grey", "http://myjenkins/job/db-utils/", "false", "true").get(),
                new JobBuilder().job("myapp", "red", "http://myjenkins/job/myapp/", "false", "true")
                        .lastBuild("http://myjenkins/job/myapp/12/", "12", FAILURE.getStatus(), "true", "2012-04-02_16-26-29")
                        .health("health-00to19", "24 tests en echec sur un total de 24 tests")
                        .parameter("param1", "ChoiceParameterDefinition", "value1", "value1", "value2", "value3")
                        .parameter("runIntegrationTest", "BooleanParameterDefinition", null)
                        .get(),
                new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", "true", "false")
                        .lastBuild("http://myjenkins/job/swing-utils/5/", "5", FAILURE.getStatus(), "false", "2012-04-02_10-26-29")
                        .health("health20to39", "0 tests en echec sur un total de 24 tests")
                        .parameter("dummyParam", null, null)
                        .get());

        assertReflectionEquals(expectedJobs, actualJobs);
    }

    @Test
    public void testLoadJob() throws Exception {
        Job actualJob = jsonParser.createJob(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJob.json")));

        assertReflectionEquals(new JobBuilder()
                .job("config-provider-model", "blue", "http://ci.jenkins-ci.org/job/config-provider-model/", "false", "true")
                .lastBuild("http://ci.jenkins-ci.org/job/config-provider-model/8/", "8", "SUCCESS", "false", "2012-04-02_16-26-29")
                .health("health-80plus", "0 tests en echec sur un total de 24 tests")
                .get(), actualJob);
    }


    @Test
    public void testBugWithNullLastBuildAndEmptyHealthReport() throws Exception {
        List<Job> actualJobs = jsonParser.createViewJobs(IOUtils.toString(getClass().getResourceAsStream("bugWithEmptyHealthReportAndNullLastBuild.json")));


        List<Job> expectedJobs = Arrays.asList(
                new JobBuilder()
                        .job("abris", "blue", "http://jenkins.home.lobach.info:8080/job/abris/", "false", "true")
                        .lastBuild("http://jenkins.home.lobach.info:8080/job/abris/80/", "80", "SUCCESS", "false", "2012-11-04_14-56-10")
                        .health("health-20to39", "Clover Coverage: Elements 23% (4292/18940)")
                        .get(),
                new JobBuilder()
                        .job("php-template", "disabled", "http://jenkins.home.lobach.info:8080/job/php-template/", "false", "false")
                        .get(),
                new JobBuilder()
                        .job("zfImageFilter", "blue", "http://jenkins.home.lobach.info:8080/job/zfImageFilter/", "false", "true")
                        .lastBuild("http://jenkins.home.lobach.info:8080/job/zfImageFilter/14/", "14", "SUCCESS", "false", "2011-10-13_11-16-52")
                        .health("health-00to19", "Clover Coverage: Statements 7% (10/136)")
                        .get()

        );

        assertReflectionEquals(expectedJobs, actualJobs);
    }


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jsonParser = new JenkinsJsonParser();
    }
}
