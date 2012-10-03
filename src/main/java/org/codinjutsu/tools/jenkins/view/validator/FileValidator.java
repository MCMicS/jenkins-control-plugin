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

package org.codinjutsu.tools.jenkins.view.validator;

import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;

import java.io.File;

public class FileValidator implements UIValidator<LabeledComponent<TextFieldWithBrowseButton>> {
    public void validate(LabeledComponent<TextFieldWithBrowseButton> component) throws ConfigurationException {
        String filepath = component.getComponent().getText();
        if (StringUtils.isEmpty(filepath)) {
            return;
        }
        File file = new File(filepath);
        if (!file.exists() || !file.isFile()) {
            throw new ConfigurationException(String.format("'%s' is not a file", filepath));
        }

    }
}
