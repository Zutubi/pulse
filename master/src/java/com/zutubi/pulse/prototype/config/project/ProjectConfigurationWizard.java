package com.zutubi.pulse.prototype.config.project;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;
import com.zutubi.util.logging.Logger;
import com.zutubi.pulse.prototype.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.ScmBuildTriggerConfiguration;

import java.util.List;
import java.util.Map;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 */
public class ProjectConfigurationWizard extends AbstractTypeWizard
{
    private CompositeType projectType;
    private CompositeType scmType;
    private CompositeType typeType;

    private ConfigurationProvider configurationProvider;

    public void initialise()
    {
        projectType = typeRegistry.getType(ProjectConfiguration.class);

        scmType = (CompositeType) projectType.getProperty("scm").getType();
        typeType = (CompositeType) projectType.getProperty("type").getType();

        List<AbstractChainableState> states = addWizardStates(null, parentPath, projectType, templateParentRecord);
        states = addWizardStates(states, null, scmType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get("scm")));
        addWizardStates(states, null, typeType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get("type")));
    }

    public void doFinish()
    {
        super.doFinish();

        MutableRecord record = projectType.createNewRecord(false);
        record.update(getCompletedStateForType(projectType).getDataRecord());
        record.put("scm", getCompletedStateForType(scmType).getDataRecord());
        record.put("type", getCompletedStateForType(typeType).getDataRecord());

        ProjectConfiguration templateParentProject = configurationProvider.get(templateParentPath, ProjectConfiguration.class);
        if(templateParentProject.getStages().size() == 0)
        {
            // Add a default stage to our new project.
            CollectionType stagesType = (CollectionType) projectType.getProperty("stages").getType();
            CompositeType stageType = (CompositeType) stagesType.getTargetType();
            MutableRecord stagesRecord = stagesType.createNewRecord(true);
            MutableRecord stageRecord = stageType.createNewRecord(true);
            stageRecord.put("name", "default");
            stagesRecord.put("default", stageRecord);
            record.put("stages", stagesRecord);
        }

        if(!hasScmTrigger(templateParentProject))
        {
            // Add a default SCM trigger
            CollectionType triggersType = (CollectionType) projectType.getProperty("triggers").getType();
            CompositeType scmTriggerType = typeRegistry.getType(ScmBuildTriggerConfiguration.class);
            MutableRecord triggersRecord = triggersType.createNewRecord(true);
            MutableRecord triggerRecord = scmTriggerType.createNewRecord(true);
            triggerRecord.put("name", "scm trigger");
            triggersRecord.put("scm trigger", triggerRecord);
            record.put("triggers", triggersRecord);
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
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) templateParentProject.getExtensions().get("triggers");
        for(TriggerConfiguration trigger: triggers.values())
        {
            if(trigger instanceof ScmBuildTriggerConfiguration)
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
