package com.zutubi.prototype.config;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.Traversable;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 *
 */
public class ConfigurationRegistry
{
    private TypeRegistry typeRegistry;
    private RecordManager recordManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;

    private Map<String, Traversable> scopes = new HashMap<String, Traversable>();

    public void init() throws TypeException
    {
        // scm configuration
        CompositeType scmConfig = typeRegistry.register("scmConfig", ScmConfiguration.class);
        typeRegistry.register("svnConfig", SvnConfiguration.class);
        typeRegistry.register("cvsConfig", CvsConfiguration.class);
        typeRegistry.register("perforceConfig", PerforceConfiguration.class);

        // sort out the extensions.
        scmConfig.addExtension("svnConfig");
        scmConfig.addExtension("cvsConfig");
        scmConfig.addExtension("perforceConfig");

        // general project configuration
        typeRegistry.register("generalConfig", GeneralConfiguration.class);

        // cleanup rule configuration
        typeRegistry.register("cleanupRuleConfig", CleanupRuleConfiguration.class);

        // commit message processors.
        CompositeType commitConfig = typeRegistry.register("commitConfig", CommitMessageConfiguration.class);
        typeRegistry.register("jiraCommitConfig", JiraCommitMessageConfiguration.class);
        typeRegistry.register("customCommitConfig", CustomCommitMessageConfiguration.class);

        commitConfig.addExtension("jiraCommitConfig");
        commitConfig.addExtension("customCommitConfig");

        // change view configuration
        CompositeType changeViewerConfig = typeRegistry.register("changeViewerConfig", ChangeViewerConfiguration.class);
        typeRegistry.register("fisheyeChangeViewerConfig", FisheyeConfiguration.class);
        typeRegistry.register("customChangeViewerConfig", CustomChangeViewerConfiguration.class);

        changeViewerConfig.addExtension("fisheyeChangeViewerConfig");
        changeViewerConfig.addExtension("customChangeViewerConfig");
        
        CompositeType artifactConfig = typeRegistry.register("artifactConfig", ArtifactConfiguration.class);
        typeRegistry.register("fileArtifactConfig", FileArtifactConfiguration.class);
        typeRegistry.register("directoryArtifactConfig", DirectoryArtifactConfiguration.class);

        artifactConfig.addExtension("fileArtifactConfig");
        artifactConfig.addExtension("directoryArtifactConfig");


        // generated dynamically as new components are registered.
        CompositeType projectConfig = new CompositeType(Project.class, "projectConfig");
        projectConfig.addProperty(new TypeProperty("scm", typeRegistry.getType("scmConfig")));
        projectConfig.addProperty(new TypeProperty("general", typeRegistry.getType("generalConfig")));
        projectConfig.addProperty(new TypeProperty("cleanup", typeRegistry.getType("cleanupRuleConfig")));
        projectConfig.addProperty(new TypeProperty("changeViewer", typeRegistry.getType("changeViewerConfig")));
        
        ListType artifacts = new ListType();
        artifacts.setTypeRegistry(typeRegistry);
        artifacts.setCollectionType(typeRegistry.getType("artifactConfig"));
        projectConfig.addProperty(new TypeProperty("artifact", artifacts));

        MapType commitTransformers = new MapType();
        commitTransformers.setTypeRegistry(typeRegistry);
        commitTransformers.setCollectionType(typeRegistry.getType("commitConfig"));
        projectConfig.addProperty(new TypeProperty("commit", commitTransformers));

        // define the root level scope.
        MapType projectCollection = new MapType(HashMap.class);
        projectCollection.setTypeRegistry(typeRegistry);
        projectCollection.setCollectionType(projectConfig);

        typeRegistry.register("projectConfig", projectConfig);

        configurationPersistenceManager.register("project", projectCollection);
        Record project = new Record();
        project.setSymbolicName("projectConfig");
        recordManager.store("project", project);

        scopes.put("project", projectCollection);
    }

    public String getSymbolicName(String path)
    {
        Type type = getType(path);
        if (type != null && type instanceof CompositeType)
        {
            return type.getSymbolicName();
        }
        return null;
    }

    public Type getType(String path)
    {
        List<String> pathElements = new LinkedList<String>();
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        while (tokens.hasMoreTokens())
        {
            pathElements.add(tokens.nextToken());
        }

        if (pathElements.size() == 0)
        {
            return null;
        }
        
        String scope = pathElements.get(0);
        Traversable traversableType = scopes.get(scope);

        return traversableType.getType(pathElements.subList(1, pathElements.size()));
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
