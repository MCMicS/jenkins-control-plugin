package org.codinjutsu.tools.jenkins.logic;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.model.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class JenkinsJsonParser {

    private static final Logger LOG = Logger.getLogger(JenkinsJsonParser.class);

    private static final String JOBS = "jobs";
    private static final String JOB_NAME = "name";
    private static final String JOB_HEALTH = "healthReport";
    private static final String JOB_HEALTH_ICON = "iconUrl";
    private static final String JOB_HEALTH_DESCRIPTION = "description";
    private static final String JOB_URL = "url";
    private static final String JOB_COLOR = "color";
    private static final String JOB_LAST_BUILD = "lastBuild";
    private static final String JOB_IS_BUILDABLE = "buildable";
    private static final String JOB_IS_IN_QUEUE = "inQueue";
    private static final String VIEWS = "views";
    private static final String PRIMARY_VIEW = "primaryView";
    private static final String VIEW_NAME = "name";
    private static final String VIEW_URL = "url";
    private static final String BUILD_IS_BUILDING = "building";
    private static final String BUILD_ID = "id";
    private static final String BUILD_RESULT = "result";
    private static final String BUILD_URL = "url";
    private static final String BUILD_NUMBER = "number";
    private static final String PARAMETER_PROPERTY = "property";
    private static final String PARAMETER_DEFINITIONS = "parameterDefinitions";
    private static final String PARAMETER_NAME = "name";
    private static final String PARAMETER_TYPE = "type";
    private static final String PARAMETER_DEFAULT_PARAM = "defaultParameterValue";
    private static final String PARAMETER_DEFAULT_PARAM_VALUE = "value";
    private static final String PARAMETER_CHOICE = "choice";

    private final JsonFactory factory;

    public JenkinsJsonParser() {
        factory = new JsonFactory();
    }

    public Jenkins createWorkspace(String jsonData, String serverUrl) {
        Jenkins jenkins = new Jenkins("", serverUrl);
        try {
            JsonParser jsonParser = factory.createJsonParser(jsonData);
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String currentName = jsonParser.getCurrentName();
                escapeFieldName(jsonParser);

                if (PRIMARY_VIEW.equals(currentName)) {
                    jenkins.setPrimaryView(getView(jsonParser));

                } else if (VIEWS.equals(currentName)) {
                    jenkins.setViews(getViews(jsonParser));
                }
            }
            jsonParser.close();
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            throw new RuntimeException(message, e);
        }
        return jenkins;
    }

    public Job createJob(String jsonData) {
        try {
            JsonParser jsonParser = factory.createJsonParser(jsonData);
            Job job = createJob(jsonParser);
            jsonParser.close();
            return job;
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }


    public List<Job> createViewJobs(String jsonData) {
        try {
            JsonParser jsonParser = factory.createJsonParser(jsonData);
            List<Job> jobs = createJobs(jsonParser);
            jsonParser.close();
            return jobs;
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            throw new RuntimeException(message, e);
        }
    }

    public List<Job> createCloudbeesViewJobs(String jsonData) {
        try {
            JsonParser jsonParser = factory.createJsonParser(jsonData);
            List<Job> jobs = new LinkedList<Job>();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                escapeFieldName(jsonParser);
                String currentName = jsonParser.getCurrentName();
                if (VIEWS.equals(currentName)) {
                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                        String currentNameInView = jsonParser.getCurrentName();
                        if (JOBS.equals(currentNameInView)) {
                            List<Job> returnedJobs = createJobs(jsonParser);
                            jobs.addAll(returnedJobs);
                        }
                    }
                }
            }

            jsonParser.close();
            return jobs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Job> createJobs(JsonParser jsonParser) throws IOException {
        List<Job> jobs = new LinkedList<Job>();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentName = jsonParser.getCurrentName();

            if (JOBS.equals(currentName)) {
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    jobs.add(createJob(jsonParser));
                }
            }
        }
        return jobs;
    }

    private Job createJob(JsonParser jsonParser) throws IOException {
        Job job = new Job();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String currentName = jsonParser.getCurrentName();
            escapeFieldName(jsonParser);

            if (JOB_NAME.equals(currentName)) {
                job.setName(jsonParser.getText());
            } else if (JOB_URL.equals(currentName)) {
                job.setUrl(jsonParser.getText());
            } else if (JOB_COLOR.equals(currentName)) {
                job.setColor(jsonParser.getText());
            } else if (JOB_HEALTH.equals(currentName)) {
                job.setHealth(getHealth(jsonParser));
            } else if (JOB_IS_BUILDABLE.equals(currentName)) {
                job.setBuildable(jsonParser.getBooleanValue());
            } else if (JOB_IS_IN_QUEUE.equals(currentName)) {
                job.setInQueue(jsonParser.getBooleanValue());
            } else if (JOB_LAST_BUILD.equals(currentName)) {
                job.setLastBuild(getLastBuild(jsonParser));
            } else if (PARAMETER_PROPERTY.equals(currentName)) {
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                        String currentNameInProperty = jsonParser.getCurrentName();
                        if (PARAMETER_DEFINITIONS.equals(currentNameInProperty)) {
                            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                JobParameter jobParameter = new JobParameter();
                                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                    escapeFieldName(jsonParser);
                                    String currentNameInParameterDefinition = jsonParser.getCurrentName();
                                    if (PARAMETER_DEFAULT_PARAM.equals(currentNameInParameterDefinition)) {
                                        if (JsonToken.VALUE_NULL.equals(jsonParser.getCurrentToken())) {
                                            continue;
                                        }
                                        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                            escapeFieldName(jsonParser);
                                            String currentNameInDefaultParameterValue = jsonParser.getCurrentName();
                                            if (PARAMETER_DEFAULT_PARAM_VALUE.equals(currentNameInDefaultParameterValue)) {
                                                jobParameter.setDefaultValue(jsonParser.getText());
                                            }
                                        }
                                    } else if (PARAMETER_NAME.equals(currentNameInParameterDefinition)) {
                                        jobParameter.setName(jsonParser.getText());
                                    } else if (PARAMETER_TYPE.equals(currentNameInParameterDefinition)) {
                                        jobParameter.setType(jsonParser.getText());
                                    } else if (PARAMETER_CHOICE.equals(currentNameInParameterDefinition)) {
                                        jobParameter.setChoices(createChoices(jsonParser));
                                    }
                                }
                                job.addParameter(jobParameter);
                            }
                        }
                    }
                }
            }
        }

        return job;
    }

    private List<String> createChoices(JsonParser jsonParser) throws IOException {
        List<String> choices = new LinkedList<String>();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            escapeFieldName(jsonParser);
            String text = jsonParser.getText();
            choices.add(text);
        }
        return choices;
    }


    private Job.Health getHealth(JsonParser jsonParser) throws IOException {
        Job.Health health = new Job.Health();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentNameInHealthReport = jsonParser.getCurrentName();
            if (JOB_HEALTH_DESCRIPTION.equals(currentNameInHealthReport)) {
                health.setDescription(jsonParser.getText());
            } else if (JOB_HEALTH_ICON.equals(currentNameInHealthReport)) {
                String jobHealthLevel = jsonParser.getText();
                if (StringUtils.isNotEmpty(jobHealthLevel)) {
                    if (jobHealthLevel.endsWith(".png"))
                        jobHealthLevel = jobHealthLevel.substring(0, jobHealthLevel.lastIndexOf(".png"));
                    else {
                        jobHealthLevel = jobHealthLevel.substring(0, jobHealthLevel.lastIndexOf(".gif"));
                    }
                } else {
                    jobHealthLevel = null;
                }
                health.setLevel(jobHealthLevel);
            }
        }
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            jsonParser.nextToken();
        }
        if (!StringUtils.isEmpty(health.getLevel())) {
            return health;
        } else {
            return null;
        }
    }

    private Build getLastBuild(JsonParser jsonParser) throws IOException {
        Build lastBuild = new Build();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentNameInLastBuild = jsonParser.getCurrentName();
            if (BUILD_ID.equals(currentNameInLastBuild)) {
                lastBuild.setBuildDate(jsonParser.getText());
            } else if (BUILD_IS_BUILDING.equals(currentNameInLastBuild)) {
                lastBuild.setBuilding(jsonParser.getBooleanValue());
            } else if (BUILD_NUMBER.equals(currentNameInLastBuild)) {
                lastBuild.setNumber(jsonParser.getIntValue());
            } else if (BUILD_RESULT.equals(currentNameInLastBuild)) {
                lastBuild.setStatus(jsonParser.getText());
            } else if (BUILD_URL.equals(currentNameInLastBuild)) {
                lastBuild.setUrl(jsonParser.getText());
            }
        }
        return lastBuild;
    }

    private List<View> getViews(JsonParser jsonParser) throws IOException {
        List<View> views = new LinkedList<View>();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            View view = getView(jsonParser);
            views.add(view);
        }
        return views;
    }

    private View getView(JsonParser jsonParser) throws IOException {
        View view = new View();
        view.setNested(false);
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentName = jsonParser.getCurrentName();
            if (VIEW_NAME.equals(currentName)) {
                view.setName(jsonParser.getText());
            } else if (VIEW_URL.equals(currentName)) {
                view.setUrl(jsonParser.getText());
            } else if (VIEWS.equals(currentName)) {
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    View nestedView = getNestedView(jsonParser);
                    view.addSubView(nestedView);
                }
            }
        }
        return view;
    }

    private View getNestedView(JsonParser jsonParser) throws IOException {
        View nestedView = new View();
        nestedView.setNested(true);
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentName = jsonParser.getCurrentName();
            if (VIEW_NAME.equals(currentName)) {
                nestedView.setName(jsonParser.getText());
            } else if (VIEW_URL.equals(currentName)) {
                nestedView.setUrl(jsonParser.getText());
            }
        }
        return nestedView;
    }

    private void escapeFieldName(JsonParser jsonParser) throws IOException {
        if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME) {
            jsonParser.nextToken();
        }
    }
}
