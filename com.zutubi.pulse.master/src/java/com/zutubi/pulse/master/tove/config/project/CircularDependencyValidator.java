package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationValidationContext;
import com.zutubi.tove.config.api.Configurations;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;

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
        if (findCircularDependency(instance, candidate, circularPath, new HashSet<Long>()))
        {
            String path = StringUtils.join("->", transform(circularPath, Configurations.toConfigurationName()));
            addError("circular.error", path);
        }
    }

    /**
     * Returns true if a circular dependency is located, false otherwise.
     *
     * @param instance  the project configuration instance that is being configured.
     * @param candidate the candidate target project of the new dependency.
     * @param path      a list used to record the path to a circular dependency if it is detected.
     * @param seenHandles set of all handles processed in the search, used to protect against
     *                    already-existing cycles
     *
     * @return true if a circular dependency is located, false otherwise.
     */
    private boolean findCircularDependency(ProjectConfiguration instance, ProjectConfiguration candidate, LinkedList<ProjectConfiguration> path, Set<Long> seenHandles)
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

                if (seenHandles.contains(dependencyProject.getHandle()))
                {
                    return false;
                }

                seenHandles.add(dependencyProject.getHandle());

                if (findCircularDependency(instance, dependencyProject, path, seenHandles))
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
