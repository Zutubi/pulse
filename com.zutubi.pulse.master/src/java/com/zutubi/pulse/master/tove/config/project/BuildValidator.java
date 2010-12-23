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
