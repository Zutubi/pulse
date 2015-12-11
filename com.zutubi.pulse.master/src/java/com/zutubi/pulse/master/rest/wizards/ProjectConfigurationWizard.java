package com.zutubi.pulse.master.rest.wizards;

import com.google.common.base.Function;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.plugins.CommandExtensionManager;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.WizardModel;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Custom wizard for creating projects.  Adds the essentials (SCM, type) and optionally defaults
 * like triggers.
 */
public class ProjectConfigurationWizard implements ConfigurationWizard
{
    private WizardModelBuilder wizardModelBuilder;
    private TypeRegistry typeRegistry;

    @Override
    public WizardModel buildModel(CompositeType type, FormContext context) throws TypeException
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForType("", type, context));
        model.appendStep(wizardModelBuilder.buildStepForType("scm", typeRegistry.getType(ScmConfiguration.class), context));
        model.appendStep(wizardModelBuilder.buildStepForType("type", typeRegistry.getType(TypeConfiguration.class), context));
        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, String parentPath, String baseName, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models) throws TypeException
    {
        MutableRecord projectRecord = wizardModelBuilder.buildAndValidateRecord(type, parentPath, templateOwnerPath, concrete, models, "");
        projectRecord.put("scm", wizardModelBuilder.buildAndValidateRecord(typeRegistry.getType(ScmConfiguration.class), parentPath, templateOwnerPath, concrete, models, "scm"));
        projectRecord.put("type", wizardModelBuilder.buildAndValidateRecord(typeRegistry.getType(TypeConfiguration.class), parentPath, templateOwnerPath, concrete, models, "type"));
        return projectRecord;
    }


    // FIXME kendo default project init

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

    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationReferenceManager configurationReferenceManager;

    private CommandExtensionManager commandExtensionManager;
    private String templateParentPath = null;
    private CompositeType projectType;

    public void confit()
    {
        MutableRecord record = null;
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
    }

    private void addDefaultResourceRequirements(MutableRecord projectRecord, CompositeType commandType)
    {
        Object existingRequirements = projectRecord.get(PROPERTY_PROJECT_REQUIREMENTS);
        if (existingRequirements == null || ((Record) existingRequirements).size() == 0)
        {
            @SuppressWarnings("unchecked")
            List<ResourceRequirement> defaultRequirements = commandExtensionManager.getDefaultResourceRequirements((Class<? extends CommandConfiguration>) commandType.getClazz());
            List<ResourceRequirementConfiguration> configurations = newArrayList(transform(defaultRequirements, new Function<ResourceRequirement, ResourceRequirementConfiguration>()
            {
                public ResourceRequirementConfiguration apply(ResourceRequirement resourceRequirement)
                {
                    return new ResourceRequirementConfiguration(resourceRequirement);
                }
            }));

            try
            {
                projectRecord.put(PROPERTY_PROJECT_REQUIREMENTS, projectType.getProperty(PROPERTY_PROJECT_REQUIREMENTS).getType().unstantiate(configurations, null));
            }
            catch (TypeException e)
            {
                // We can continue without these defaults.
                //LOG.severe(e);
            }
        }
    }

    private MutableRecord createSingleCommandType(ProjectConfiguration templateParentProject)
    {
        MutableRecord commandDataRecord = null;//commandState.getDataRecord();
        String recipeName = DEFAULT_RECIPE;

        if (templateParentProject.getType() == null)
        {
//            SimpleInstantiator instantiator = new SimpleInstantiator(templateParentPath, configurationReferenceManager, configurationTemplateManager);
//            try
//            {
//                CommandConfiguration commandConfig = (CommandConfiguration) instantiator.instantiate(commandState.getType(), commandDataRecord);
//                commandConfig.initialiseSingleCommandProject(templateParentProject.getPostProcessors());
//                commandDataRecord = commandState.getType().unstantiate(commandConfig, null);
//            }
//            catch (TypeException e)
//            {
//                // This is not fatal, we just won't get any extra initialisation.
//                //LOG.severe(e);
//            }
        }
        else
        {
            MultiRecipeTypeConfiguration parentType = (MultiRecipeTypeConfiguration) templateParentProject.getType();
            recipeName = parentType.getRecipes().keySet().iterator().next();
        }

        MutableRecord typeRecord = createMultiRecipeType();
        typeRecord.put(PROPERTY_DEFAULT_RECIPE, recipeName);

        MutableRecord recipeRecord = typeRegistry.getType(RecipeConfiguration.class).createNewRecord(true);
        recipeRecord.put(PROPERTY_NAME, recipeName);
        MutableRecord commandsRecord = (MutableRecord) recipeRecord.get(PROPERTY_COMMANDS);
//        Record commandRenderRecord = commandState.getRenderRecord();
//        commandsRecord.put((String) commandRenderRecord.get(PROPERTY_NAME), commandDataRecord);

        MutableRecord recipesRecord = (MutableRecord) typeRecord.get(PROPERTY_RECIPES);
        recipesRecord.put(recipeName, recipeRecord);
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

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
