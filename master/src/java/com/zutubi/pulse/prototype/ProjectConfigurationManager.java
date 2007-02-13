package com.zutubi.pulse.prototype;

import com.zutubi.prototype.PrototypePath;

import java.util.Map;
import java.util.List;

/**
 * Controls all loading and storing of project configuration, and acts as the
 * single point to access project configuration data.
 */
public interface ProjectConfigurationManager
{
    ProjectConfiguration getProject(long projectId);

    TemplateRecord getRecord(PrototypePath path);

    void setRecord(PrototypePath path, Map data);

    List<String> getProjectConfigurationRoot();

    String getSymbolicName(PrototypePath path);
}
