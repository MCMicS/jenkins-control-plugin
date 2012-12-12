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

package org.codinjutsu.tools.jenkins.view.util;

import org.codinjutsu.tools.jenkins.logic.BuildStatusAggregator;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;


public class BuildStatusIconTest {

    @Test
    public void noBuildsShouldDisplayGreyIcon() throws Exception {
        BuildStatusAggregator aggregatorMock = Mockito.mock(BuildStatusAggregator.class);
        Mockito.when(aggregatorMock.hasNoResults()).thenReturn(true);

        BuildStatusIcon statusIcon = (BuildStatusIcon) BuildStatusIcon.createIcon(aggregatorMock);
        assertEquals(GuiUtil.loadIcon("grey.png"), statusIcon.icon);
        assertEquals("No builds", statusIcon.toolTipText);
        assertEquals(0, statusIcon.numberToDisplay);
    }

    @Test
    public void brokenBuildsShouldDisplayRedIcon() throws Exception {
        BuildStatusAggregator aggregatorMock = Mockito.mock(BuildStatusAggregator.class);
        Mockito.when(aggregatorMock.hasNoResults()).thenReturn(false);
        Mockito.when(aggregatorMock.getNbBrokenBuilds()).thenReturn(4);
        Mockito.when(aggregatorMock.getNbUnstableBuilds()).thenReturn(2);

        BuildStatusIcon statusIcon = (BuildStatusIcon) BuildStatusIcon.createIcon(aggregatorMock);
        assertEquals(GuiUtil.loadIcon("red.png"), statusIcon.icon);
        assertEquals("4 broken builds", statusIcon.toolTipText);
        assertEquals(4, statusIcon.numberToDisplay);
    }

    @Test
    public void unstableBuildsShouldDisplayYellowIcon() throws Exception {
        BuildStatusAggregator aggregatorMock = Mockito.mock(BuildStatusAggregator.class);
        Mockito.when(aggregatorMock.hasNoResults()).thenReturn(false);
        Mockito.when(aggregatorMock.getNbBrokenBuilds()).thenReturn(0);
        Mockito.when(aggregatorMock.getNbUnstableBuilds()).thenReturn(2);

        BuildStatusIcon statusIcon = (BuildStatusIcon) BuildStatusIcon.createIcon(aggregatorMock);
        assertEquals(GuiUtil.loadIcon("yellow.png"), statusIcon.icon);
        assertEquals("2 unstable builds", statusIcon.toolTipText);
        assertEquals(2, statusIcon.numberToDisplay);
    }

    @Test
    public void noBrokenBuildsShouldDisplayYellowIcon() throws Exception {
        BuildStatusAggregator aggregatorMock = Mockito.mock(BuildStatusAggregator.class);
        Mockito.when(aggregatorMock.hasNoResults()).thenReturn(false);
        Mockito.when(aggregatorMock.getNbBrokenBuilds()).thenReturn(0);
        Mockito.when(aggregatorMock.getNbUnstableBuilds()).thenReturn(0);

        BuildStatusIcon statusIcon = (BuildStatusIcon) BuildStatusIcon.createIcon(aggregatorMock);
        assertEquals(GuiUtil.loadIcon("blue.png"), statusIcon.icon);
        assertEquals("No broken builds", statusIcon.toolTipText);
        assertEquals(0, statusIcon.numberToDisplay);
    }
}
