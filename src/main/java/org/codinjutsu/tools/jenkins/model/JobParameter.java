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

package org.codinjutsu.tools.jenkins.model;

import java.util.Collections;
import java.util.LinkedList;

public class JobParameter {

    public static enum JobParameterType {
        ChoiceParameterDefinition,
        BooleanParameterDefinition,
        StringParameterDefinition,
        PasswordParameterDefinition,
        FileParameterDefinition,
        TextParameterDefinition,
        RunParameterDefinition,
        ListSubversionTagsParameterDefinition
    }

    private final String name;

    private JobParameterType jobParameterType;

    private String defaultValue;

    private final LinkedList<String> values = new LinkedList<String>();

    private JobParameter(String name) {
        this.name = name;
    }

    public static JobParameter create(String paramName, String paramType, String defaultValue, String... choices) {
        JobParameter jobParameter = new JobParameter(paramName);
        jobParameter.setType(paramType);
        jobParameter.setDefaultValue(defaultValue);
        jobParameter.setChoices(choices);
        return jobParameter;
    }

    private void setChoices(String... choices) {
        Collections.addAll(values, choices);
    }


    private void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    private void setType(String paramType) {
        jobParameterType = evaluate(paramType);
    }


    public String getName() {
        return name;
    }

    public JobParameterType getJobParameterType() {
        return jobParameterType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public LinkedList<String> getValues() {
        return values;
    }

    private static JobParameterType evaluate(String paramTypeToEvaluate) {
        for (JobParameterType parameterType : JobParameterType.values()) {
            if (parameterType.name().equals(paramTypeToEvaluate)) {
                return parameterType;
            }
        }
        return null;
    }
}
