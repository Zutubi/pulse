package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.*;
import com.zutubi.prototype.Path;

import java.util.*;

/**
 *
 *
 */
public class DummyProjectConfigurationManager implements ProjectConfigurationManager
{
    private PrototypeConfigRegistry configRegistry;
    
    private RecordTypeRegistry recordTypeRegistry;

    private RecordManager recordManager;

    public DummyProjectConfigurationManager()
    {
    }

    public void init() throws InvalidRecordTypeException
    {
        recordTypeRegistry.register("svnConfig", SvnConfiguration.class);
        recordTypeRegistry.register("cvsConfig", CvsConfiguration.class);
        recordTypeRegistry.register("cleanupRuleConfig", CleanupRuleConfiguration.class);
        recordTypeRegistry.register("generalConfig", GeneralConfiguration.class);
        recordTypeRegistry.register("scmConfig", ScmConfiguration.class);
        
        // this is behaviour that will be moved into the scm configuration extension point manager.
        RecordTypeInfo scmTypeInfo = recordTypeRegistry.getInfo("scmConfig");
        scmTypeInfo.addExtension(recordTypeRegistry.getInfo("svnConfig"));
        scmTypeInfo.addExtension(recordTypeRegistry.getInfo("cvsConfig"));

        Record scm = new SingleRecord("svnConfig");
        scm.put("url", "http://www.zutubi.com");
        scm.put("password", "secret");
        scm.put("name", "Duuude");
        Record filters = new SingleRecord("");
        filters.put("0", "a");
        filters.put("1", "b");
        filters.put("2", "c");
        scm.put("filterPaths", filters);

        recordManager.store("project/1/scm", scm);

        Record general = new SingleRecord("generalConfig");
        general.put("name", "project name");
        general.put("description", "a simple dummy project for testing");
        general.put("url", "http://www.zutubi.com/roxor");

        recordManager.store("project/1/general", general);

        // configuration setup.
        Map<String, String> projectScope = configRegistry.addScope("project");
        projectScope.put("general", "generalConfig");
        projectScope.put("cleanup", "cleanupRuleConfig");
        projectScope.put("scm", "scmConfig");
    }

    public List<String> getProjectConfigurationRoot()
    {
        return configRegistry.getRoot("project");
    }

    public String getSymbolicName(Path path)
    {
        // resolve the path into an associated info, and if it is the correct type, return its symbolic name.
        List<String> pathElements = path.getPathElements();

        // the path starts with the built in project root configurations.
        String symbolicName = configRegistry.getScope("project").get(pathElements.get(2));
        if (pathElements.size() == 3)
        {
            return symbolicName;
        }

        // navigate through the type tree extracting the info as we go.
        RecordTypeInfo typeInfo = recordTypeRegistry.getInfo(symbolicName);
        for (int i = 3; i < pathElements.size(); i++)
        {
            RecordPropertyInfo propertyInfo = typeInfo.getProperty(pathElements.get(i));
            if (propertyInfo instanceof SubrecordRecordPropertyInfo)
            {
                typeInfo = ((SubrecordRecordPropertyInfo)propertyInfo).getSubrecordType();
            }
            else if (propertyInfo instanceof RecordMapRecordPropertyInfo)
            {
                typeInfo = ((RecordMapRecordPropertyInfo)propertyInfo).getRecordType();
            }
            else
            {
                return null;
            }
        }
        return typeInfo.getSymbolicName();
    }

    // Get a specific record within a project, referenced by a path made up
    // of field names and map keys (i.e. subrecord names)
    public Record getRecord(Path path)
    {
        return recordManager.load(path.toString());
    }

    public void setRecord(Path path, Map data)
    {
        Record record = getRecord(path);
        if (record == null)
        {
            record = new SingleRecord(getSymbolicName(path));
            recordManager.store(path.toString(), record);
        }
        record.putAll(data);
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }

    public void setConfigRegistry(PrototypeConfigRegistry configRegistry)
    {
        this.configRegistry = configRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
