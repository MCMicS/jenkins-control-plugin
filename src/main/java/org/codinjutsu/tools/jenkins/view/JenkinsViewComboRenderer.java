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

package org.codinjutsu.tools.jenkins.view;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.jenkins.model.FavoriteView;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JenkinsViewComboRenderer extends ColoredListCellRenderer<View> {

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends View> list, View view, int index, boolean selected, boolean hasFocus) {
        append(view.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

        if (view instanceof FavoriteView) {
            setIcon(JenkinsTreeRenderer.FAVORITE_ICON);
        }

    }
}
