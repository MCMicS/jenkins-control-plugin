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

import org.codinjutsu.tools.jenkins.model.View;

import javax.swing.*;
import java.util.List;

class JenkinsViewComboboxModel extends DefaultComboBoxModel {


    public JenkinsViewComboboxModel(List<View> views) {
        super(views.toArray());
    }

    @Override
    public void setSelectedItem(Object obj) {
        if (obj == null) return;

        View view = (View) obj;
        if (!view.hasNestedView()) {
            super.setSelectedItem(obj);
        }
    }
}
