package com.zutubi.prototype.config;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.ProjectMapType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.prototype.config.*;
import com.zutubi.pulse.prototype.config.admin.EmailConfiguration;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.prototype.config.admin.JabberConfiguration;
import com.zutubi.pulse.prototype.config.admin.LDAPConfiguration;
import com.zutubi.pulse.prototype.config.admin.LicenseKeyConfiguration;
import com.zutubi.pulse.prototype.config.admin.LoggingConfiguration;

import java.util.HashMap;

/**
 *
 *
 */
public class ConfigurationRegistry
{
    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ProjectManager projectManager;

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

        CompositeType typeConfig = typeRegistry.register("typeConfig", ProjectTypeConfiguration.class);
        typeRegistry.register("antConfig", AntTypeConfiguration.class);
        typeRegistry.register("mavenConfig", MavenTypeConfiguration.class);

        typeConfig.addExtension("antConfig");
        typeConfig.addExtension("mavenConfig");

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
        CompositeType projectConfig = typeRegistry.register("projectConfig", ProjectConfiguration.class);
        projectConfig.addProperty(new TypeProperty("scm", typeRegistry.getType("scmConfig")));
        projectConfig.addProperty(new TypeProperty("type", typeRegistry.getType("typeConfig")));
        projectConfig.addProperty(new TypeProperty("cleanup", typeRegistry.getType("cleanupRuleConfig")));
        projectConfig.addProperty(new TypeProperty("changeViewer", typeRegistry.getType("changeViewerConfig")));

        // Triggers
        CompositeType triggerConfig = typeRegistry.register("triggerConfig", TriggerConfiguration.class);
        typeRegistry.register("buildCompletedConfig", BuildCompletedTriggerConfiguration.class);
        triggerConfig.addExtension("buildCompletedConfig");
        MapType triggers = new MapType();
        triggers.setTypeRegistry(typeRegistry);
        triggers.setCollectionType(typeRegistry.getType("triggerConfig"));
        projectConfig.addProperty(new TypeProperty("trigger", triggers));
        
        ListType artifacts = new ListType();
        artifacts.setTypeRegistry(typeRegistry);
        artifacts.setCollectionType(typeRegistry.getType("artifactConfig"));
        projectConfig.addProperty(new TypeProperty("artifact", artifacts));

        MapType commitTransformers = new MapType();
        commitTransformers.setTypeRegistry(typeRegistry);
        commitTransformers.setCollectionType(typeRegistry.getType("commitConfig"));
        projectConfig.addProperty(new TypeProperty("commit", commitTransformers));

        // define the root level scope.
        ProjectMapType projectCollection = new ProjectMapType(HashMap.class, projectManager);
        projectCollection.setTypeRegistry(typeRegistry);
        projectCollection.setCollectionType(projectConfig);

        configurationPersistenceManager.register("project", projectCollection);

        // setup the global configuration options.
        typeRegistry.register("generalAdminConfig", GeneralAdminConfiguration.class);
        typeRegistry.register("loggingConfig", LoggingConfiguration.class);
        typeRegistry.register("emailConfig", EmailConfiguration.class);
        typeRegistry.register("ldapConfig", LDAPConfiguration.class);
        typeRegistry.register("jabberConfig", JabberConfiguration.class);
        typeRegistry.register("licenseConfig", LicenseKeyConfiguration.class);

        CompositeType globalConfig = typeRegistry.register("globalConfig", GlobalConfiguration.class);
        globalConfig.addProperty(new TypeProperty("general", typeRegistry.getType("generalAdminConfig")));
        globalConfig.addProperty(new TypeProperty("logging", typeRegistry.getType("loggingConfig")));
        globalConfig.addProperty(new TypeProperty("email", typeRegistry.getType("emailConfig")));
        globalConfig.addProperty(new TypeProperty("ldap", typeRegistry.getType("ldapConfig")));
        globalConfig.addProperty(new TypeProperty("jabber", typeRegistry.getType("jabberConfig")));
        globalConfig.addProperty(new TypeProperty("license", typeRegistry.getType("licenseConfig")));

        configurationPersistenceManager.register("global", globalConfig);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
