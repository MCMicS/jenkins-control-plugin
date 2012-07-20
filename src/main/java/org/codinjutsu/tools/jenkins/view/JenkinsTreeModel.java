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

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class JenkinsTreeModel extends DefaultTreeModel {

    private DefaultTreeModel customizedModel;

    private JobComparator jobStatusComparator;

    private boolean needsUpdate = true;

    public JenkinsTreeModel(DefaultMutableTreeNode rootNode) {
        super(rootNode);
    }

    @Override
    public void reload() {
        super.reload();
        getCustomizedModel().reload();
        needsUpdate = true;
    }


    private DefaultTreeModel getCustomizedModel() {
        if (needsUpdate) {
            needsUpdate = false;
            rebuildCustomizedModel();
        }
        return customizedModel;

    }

    private void rebuildCustomizedModel() {
        List<DefaultMutableTreeNode> jobNodeList = new LinkedList<DefaultMutableTreeNode>();

        DefaultMutableTreeNode sourceJobRoot = (DefaultMutableTreeNode) super.getRoot();
        DefaultMutableTreeNode customizedJobRoot = (DefaultMutableTreeNode) sourceJobRoot.clone();
        for (int i = 0; i < sourceJobRoot.getChildCount(); i++) {
            DefaultMutableTreeNode jobNodeChild = (DefaultMutableTreeNode) sourceJobRoot.getChildAt(i);
            DefaultMutableTreeNode targetJobNode = (DefaultMutableTreeNode)jobNodeChild.clone();
            jobNodeList.add(targetJobNode);
        }

        if (jobStatusComparator.isApplicable()) {
            Collections.sort(jobNodeList, jobStatusComparator);
        }

        for (DefaultMutableTreeNode jobNode : jobNodeList) {
            customizedJobRoot.add(jobNode);
        }

        customizedModel = new DefaultTreeModel(customizedJobRoot);

        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                customizedModel.addTreeModelListener((TreeModelListener) listeners[i + 1]);
            }
        }

        getCustomizedModel().reload();
    }

    @Override
    public int getChildCount(Object parent) {
        return getCustomizedModel().getChildCount(parent);
    }


    @Override
    public Object getRoot() {
        return getCustomizedModel().getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {
        return getCustomizedModel().getChild(parent, index);
    }


    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return getCustomizedModel().getIndexOfChild(parent, child);
    }

    @Override
    public boolean isLeaf(Object node) {
        return getCustomizedModel().isLeaf(node);
    }

    public void setJobStatusComparator(JobComparator jobStatusComparator) {
        this.jobStatusComparator = jobStatusComparator;
    }
}
