package com.zutubi.pulse.prototype;

import com.zutubi.prototype.Path;

/**
 * Controls all loading and storing of project configuration, and acts as the
 * single point to access project configuration data.
 */
public interface ProjectConfigurationManager
{
    // should be moved: recordManager.getRecord(path).getSymbolicName() | registry.getType(path).getSymbolicName();
    String getSymbolicName(Path path);
}
