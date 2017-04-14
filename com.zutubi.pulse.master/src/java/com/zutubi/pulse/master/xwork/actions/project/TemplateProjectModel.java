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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.UnaryProcedure;
import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * JSON-encodable object representing the current state of a template project
 * and its descendants.
 */
public class TemplateProjectModel extends ProjectModel
{
    private static final Messages I18N = Messages.getInstance(TemplateProjectModel.class);

    private boolean collapsed;
    private List<ProjectModel> children = new LinkedList<ProjectModel>();

    public TemplateProjectModel(ProjectsModel group, String name, boolean collapsed)
    {
        super(group, name);
        this.collapsed = collapsed;
    }

    public boolean isCollapsed()
    {
        return collapsed;
    }

    @JSON
    public List<ProjectModel> getChildren()
    {
        return children;
    }

    public void addChild(ProjectModel child)
    {
        children.add(child);
        child.setParent(this);
    }

    public boolean isConcrete()
    {
        return false;
    }

    public ProjectHealth latestHealth()
    {
        ProjectHealth health = ProjectHealth.UNKNOWN;
        for (ProjectModel child: children)
        {
            ProjectHealth childHealth = child.latestHealth();
            if(childHealth.ordinal() > health.ordinal())
            {
                health = childHealth;
            }
        }

        return health;
    }

    public int getUnknownCount()
    {
        return getCount(ProjectHealth.UNKNOWN);
    }

    public int getOkCount()
    {
        return getCount(ProjectHealth.OK);
    }

    public int getWarningCount()
    {
        return getCount(ProjectHealth.WARNINGS);
    }

    public int getBrokenCount()
    {
        return getCount(ProjectHealth.BROKEN);
    }

    public String getBuilding()
    {
        int count = getCount(ResultState.IN_PROGRESS);
        if (count == 0)
        {
            return I18N.format("builds.inprogress.none");
        }
        else if (count == 1)
        {
            return I18N.format("builds.inprogress.one");
        }
        else
        {
            return I18N.format("builds.inprogress", count);
        }
    }
    
    public ResultState latestState()
    {
        return null;
    }

    public int getCount(ProjectHealth health)
    {
        int count = 0;
        for(ProjectModel child: children)
        {
            count += child.getCount(health);
        }

        return count;
    }

    public int getCount(final ResultState state)
    {
        int count = 0;
        for(ProjectModel child: children)
        {
            count += child.getCount(state);
        }

        return count;
    }

    public void forEach(UnaryProcedure<ProjectModel> proc)
    {
        proc.run(this);
        for(ProjectModel child: children)
        {
            child.forEach(proc);
        }
    }
}
