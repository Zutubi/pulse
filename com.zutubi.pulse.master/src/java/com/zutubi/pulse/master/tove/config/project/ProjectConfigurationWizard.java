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
import com.zutubi.tove.type.record.PathUtils;
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

    public static final String DEPENDENCY_TRIGGER = "dependency trigger";
    public static final String SCM_TRIGGER = "scm trigger";
    public static final String DEFAULT_STAGE = "default";
    public static final String DEFAULT_RECIPE = "default";
    public static final String DEFAULT_COMMAND = "build";

    private static final String PROPERTY_COMMANDS = "commands";
    private static final String PROPERTY_DEFAULT_RECIPE = "defaultRecipe";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_PRIMARY_TYPE = "primaryType";
    private static final String PROPERTY_PROJECT_REQUIREMENTS = "requirements";
    private static final String PROPERTY_RECIPES = "recipes";
    private static final String PROPERTY_SCM = "scm";
    private static final String PROPERTY_STAGES = "stages";
    private static final String PROPERTY_TRIGGERS = "triggers";
    private static final String PROPERTY_TYPE = "type";

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
        states = addWizardStates(states, null, scmType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get(PROPERTY_SCM)));

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
                addWizardStates(states, null, typeType, (TemplateRecord) templateParentRecord.get(PROPERTY_TYPE));
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
        record.put(PROPERTY_SCM, getCompletedStateForType(scmType).getDataRecord());

        ProjectConfiguration templateParentProject = configurationProvider.get(templateParentPath, ProjectConfiguration.class);
        if (templateParentProject.getStages().size() == 0 &&
            configurationTemplateManager.findAncestorPath(PathUtils.getPath(templateParentPath, PROPERTY_STAGES, DEFAULT_STAGE)) == null)
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

        ensureTrigger(record, templateParentProject, ScmBuildTriggerConfiguration.class, SCM_TRIGGER);
        ensureTrigger(record, templateParentProject, DependentBuildTriggerConfiguration.class, DEPENDENCY_TRIGGER);

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
            String primaryType = (String) typeSelectionState.getDataRecord().get(PROPERTY_PRIMARY_TYPE);
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
            record.put(PROPERTY_TYPE, typeRecord);
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
        typeRecord.put(PROPERTY_DEFAULT_RECIPE, DEFAULT_RECIPE);

        MutableRecord recipeRecord = typeRegistry.getType(RecipeConfiguration.class).createNewRecord(true);
        recipeRecord.put(PROPERTY_NAME, DEFAULT_RECIPE);
        MutableRecord commandsRecord = (MutableRecord) recipeRecord.get(PROPERTY_COMMANDS);
        Record commandRenderRecord = commandState.getRenderRecord();
        commandsRecord.put((String) commandRenderRecord.get(PROPERTY_NAME), commandDataRecord);

        MutableRecord recipesRecord = (MutableRecord) typeRecord.get(PROPERTY_RECIPES);
        recipesRecord.put(DEFAULT_RECIPE, recipeRecord);
        return typeRecord;
    }

    private MutableRecord createMultiRecipeType()
    {
        return typeRegistry.getType(MultiRecipeTypeConfiguration.class).createNewRecord(true);
    }

    private <T extends TriggerConfiguration> void ensureTrigger(MutableRecord record, ProjectConfiguration project, Class<T> triggerType, String name)
    {
        if (!hasTrigger(project, triggerType, name) && configurationTemplateManager.findAncestorPath(PathUtils.getPath(templateParentPath, PROPERTY_TRIGGERS, name)) == null)
        {
            CollectionType triggersType = (CollectionType) projectType.getProperty(PROPERTY_TRIGGERS).getType();
            if (!record.containsKey(PROPERTY_TRIGGERS))
            {
                MutableRecord triggersRecord = triggersType.createNewRecord(true);
                record.put(PROPERTY_TRIGGERS, triggersRecord);
            }
            MutableRecord triggersRecord = (MutableRecord) record.get(PROPERTY_TRIGGERS);

            CompositeType triggerCompositeType = typeRegistry.getType(triggerType);

            MutableRecord triggerRecord = triggerCompositeType.createNewRecord(true);
            triggerRecord.put(PROPERTY_NAME, name);
            triggersRecord.put(name, triggerRecord);
        }
    }

    private <T extends TriggerConfiguration> boolean hasTrigger(ProjectConfiguration project, Class<T> triggerType, String triggerName)
    {
        @SuppressWarnings("unchecked")
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) project.getExtensions().get(PROPERTY_TRIGGERS);
        for (TriggerConfiguration trigger : triggers.values())
        {
            if (trigger.getClass() == triggerType || triggerName.equals(trigger.getName()))
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
