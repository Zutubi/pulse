package com.zutubi.pulse.prototype;

import com.zutubi.prototype.Path;
import com.zutubi.pulse.prototype.record.Record;

import java.util.Map;
import java.util.List;

/**
 * Controls all loading and storing of project configuration, and acts as the
 * single point to access project configuration data.
 */
public interface ProjectConfigurationManager
{
    // should be moved into the record manager.
    Record getRecord(Path path);

    // should be moved into the record manager.
    void setRecord(Path path, Map data);

    // should be moved into the configuration manager.
    List<String> getProjectConfigurationRoot();

    // should be moved: recordManager.getRecord(path).getSymbolicName() | registry.getType(path).getSymbolicName();
    String getSymbolicName(Path path);
}
