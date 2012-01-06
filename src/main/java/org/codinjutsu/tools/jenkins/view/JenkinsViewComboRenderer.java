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
import java.awt.*;

class JenkinsViewComboRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        if (value instanceof View) {
            View view = (View) value;
            return super.getListCellRendererComponent(list, view.getName(), index, isSelected, cellHasFocus);
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}


//public abstract class ComboSeparatorsRenderer implements ListCellRenderer{
//    private ListCellRenderer delegate;
//    private JPanel separatorPanel = new JPanel(new BorderLayout());
//    private JSeparator separator = new JSeparator();
//
//    public ComboSeparatorsRenderer(ListCellRenderer delegate){
//        this.delegate = delegate;
//    }
//
//    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
//        Component comp = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//        if(index!=-1 && addSeparatorAfter(list, value, index)){ // index==1 if renderer is used to paint current value in combo
//            separatorPanel.removeAll();
//            separatorPanel.add(comp, BorderLayout.CENTER);
//            separatorPanel.add(separator, BorderLayout.SOUTH);
//            return separatorPanel;
//        }else
//            return comp;
//    }
//
//    protected abstract boolean addSeparatorAfter(JList list, Object value, int index);
//}