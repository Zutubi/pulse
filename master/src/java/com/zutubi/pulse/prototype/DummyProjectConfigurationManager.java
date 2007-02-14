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
        // setup the initial configuration.
        recordTypeRegistry.register("svnConfig", SvnConfiguration.class);
        recordTypeRegistry.register("cvsConfig", CvsConfiguration.class);
        recordTypeRegistry.register("cleanupRuleConfig", CleanupRuleConfiguration.class);
        recordTypeRegistry.register("generalConfig", GeneralConfiguration.class);
        recordTypeRegistry.register("scmConfig", ScmConfiguration.class);

        // this is behaviour that will be moved into the scm configuration extension point manager.
        RecordTypeInfo scmTypeInfo = recordTypeRegistry.getInfo("scmConfig");
        scmTypeInfo.addExtension(recordTypeRegistry.getInfo("svnConfig"));
        scmTypeInfo.addExtension(recordTypeRegistry.getInfo("cvsConfig"));

        // configuration setup - this should be handled via dynamic type info.
        Map<String, String> projectScope = configRegistry.addScope("project");
        projectScope.put("general", "generalConfig");
        projectScope.put("cleanup", "cleanupRuleConfig");
        projectScope.put("scm", "scmConfig");

        // link the projectConfig to a dynamic type instance.
        Record project = new SingleRecord("projectConfig");
        recordManager.store("project/1", project);

        // setup default data.
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
    }

    public String getSymbolicName(Path path)
    {
        // resolve the path into an associated info, and if it is the correct type, return its symbolic name.
        List<String> pathElements = path.getPathElements();

        String scope = pathElements.get(0);
        String rootLevelConfig = pathElements.get(2);

        // the path starts with the built in project root configurations.
        String symbolicName = configRegistry.getScope(scope).get(rootLevelConfig);
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
                i++;
            }
            else
            {
                return null;
            }
        }
        return typeInfo.getSymbolicName();
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
