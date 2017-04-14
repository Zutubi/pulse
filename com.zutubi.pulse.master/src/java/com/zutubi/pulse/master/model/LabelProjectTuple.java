/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.zutubi.util.StringUtils;

/**
 * Stores a (label, project template) pairing used to represent a collapsible
 * row on project summary views (e.g. the dashboard and browse views).
 * <p/>
 * Note that these tuples may become stale, as there is no foreign key
 * relationship for either of the components (they are pure configuration).
 * Clients should ignore stale items (and preferrably clean them).
 */
public class LabelProjectTuple
{
    private static final int NO_PROJECT = 0;
    
    /**
     * Label, may be empty to indicate "ungrouped" projects.
     */
    private String label;
    /**
     * Handle of the project template, may be zero to indicate the label
     * (group) itself is collapsed.
     */
    private long projectHandle;

    public LabelProjectTuple()
    {
    }

    public LabelProjectTuple(String label)
    {
        this(label, NO_PROJECT);
    }

    public LabelProjectTuple(String label, long projectHandle)
    {
        this.label = label;
        this.projectHandle = projectHandle;
    }

    public boolean isUnlabelled()
    {
        return !StringUtils.stringSet(label);
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public boolean isSpecificProject()
    {
        return projectHandle != NO_PROJECT;
    }

    public long getProjectHandle()
    {
        return projectHandle;
    }

    public void setProjectHandle(long projectHandle)
    {
        this.projectHandle = projectHandle;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        LabelProjectTuple that = (LabelProjectTuple) o;

        if (projectHandle != that.projectHandle)
        {
            return false;
        }
        return label.equals(that.label);
    }

    @Override
    public int hashCode()
    {
        int result = label.hashCode();
        result = 31 * result + (int) (projectHandle ^ (projectHandle >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return "(" + label + ", " + projectHandle + ")";
    }
}
