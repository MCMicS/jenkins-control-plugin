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

import org.codinjutsu.tools.jenkins.logic.BuildStatusAggregator;

import javax.swing.*;
import java.awt.*;


public class BuildSummaryPanel extends JPanel{
    private JPanel rootPanel;
    private JLabel brokenBuildsLabel;
    private JLabel succeededBuildsLabel;
    private JLabel unstableBuildsLabel;


    public BuildSummaryPanel() {
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }

    public void setInformation(BuildStatusAggregator aggregator) {
        brokenBuildsLabel.setText(String.valueOf(aggregator.getNbBrokenBuilds()));
        succeededBuildsLabel.setText(String.valueOf(aggregator.getNbSucceededBuilds()));
        unstableBuildsLabel.setText(String.valueOf(aggregator.getNbUnstableBuilds()));
    }
}
