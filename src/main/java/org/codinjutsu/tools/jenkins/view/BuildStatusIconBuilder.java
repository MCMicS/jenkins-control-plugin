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

package org.codinjutsu.tools.jenkins.view;

import javax.swing.*;

public class BuildStatusIconBuilder {

    private Icon icon;
    private String toolTipText;
    private int nbToDisplay;

    public BuildStatusIconBuilder() {
    }

    public BuildStatusIconBuilder icon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public BuildStatusIconBuilder toolTip(String toolTipText) {
        this.toolTipText = toolTipText;
        return this;
    }

    public BuildStatusIconBuilder withNumber(int nbToDisplay) {
        this.nbToDisplay = nbToDisplay;
        return this;
    }



}
