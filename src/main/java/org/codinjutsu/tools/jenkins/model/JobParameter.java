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

package org.codinjutsu.tools.jenkins.model;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    private String name;

    private JobParameterType jobParameterType;

    private String defaultValue;

    private VirtualFile virtualFile;

    private final List<String> values = new LinkedList<String>();

    public JobParameter() {
    }

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

    public static JobParameter create(String paramName, String paramType, VirtualFile virtualFile) {
        JobParameter parameter = create(paramName, paramType, "", "");
        parameter.setVirtualFile(virtualFile);
        return parameter;
    }


    public void setChoices(String... choices) {
        Collections.addAll(values, choices);
    }

    public void setChoices(List<String> choices) {
        values.addAll(choices);
    }


    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public void setType(String paramType) {
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

    public List<String> getValues() {
        return values;
    }

    public void setName(String name) {
        this.name = name;
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
