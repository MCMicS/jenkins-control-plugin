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

package org.codinjutsu.tools.jenkins.view.util;

import com.intellij.ui.LayeredIcon;
import com.intellij.util.ui.AnimatedIcon;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;

public class BuildAnimatedIcon extends AnimatedIcon {

    public static final int CYCLE_LENGTH = 1500;
    private static final int INTER_CYCLE_GAP = 100;
    private static final int MAX_REPEAT_COUNT = 5;

    private static final Icon[] SMALL_ICONS = {
            new ProcessIcon(GuiUtil.loadIcon("red.png"), null),
            new ProcessIcon(GuiUtil.loadIcon("null.png"), null)
    };

    private static final Icon SMALL_PASSIVE_ICON = GuiUtil.loadIcon("jenkins_logo.png");
    private boolean myUseMask;

    public BuildAnimatedIcon(@NonNls String name) {
        this(name, SMALL_ICONS, SMALL_PASSIVE_ICON);
    }

    private BuildAnimatedIcon(@NonNls String name, Icon[] icons, Icon passive) {
        super(name);

        init(icons, passive, CYCLE_LENGTH, INTER_CYCLE_GAP, MAX_REPEAT_COUNT);
        setUseMask(false);
    }

    public BuildAnimatedIcon setUseMask(boolean useMask) {
        myUseMask = useMask;
        return this;
    }

    @Override
    protected void paintIcon(Graphics g, Icon icon, int x, int y) {
        if (icon instanceof ProcessIcon) {
            ((ProcessIcon) icon).setLayerEnabled(0, myUseMask);
        }
        super.paintIcon(g, icon, x, y);

        if (icon instanceof ProcessIcon) {
            ((ProcessIcon) icon).setLayerEnabled(0, false);
        }
    }

    private static class ProcessIcon extends LayeredIcon {
        private ProcessIcon(Icon mask, Icon stepIcon) {
            super(mask, stepIcon);
        }
    }

    public boolean isDisposed() {
        return myAnimator.isDisposed();
    }
}
