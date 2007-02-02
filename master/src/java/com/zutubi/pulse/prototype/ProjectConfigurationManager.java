package com.zutubi.pulse.prototype;

/**
 * Controls all loading and storing of project configuration, and acts as the
 * single point to access project configuration data.
 */
public interface ProjectConfigurationManager
{
    // Top level of a project.  Does not correspond to a single object, as we
    // want it to be extensible.  Hence a map of records and not a record
    // itself (records always map to objects).
    ProjectConfiguration getProject(long projectId);

    // Get a specific record within a project, referenced by a path made up
    // of field names and map keys (i.e. subrecord names)
    TemplateRecord getRecord(long projectId, String path);
}
