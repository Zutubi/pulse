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

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.tove.config.ConfigurationValidationContext;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.validation.validators.StringFieldValidatorSupport;

/**
 * Validates that the string represents a valid build number for the
 * project in the validation context.
 */
public class BuildValidator extends StringFieldValidatorSupport
{
    private ProjectManager projectManager;
    private BuildManager buildManager;

    public void validateStringField(String build)
    {
        ConfigurationValidationContext context = (ConfigurationValidationContext) getValidationContext();
        String[] parentPathElements = PathUtils.getPathElements(context.getParentPath());
        if(parentPathElements.length < 2)
        {
            context.addFieldError(getFieldName(), context.getText("invalid.parent.path", context.getParentPath()));
            return;
        }

        String projectName = parentPathElements[1];
        Project project = projectManager.getProject(projectName, true);
        if (project == null)
        {
            context.addFieldError(getFieldName(), context.getText("invalid.project", projectName));
            return;
        }

        BuildResult buildResult = buildManager.getByProjectAndVirtualId(project, build);
        if(buildResult == null || !buildResult.completed())
        {
            context.addFieldError(getFieldName(), context.getText("invalid.build", build));
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
