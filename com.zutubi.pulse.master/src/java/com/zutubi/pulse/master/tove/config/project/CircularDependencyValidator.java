package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationValidationContext;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;
import com.zutubi.util.StringUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.LinkedList;

/**
 * A validator that checks to ensure that if a dependency to the specified project
 * is added that it does not create a circular dependency.
 */
@Wire
public class CircularDependencyValidator extends FieldValidatorSupport
{
    private ConfigurationProvider configurationProvider;

    protected void validateField(Object value) throws ValidationException
    {
        ConfigurationValidationContext context = (ConfigurationValidationContext) getValidationContext();

        String parentPath = context.getParentPath();
        if (parentPath == null)
        {
            // We have zero context information to work with here.  This occurs when the project is being saved
            // with dependency details.  If the project doesn't have a path, nothing can refer to it, so we
            // are clear.
            return;
        }

        ProjectConfiguration instance = configurationProvider.getAncestorOfType(parentPath, ProjectConfiguration.class);

        // check that there is no way that we can get from the project we are adding to the instance being configured.
        ProjectConfiguration candidate = (ProjectConfiguration) value;
        LinkedList<ProjectConfiguration> circularPath = new LinkedList<ProjectConfiguration>();
        if (findCircularDependency(instance, candidate, circularPath))
        {
            String path = StringUtils.join("->", CollectionUtils.map(circularPath, new Mapping<ProjectConfiguration, String>()
            {
                public String map(ProjectConfiguration project)
                {
                    return project.getName();
                }
            }));
            addError("circular.error", path);
        }
    }

    /**
     * Returns true if a circular dependency is located, false otherwise.
     *
     * @param instance  the project configuration instance that is being configured.
     * @param candidate the candidate target project of the new dependency.
     * @param path      a list used to record the path to a circular dependency if it is detected.
     *
     * @return true if a circular dependency is located, false otherwise.
     */
    private boolean findCircularDependency(ProjectConfiguration instance, ProjectConfiguration candidate, LinkedList<ProjectConfiguration> path)
    {
        if (candidate.hasDependencies())
        {
            for (DependencyConfiguration dependency : candidate.getDependencies().getDependencies())
            {
                ProjectConfiguration dependencyProject = dependency.getProject();
                if (dependencyProject.equals(instance))
                {
                    // circular dependency detected, stop.
                    path.addFirst(candidate);
                    return true;
                }

                if (findCircularDependency(instance, dependencyProject, path))
                {
                    path.addFirst(candidate);
                    return true;
                }
            }
        }
        return false;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
