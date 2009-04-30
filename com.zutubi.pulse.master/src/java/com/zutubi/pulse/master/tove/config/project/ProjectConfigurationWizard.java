package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.master.tove.wizard.webwork.AbstractTypeWizard;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.TemplateRecord;

import java.util.List;
import java.util.Map;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 */
public class ProjectConfigurationWizard extends AbstractTypeWizard
{
    public static final String DEFAULT_STAGE = "default";
    public static final String SCM_TRIGGER = "scm trigger";

    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_SCM = "scm";
    private static final String PROPERTY_STAGES = "stages";
    private static final String PROPERTY_TRIGGERS = "triggers";
    private static final String PROPERTY_TYPE = "type";

    private CompositeType projectType;
    private CompositeType scmType;
    private CompositeType typeType;

    private ConfigurationProvider configurationProvider;

    public void initialise()
    {
        projectType = typeRegistry.getType(ProjectConfiguration.class);

        scmType = (CompositeType) projectType.getProperty(PROPERTY_SCM).getType();
        typeType = (CompositeType) projectType.getProperty(PROPERTY_TYPE).getType();

        List<AbstractChainableState> states = addWizardStates(null, parentPath, projectType, templateParentRecord);
        states = addWizardStates(states, null, scmType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get(PROPERTY_SCM)));
        addWizardStates(states, null, typeType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get(PROPERTY_TYPE)));
    }

    public void doFinish()
    {
        super.doFinish();

        MutableRecord record = projectType.createNewRecord(false);
        record.update(getCompletedStateForType(projectType).getDataRecord());
        record.put(PROPERTY_SCM, getCompletedStateForType(scmType).getDataRecord());
        record.put(PROPERTY_TYPE, getCompletedStateForType(typeType).getDataRecord());

        ProjectConfiguration templateParentProject = configurationProvider.get(templateParentPath, ProjectConfiguration.class);
        if(templateParentProject.getStages().size() == 0)
        {
            if (configurationTemplateManager.findAncestorPath(PathUtils.getPath(templateParentPath, PROPERTY_STAGES, DEFAULT_STAGE)) == null)
            {
                // Add a default stage to our new project.
                CollectionType stagesType = (CollectionType) projectType.getProperty(PROPERTY_STAGES).getType();
                CompositeType stageType = (CompositeType) stagesType.getTargetType();
                MutableRecord stagesRecord = stagesType.createNewRecord(true);
                MutableRecord stageRecord = stageType.createNewRecord(true);
                stageRecord.put(PROPERTY_NAME, DEFAULT_STAGE);
                stagesRecord.put(DEFAULT_STAGE, stageRecord);
                record.put(PROPERTY_STAGES, stagesRecord);
            }
        }

        if(!hasScmTrigger(templateParentProject))
        {
            if (configurationTemplateManager.findAncestorPath(PathUtils.getPath(templateParentPath, PROPERTY_TRIGGERS, SCM_TRIGGER)) == null)
            {
                // Add a default SCM trigger
                CollectionType triggersType = (CollectionType) projectType.getProperty(PROPERTY_TRIGGERS).getType();
                CompositeType scmTriggerType = typeRegistry.getType(ScmBuildTriggerConfiguration.class);
                MutableRecord triggersRecord = triggersType.createNewRecord(true);
                MutableRecord triggerRecord = scmTriggerType.createNewRecord(true);
                triggerRecord.put(PROPERTY_NAME, SCM_TRIGGER);
                triggersRecord.put(SCM_TRIGGER, triggerRecord);
                record.put(PROPERTY_TRIGGERS, triggersRecord);
            }
        }

        configurationTemplateManager.setParentTemplate(record, templateParentRecord.getHandle());
        if(template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }
        
        successPath = configurationTemplateManager.insertRecord(ConfigurationRegistry.PROJECTS_SCOPE, record);
    }

    private boolean hasScmTrigger(ProjectConfiguration templateParentProject)
    {
        @SuppressWarnings({"unchecked"})
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) templateParentProject.getExtensions().get(PROPERTY_TRIGGERS);
        for(TriggerConfiguration trigger: triggers.values())
        {
            if (trigger instanceof ScmBuildTriggerConfiguration || SCM_TRIGGER.equals(trigger.getName()))
            {
                return true;
            }
        }

        return false;
    }

    public Type getType()
    {
        return projectType;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
