package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiStepTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.pulse.master.tove.wizard.*;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;

import java.util.List;
import java.util.Map;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 */
public class ProjectConfigurationWizard extends AbstractTypeWizard
{
    private static final Logger LOG = Logger.getLogger(ProjectConfigurationWizard.class);

    public static final String DEFAULT_STAGE = "default";

    private CompositeType projectType;
    private CompositeType scmType;
    private CompositeType typeType;
    private CompositeType typeSelectType;
    private CompositeType commandType;

    private ConfigurationProvider configurationProvider;

    public class ProjectTypeSelectState extends SingleStepWizardState
    {
        public ProjectTypeSelectState(AbstractTypeWizard wizard, String parentPath, CompositeType baseType, CompositeType type, TemplateRecord templateRecord)
        {
            super(wizard, wizard.getNextUniqueId(), parentPath, baseType, type, templateRecord);
        }

        @Override
        public TypeWizardState getNextState()
        {
            MutableRecord record = getDataRecord();
            try
            {
                SimpleInstantiator instantiator = new SimpleInstantiator(null, configurationTemplateManager);
                ProjectTypeSelectionConfiguration config = (ProjectTypeSelectionConfiguration) instantiator.instantiate(typeSelectType, record);
                String primaryType = config.getPrimaryType();
                if (primaryType.equals(ProjectTypeSelectionConfiguration.TYPE_SINGLE_STEP))
                {
                    String commandSymbolicName = config.getCommandType();
                    if (TextUtils.stringSet(commandSymbolicName))
                    {
                        CompositeType selectedCommandType = typeRegistry.getType(commandSymbolicName);
                        return new SingleStepWizardState(ProjectConfigurationWizard.this, wizard.getNextUniqueId(), null, commandType, selectedCommandType, null);
                    }
                    else
                    {
                        return new UnknownTypeState(wizard.getNextUniqueId(), typeType);
                    }
                }
                else
                {
                    CompositeType selectedTypeType = typeRegistry.getType(ProjectTypeSelectionConfiguration.TYPE_MAPPING.get(primaryType));
                    return new SingleStepWizardState(ProjectConfigurationWizard.this, wizard.getNextUniqueId(), null, typeType, selectedTypeType, null);
                }
            }
            catch (TypeException e)
            {
                LOG.severe(e);
                return null;
            }
        }
    }

    public void initialise()
    {
        projectType = typeRegistry.getType(ProjectConfiguration.class);
        scmType = typeRegistry.getType(ScmConfiguration.class);
        typeType = typeRegistry.getType(TypeConfiguration.class);
        typeSelectType = typeRegistry.getType(ProjectTypeSelectionConfiguration.class);
        commandType = typeRegistry.getType(CommandConfiguration.class);

        List<AbstractChainableState> states = addWizardStates(null, parentPath, projectType, templateParentRecord);
        states = addWizardStates(states, null, scmType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get("scm")));

        ProjectConfiguration templateParent = templateParentPath == null ? null : configurationProvider.get(templateParentPath, ProjectConfiguration.class);
        if (templateParent != null && templateParent.getType() != null)
        {
            TypeConfiguration configuredType = templateParent.getType();
            TemplateRecord commandTemplate = getSingleCommandTemplate(configuredType);
            if (commandTemplate == null)
            {
                addWizardStates(states, null, typeType, (TemplateRecord) templateParentRecord.get("type"));
            }
            else
            {
                addWizardStates(states, null, commandType, commandTemplate);
            }
        }
        else
        {
            ProjectTypeSelectState typeSelectState = new ProjectTypeSelectState(this, MasterConfigurationRegistry.TRANSIENT_SCOPE, typeSelectType, typeSelectType, null);
            SpringComponentContext.autowire(typeSelectState);
            addWizardState(states, typeSelectState);
        }

    }

    private TemplateRecord getSingleCommandTemplate(TypeConfiguration configuredType)
    {
        if (configuredType instanceof MultiStepTypeConfiguration)
        {
            MultiStepTypeConfiguration multiStep = (MultiStepTypeConfiguration) configuredType;
            if (multiStep.getCommands().size() == 1)
            {
                CommandConfiguration command = multiStep.getCommands().values().iterator().next();
                return (TemplateRecord) configurationTemplateManager.getRecord(command.getConfigurationPath());
            }
        }

        return null;
    }

    public void doFinish()
    {
        super.doFinish();

        MutableRecord record = projectType.createNewRecord(false);
        record.update(getCompletedStateForType(projectType).getDataRecord());
        record.put("scm", getCompletedStateForType(scmType).getDataRecord());

        TypeWizardState typeState = getCompletedStateForType(typeType);
        MutableRecord typeRecord;
        if (typeState == null)
        {
            TypeWizardState commandState = getCompletedStateForType(commandType);
            typeRecord = typeRegistry.getType(MultiStepTypeConfiguration.class).createNewRecord(true);
            MutableRecord commandsRecord = (MutableRecord) typeRecord.get("commands");
            Record commandRenderRecord = commandState.getRenderRecord();
            MutableRecord commandRecord = commandState.getDataRecord();
            commandsRecord.put((String) commandRenderRecord.get("name"), commandRecord);
        }
        else
        {
            typeRecord = typeState.getDataRecord();
        }
        
        record.put("type", typeRecord);

        ProjectConfiguration templateParentProject = configurationProvider.get(templateParentPath, ProjectConfiguration.class);
        if(templateParentProject.getStages().size() == 0)
        {
            // Add a default stage to our new project.
            CollectionType stagesType = (CollectionType) projectType.getProperty("stages").getType();
            CompositeType stageType = (CompositeType) stagesType.getTargetType();
            MutableRecord stagesRecord = stagesType.createNewRecord(true);
            MutableRecord stageRecord = stageType.createNewRecord(true);
            stageRecord.put("name", DEFAULT_STAGE);
            stagesRecord.put(DEFAULT_STAGE, stageRecord);
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
        
        successPath = configurationTemplateManager.insertRecord(MasterConfigurationRegistry.PROJECTS_SCOPE, record);
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
