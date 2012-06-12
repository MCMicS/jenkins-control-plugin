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
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.awt.*;


public class BuildSummaryPanel extends JPanel {
    private JPanel rootPanel;
    private JLabel brokenBuildsLabel;
    private JLabel succeededBuildsLabel;
    private JLabel unstableBuildsLabel;
    private JLabel abortedBuildsLabel;
    private JLabel weatherLabel;

    enum Health {
        upTo19(GuiUtil.loadIcon("health-00to19-Large.png"), "Heath is between 0 and 19%"),
        upTo39(GuiUtil.loadIcon("health-20to39-Large.png"), "Heath is between 20 and 39%"),
        upTo59(GuiUtil.loadIcon("health-40to59-Large.png"), "Heath is between 40 and 59%"),
        upTo79(GuiUtil.loadIcon("health-60to79-Large.png"), "Heath is between 60 and 79%"),
        upTo100(GuiUtil.loadIcon("health-80plus-Large.png"), "Heath is over 80%"),
        none(GuiUtil.loadIcon("null.png"), "");
        final Icon icon;
        final String tooltipText;

        Health(Icon icon, String tooltipText) {
            this.icon = icon;
            this.tooltipText = tooltipText;
        }
    }

    public BuildSummaryPanel() {
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        brokenBuildsLabel.setName("brokenBuilds");
        succeededBuildsLabel.setName("succeededBuilds");
        unstableBuildsLabel.setName("unstableBuilds");
        abortedBuildsLabel.setName("abortedBuilds");

        weatherLabel.setName("weather");
    }

    public void setInformation(BuildStatusAggregator aggregator) {
        brokenBuildsLabel.setText(String.valueOf(aggregator.getNbBrokenBuilds()));
        succeededBuildsLabel.setText(String.valueOf(aggregator.getNbSucceededBuilds()));
        unstableBuildsLabel.setText(String.valueOf(aggregator.getNbUnstableBuilds()));
        abortedBuildsLabel.setText(String.valueOf(aggregator.getNbAbortedBuilds()));

        Health health = computeHealth(aggregator);
        weatherLabel.setIcon(health.icon);
        weatherLabel.setToolTipText(health.tooltipText);
    }

    private static Health computeHealth(BuildStatusAggregator aggregator) {
        int sum = aggregator.sumAll();

        if (sum == 0) {
            return Health.none;
        }

        double ratio = (double) aggregator.getNbSucceededBuilds() / (double) sum;
        Health health;
        if (ratio < 0.2) {
            health = Health.upTo19;
        } else if (ratio < 0.4) {
            health = Health.upTo39;
        } else if (ratio < 0.6) {
            health = Health.upTo59;
        } else if (ratio < 0.8) {
            health = Health.upTo79;
        } else {
            health = Health.upTo100;
        }

        return health;
    }
}
