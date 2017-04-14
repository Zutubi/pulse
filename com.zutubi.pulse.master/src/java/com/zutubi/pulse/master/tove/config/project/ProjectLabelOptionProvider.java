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

package com.zutubi.pulse.master.tove.config.project;

import com.google.common.base.Function;
import com.zutubi.pulse.master.model.ProjectGroup;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.ListOptionProvider;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Collections2.transform;

/**
 */
public class ProjectLabelOptionProvider extends ListOptionProvider
{
    private static final Logger LOG = Logger.getLogger(ProjectLabelOptionProvider.class);

    private ProjectManager projectManager;

    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        Set<String> sortedLabels = new TreeSet<>(new Sort.StringComparator());
        if (context.getExistingInstance() != null)
        {
            try
            {
                Object propertyValue = property.getValue(context.getExistingInstance());
                if(propertyValue instanceof List)
                {
                    sortedLabels.addAll((List<String>)propertyValue);
                }
            }
            catch (Exception e)
            {
                LOG.severe(e);
            }
        }

        sortedLabels.addAll(transform(projectManager.getAllProjectGroups(), new Function<ProjectGroup, String>()
        {
            public String apply(ProjectGroup projectGroup)
            {
                return projectGroup.getName();
            }
        }));

        return new ArrayList<>(sortedLabels);
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
