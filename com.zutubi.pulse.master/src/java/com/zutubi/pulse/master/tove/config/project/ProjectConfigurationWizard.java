package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.plugins.CommandExtensionManager;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.pulse.master.tove.wizard.*;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
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
    public static final String DEFAULT_RECIPE = "default";
    public static final String DEFAULT_COMMAND = "build";

    private static final String PROPERTY_PROJECT_REQUIREMENTS = "requirements";

    private CompositeType projectType;
    private CompositeType scmType;
    private CompositeType typeType;
    private CompositeType typeSelectType;
    private CompositeType commandType;

    private ConfigurationProvider configurationProvider;
    private ConfigurationReferenceManager configurationReferenceManager;
    private CommandExtensionManager commandExtensionManager;

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
                SimpleInstantiator instantiator = new SimpleInstantiator(null, null, configurationTemplateManager);
                ProjectTypeSelectionConfiguration config = (ProjectTypeSelectionConfiguration) instantiator.instantiate(typeSelectType, record);
                String primaryType = config.getPrimaryType();
                if (primaryType.equals(ProjectTypeSelectionConfiguration.TYPE_SINGLE_STEP))
                {
                    String commandSymbolicName = config.getCommandType();
                    if (TextUtils.stringSet(commandSymbolicName))
                    {
                        CompositeType selectedCommandType = typeRegistry.getType(commandSymbolicName);
                        SingleStepWizardState commandState = new SingleStepWizardState(ProjectConfigurationWizard.this, wizard.getNextUniqueId(), null, commandType, selectedCommandType, null);
                        commandState.getDataRecord().put("name", DEFAULT_COMMAND);
                        return commandState;
                    }
                    else
                    {
                        return new UnknownTypeState(wizard.getNextUniqueId(), typeType);
                    }
                }
                else if (primaryType.equals(ProjectTypeSelectionConfiguration.TYPE_MULTI_STEP))
                {
                    return null;
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
            if (configuredType instanceof MultiRecipeTypeConfiguration)
            {
                TemplateRecord commandTemplate = getSingleCommandTemplate(configuredType);
                if (commandTemplate != null)
                {
                    addWizardStates(states, null, commandType, commandTemplate);
                }
            }
            else
            {
                addWizardStates(states, null, typeType, (TemplateRecord) templateParentRecord.get("type"));
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
        if (configuredType instanceof MultiRecipeTypeConfiguration)
        {
            MultiRecipeTypeConfiguration multiRecipe = (MultiRecipeTypeConfiguration) configuredType;
            if (multiRecipe.getRecipes().size() == 1)
            {
                RecipeConfiguration recipe = multiRecipe.getRecipes().values().iterator().next();
                if (recipe.getCommands().size() == 1)
                {
                    CommandConfiguration command = recipe.getCommands().values().iterator().next();
                    return (TemplateRecord) configurationTemplateManager.getRecord(command.getConfigurationPath());
                }
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

        ProjectConfiguration templateParentProject = configurationProvider.get(templateParentPath, ProjectConfiguration.class);
        if (templateParentProject.getStages().size() == 0)
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

        ensureTrigger(record, templateParentProject, ScmBuildTriggerConfiguration.class, "scm trigger");
        ensureTrigger(record, templateParentProject, DependentBuildTriggerConfiguration.class, "dependency trigger");

        MutableRecord typeRecord = null;

        TypeWizardState typeSelectionState = getCompletedStateForType(typeSelectType);
        if (typeSelectionState == null)
        {
            // No type selection because we are overriding an existing type.
            TypeWizardState typeState = getCompletedStateForType(typeType);
            if (typeState != null)
            {
                typeRecord = typeState.getDataRecord();
            }
            else if (getCompletedStateForType(commandType) != null)
            {
                typeRecord = createSingleCommandType(templateParentProject);
            }
        }
        else
        {
            String primaryType = (String) typeSelectionState.getDataRecord().get("primaryType");
            if (primaryType.equals(ProjectTypeSelectionConfiguration.TYPE_SINGLE_STEP))
            {
                typeRecord = createSingleCommandType(templateParentProject);
                addDefaultResourceRequirements(record, getCompletedStateForType(commandType).getType());
            }
            else if (primaryType.equals(ProjectTypeSelectionConfiguration.TYPE_MULTI_STEP))
            {
                typeRecord = createMultiRecipeType();
            }
            else
            {
                TypeWizardState typeState = getCompletedStateForType(typeType);
                typeRecord = typeState.getDataRecord();
            }
        }

        if (typeRecord != null)
        {
            record.put("type", typeRecord);
        }

        configurationTemplateManager.setParentTemplate(record, templateParentRecord.getHandle());
        if (template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }

        successPath = configurationTemplateManager.insertRecord(MasterConfigurationRegistry.PROJECTS_SCOPE, record);
    }

    private void addDefaultResourceRequirements(MutableRecord projectRecord, CompositeType commandType)
    {
        Object existingRequirements = projectRecord.get(PROPERTY_PROJECT_REQUIREMENTS);
        if (existingRequirements == null || ((Record) existingRequirements).size() == 0)
        {
            @SuppressWarnings("unchecked")
            List<ResourceRequirement> defaultRequirements = commandExtensionManager.getDefaultResourceRequirements((Class<? extends CommandConfiguration>) commandType.getClazz());
            List<ResourceRequirementConfiguration> configurations = CollectionUtils.map(defaultRequirements, new Mapping<ResourceRequirement, ResourceRequirementConfiguration>()
            {
                public ResourceRequirementConfiguration map(ResourceRequirement resourceRequirement)
                {
                    return new ResourceRequirementConfiguration(resourceRequirement);
                }
            });

            try
            {
                projectRecord.put(PROPERTY_PROJECT_REQUIREMENTS, projectType.getProperty(PROPERTY_PROJECT_REQUIREMENTS).getType().unstantiate(configurations));
            }
            catch (TypeException e)
            {
                // We can continue without these defaults.
                LOG.severe(e);
            }
        }
    }

    private MutableRecord createSingleCommandType(ProjectConfiguration templateParentProject)
    {
        TypeWizardState commandState = getCompletedStateForType(commandType);
        MutableRecord commandDataRecord = commandState.getDataRecord();
        if (templateParentProject.getType() == null)
        {
            SimpleInstantiator instantiator = new SimpleInstantiator(templateParentPath, configurationReferenceManager, configurationTemplateManager);
            try
            {
                CommandConfiguration commandConfig = (CommandConfiguration) instantiator.instantiate(commandState.getType(), commandDataRecord);
                commandConfig.initialiseSingleCommandProject(templateParentProject.getPostProcessors());
                commandDataRecord = commandState.getType().unstantiate(commandConfig);
            }
            catch (TypeException e)
            {
                // This is not fatal, we just won't get any extra initialisation.
                LOG.severe(e);
            }
        }

        MutableRecord typeRecord = createMultiRecipeType();
        typeRecord.put("defaultRecipe", DEFAULT_RECIPE);

        MutableRecord recipeRecord = typeRegistry.getType(RecipeConfiguration.class).createNewRecord(true);
        recipeRecord.put("name", DEFAULT_RECIPE);
        MutableRecord commandsRecord = (MutableRecord) recipeRecord.get("commands");
        Record commandRenderRecord = commandState.getRenderRecord();
        commandsRecord.put((String) commandRenderRecord.get("name"), commandDataRecord);

        MutableRecord recipesRecord = (MutableRecord) typeRecord.get("recipes");
        recipesRecord.put(DEFAULT_RECIPE, recipeRecord);
        return typeRecord;
    }

    private MutableRecord createMultiRecipeType()
    {
        return typeRegistry.getType(MultiRecipeTypeConfiguration.class).createNewRecord(true);
    }

    private <T extends TriggerConfiguration> void ensureTrigger(MutableRecord record, ProjectConfiguration project, Class<T> triggerType, String name)
    {
        if (!hasTrigger(project, triggerType))
        {
            CollectionType triggersType = (CollectionType) projectType.getProperty("triggers").getType();
            if (!record.containsKey("triggers"))
            {
                MutableRecord triggersRecord = triggersType.createNewRecord(true);
                record.put("triggers", triggersRecord);
            }
            MutableRecord triggersRecord = (MutableRecord) record.get("triggers");

            CompositeType triggerCompositeType = typeRegistry.getType(triggerType);

            MutableRecord triggerRecord = triggerCompositeType.createNewRecord(true);
            triggerRecord.put("name", name);
            triggersRecord.put(name, triggerRecord);
        }
    }

    private <T extends TriggerConfiguration> boolean hasTrigger(ProjectConfiguration project, Class<T> triggerType)
    {
        @SuppressWarnings("unchecked")
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) project.getExtensions().get("triggers");
        for (TriggerConfiguration trigger : triggers.values())
        {
            if (trigger.getClass() == triggerType)
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

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public void setCommandExtensionManager(CommandExtensionManager commandExtensionManager)
    {
        this.commandExtensionManager = commandExtensionManager;
    }
}
