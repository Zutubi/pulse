package com.zutubi.pulse.prototype;

import java.util.Map;

/**
 * Controls all loading and storing of project configuration, and acts as the
 * single point to access project configuration data.
 */
public interface ProjectConfigurationManager
{
    ProjectConfiguration getProject(long projectId);

    TemplateRecord getRecord(long projectId, String path);

    void setRecord(long projectId, String path, Map data);
}
