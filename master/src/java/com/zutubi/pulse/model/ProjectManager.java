/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import org.acegisecurity.annotation.Secured;

import java.util.List;

/**
 * 
 *
 */
public interface ProjectManager extends EntityManager<Project>
{
    /**
     * Looks up the project of the given name.
     *
     * @param name the name of the project to find
     * @return the relevant project, or null if not found
     */
    Project getProject(String name);

    Project getProject(long id);

    List<Project> getAllProjects();

    List<Project> getProjectsWithNameLike(String s);

    /**
     * Retrieve the build specification
     *
     * @param id uniquely identifying the build specification.
     *
     * @return the requested build specification or null if it does not exist.
     */
    BuildSpecification getBuildSpecification(long id);

    /**
     * Retrieve the build specification
     *
     * @param name uniquely identifying the build specification.
     *
     * @return the requested build specification or null if it does not exist.
     */
    BuildSpecification getBuildSpecification(String name);

    void initialise();

    /**
     * Deletes a build specification *and* all triggers that refer to it.
     *
     * @param project   the project to delete the specification from
     * @param specId    the identifier of the specification to delete
     * @throws com.zutubi.pulse.core.PulseRuntimeException
     *          if there is no
     *          specification with the given identifier, or an error occurs
     *          while deleting it
     */
    @Secured({"ACL_PROJECT_WRITE"})
    void deleteBuildSpecification(Project project, long specId);

    @Secured({"ACL_PROJECT_WRITE"})
    void deleteArtifact(Project project, long id);

    void buildCommenced(long projectId);

    void buildCompleted(long projectId);

    @Secured({"ACL_PROJECT_WRITE"})
    Project pauseProject(Project project);

    @Secured({"ACL_PROJECT_WRITE"})
    void resumeProject(Project project);

    @Secured({"ROLE_ADMINISTRATOR", "ACL_PROJECT_WRITE"})
    void save(Project project);

    @Secured({"ROLE_ADMINISTRATOR"})
    void delete(Project project);

    @Secured({"ACL_PROJECT_WRITE"})
    void checkWrite(Project project);

    /**
     * Creates and saves a project that is a replica of the given project,
     * but with the given name.
     *
     * @param project the project to copy
     * @param name    the name of the new project
     * @return the new project
     */
    @Secured({"ROLE_ADMINISTRATOR"})
    Project cloneProject(Project project, String name, String description);
}
