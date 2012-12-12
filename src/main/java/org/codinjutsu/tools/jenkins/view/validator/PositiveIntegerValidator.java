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

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;

import javax.swing.*;

public class PositiveIntegerValidator implements UIValidator<JTextField> {
    public void validate(JTextField component) throws ConfigurationException {
        String value = component.getText();
        if (component.isEnabled() && StringUtils.isNotEmpty(value)) {    //TODO A revoir
            try {
                int intValue = Integer.parseInt(value);
                if (intValue < 0)
                    throw new ConfigurationException(String.format("'%s' is not a positive integer", value));
            } catch (NumberFormatException ex) {
                throw new ConfigurationException(String.format("'%s' is not a positive integer", value));
            }
        }
    }
}