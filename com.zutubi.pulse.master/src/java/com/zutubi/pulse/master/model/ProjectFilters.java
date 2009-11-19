package com.zutubi.pulse.master.model;

import com.zutubi.util.Predicate;
import com.zutubi.util.InvertedPredicate;
import com.zutubi.util.ConjunctivePredicate;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * A collection of filters for projects.
 */
public class ProjectFilters
{
    /**
     * A filter that rejects project configurations that are not concrete.
     *
     * @return a filter that rejects project configurations that are not concrete.
     */
    public static Predicate<ProjectConfiguration> concrete()
    {
        return new Predicate<ProjectConfiguration>()
        {
            public boolean satisfied(ProjectConfiguration projectConfiguration)
            {
                return projectConfiguration.isConcrete();
            }
        };
    }

    /**
     * Returns true if the specified project satisfies the concrete filter.
     *
     * @param project   the project being tested.
     *
     * @return true if the project is concrete, false otherwise
     *
     * @see #concrete() 
     */
    public static boolean concrete(ProjectConfiguration project)
    {
        return concrete().satisfied(project);
    }

    /**
     * A filter that rejects projects with no associated configuration.
     *
     * @return a filter that rejects projects with no associated configuration.
     */
    public static Predicate<Project> notOrphaned()
    {
        return new InvertedPredicate(orphaned());
    }

    public static Predicate<Project> orphaned()
    {
        return new Predicate<Project>()
        {
            public boolean satisfied(Project project)
            {
                return project.getConfig() == null;
            }
        };
    }

    /**
     * A filter that rejects any project instances that are classified as not existing.
     * That is, they are null or they are orphaned.
     *
     * @return a filter that rejects projects that are considered to not exist.
     */
    public static Predicate<Project> exists()
    {
        return new ConjunctivePredicate<Project>(new Predicate<Project>()
        {
            public boolean satisfied(Project project)
            {
                return project != null;
            }
        }, notOrphaned());
    }

    public static boolean notExists(Project project)
    {
        return notExists().satisfied(project);
    }

    public static Predicate<Project> notExists()
    {
        return new InvertedPredicate(exists());
    }

    /**
     * Returns true if the specified project satisfies the exists fitler.
     *
     * @param project   the project being tested.
     *
     * @return true if the project exists, false otherwise.
     *
     * @see #exists() 
     */
    public static boolean exists(Project project)
    {
        return exists().satisfied(project);
    }
}
