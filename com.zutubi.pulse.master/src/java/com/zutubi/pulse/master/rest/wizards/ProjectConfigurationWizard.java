package com.zutubi.pulse.master.rest.wizards;

import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.rest.model.TypedWizardStepModel;
import com.zutubi.pulse.master.rest.model.WizardModel;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;

/**
 * Custom wizard for creating projects.  Adds the essentials (SCM, type) and optionally defaults
 * like triggers.
 */
public class ProjectConfigurationWizard implements ConfigurationWizard
{
    public static final String DEPENDENCY_TRIGGER = "dependency trigger";
    public static final String SCM_TRIGGER = "scm trigger";
    public static final String DEFAULT_STAGE = "default";
    public static final String DEFAULT_RECIPE = "default";
    public static final String DEFAULT_COMMAND = "build";

    private static final String PROPERTY_DEFAULT_RECIPE = "defaultRecipe";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_RECIPES = "recipes";
    private static final String PROPERTY_STAGES = "stages";
    private static final String PROPERTY_TRIGGERS = "triggers";

    private static final String TYPE_GIT = "zutubi.gitConfig";

    private static final String KEY_SCM = "scm";
    private static final String KEY_TYPE = "type";
    private static final String KEY_DEFAULTS = "defaults";

    private WizardModelBuilder wizardModelBuilder;
    private TypeRegistry typeRegistry;
    private ConfigurationTemplateManager configurationTemplateManager;

    @Override
    public WizardModel buildModel(CompositeType type, FormContext context) throws TypeException
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForType("", type, context));
        TypedWizardStepModel scmStep = wizardModelBuilder.buildStepForClass(KEY_SCM, ScmConfiguration.class, context);
        // Git is a plugin, so not always available, but a nicer default than whatever is alphabetically first!
        if (scmStep.hasType(TYPE_GIT))
        {
            scmStep.setDefaultType(TYPE_GIT);
        }

        model.appendStep(scmStep);
        TypedWizardStepModel projectTypeStep = wizardModelBuilder.buildStepForClass(KEY_TYPE, TypeConfiguration.class, context);
        projectTypeStep.setDefaultType(typeRegistry.getType(MultiRecipeTypeConfiguration.class).getSymbolicName());
        model.appendStep(projectTypeStep);
        model.appendStep(wizardModelBuilder.buildStepForClass(KEY_DEFAULTS, ProjectDefaultsConfiguration.class, context));
        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, WizardContext wizardContext) throws TypeException
    {
        MutableRecord projectRecord = wizardModelBuilder.buildAndValidateRecord(type, "", wizardContext);
        CompositeType scmType = wizardModelBuilder.getCompositeType(ScmConfiguration.class);
        projectRecord.put(KEY_SCM, wizardModelBuilder.buildAndValidateRecord(scmType, KEY_SCM, wizardContext));
        CompositeType typeType = wizardModelBuilder.getCompositeType(TypeConfiguration.class);
        MutableRecord typeRecord = wizardModelBuilder.buildAndValidateRecord(typeType, KEY_TYPE, wizardContext);
        projectRecord.put(KEY_TYPE, typeRecord);

        if (wizardContext.getModels().containsKey(KEY_DEFAULTS))
        {
            CompositeType defaultsType = typeRegistry.getType(ProjectDefaultsConfiguration.class);
            MutableRecord defaultsRecord = wizardModelBuilder.buildRecord(wizardContext.getTemplateParentRecord(), wizardContext.getTemplateOwnerPath(), defaultsType, wizardContext.getModels().get(KEY_DEFAULTS));
            ProjectDefaultsConfiguration defaults = (ProjectDefaultsConfiguration) wizardModelBuilder.buildAndValidateCreatorInstance(defaultsType, wizardContext.getParentPath(), wizardContext.getBaseName(), defaultsRecord);
            applyDefaults(type, projectRecord, typeRecord, defaults, wizardContext);
        }

        return projectRecord;
    }

    private void applyDefaults(CompositeType projectType, MutableRecord projectRecord, MutableRecord typeRecord, ProjectDefaultsConfiguration defaults, WizardContext context)
    {
        if (defaults.isAddScmTrigger())
        {
            addTrigger(projectType, projectRecord, ScmBuildTriggerConfiguration.class, SCM_TRIGGER, context);
        }

        if (defaults.isAddDependenciesTrigger())
        {
            addTrigger(projectType, projectRecord, DependentBuildTriggerConfiguration.class, DEPENDENCY_TRIGGER, context);
        }

        CompositeType multiRecipeType = typeRegistry.getType(MultiRecipeTypeConfiguration.class);
        if (multiRecipeType.getSymbolicName().equals(typeRecord.getSymbolicName()) && defaults.isAddDefaultRecipe())
        {
            MutableRecord recipesRecord = (MutableRecord) typeRecord.get(PROPERTY_RECIPES);
            if (recipesRecord == null)
            {
                recipesRecord = ((CollectionType) multiRecipeType.getPropertyType(PROPERTY_RECIPES)).createNewRecord(true);
                typeRecord.put(PROPERTY_RECIPES, recipesRecord);
            }

            if (recipesRecord.size() == 0)
            {
                String recipeName = defaults.getRecipeName();
                if (!StringUtils.stringSet(recipeName))
                {
                    recipeName = DEFAULT_RECIPE;
                }

                typeRecord.put(PROPERTY_DEFAULT_RECIPE, recipeName);

                MutableRecord recipeRecord = typeRegistry.getType(RecipeConfiguration.class).createNewRecord(true);
                recipeRecord.put(PROPERTY_NAME, recipeName);
                recipesRecord.put(recipeName, recipeRecord);
            }
        }

        if (defaults.isAddDefaultStage())
        {
            String stageName = defaults.getStageName();
            if (!StringUtils.stringSet(stageName))
            {
                stageName = DEFAULT_STAGE;
            }

            MutableRecord stagesRecord = (MutableRecord) projectRecord.get(PROPERTY_STAGES);
            if (stagesRecord == null)
            {
                stagesRecord = ((CollectionType) projectType.getPropertyType(PROPERTY_STAGES)).createNewRecord(true);
                projectRecord.put(PROPERTY_STAGES, stagesRecord);
            }

            if (stagesRecord.size() == 0)
            {
                MutableRecord stageRecord = typeRegistry.getType(BuildStageConfiguration.class).createNewRecord(true);
                stageRecord.put(PROPERTY_NAME, stageName);
                stagesRecord.put(stageName, stageRecord);
            }
        }
    }

    private <T extends TriggerConfiguration> void addTrigger(CompositeType projectType, MutableRecord record, Class<T> triggerType, String name, WizardContext context)
    {
        String templateParentPath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, (String) context.getTemplateParentRecord().get(PROPERTY_NAME));
        if (configurationTemplateManager.findAncestorPath(PathUtils.getPath(templateParentPath, PROPERTY_TRIGGERS, name)) == null)
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


    // FIXME kendo adding default resource reqs for command


    private static final String PROPERTY_PROJECT_REQUIREMENTS = "requirements";
    private void addDefaultResourceRequirements(MutableRecord projectRecord, CompositeType commandType)
    {
//        Object existingRequirements = projectRecord.get(PROPERTY_PROJECT_REQUIREMENTS);
//        if (existingRequirements == null || ((Record) existingRequirements).size() == 0)
//        {
//            @SuppressWarnings("unchecked")
//            List<ResourceRequirement> defaultRequirements = commandExtensionManager.getDefaultResourceRequirements((Class<? extends CommandConfiguration>) commandType.getClazz());
//            List<ResourceRequirementConfiguration> configurations = newArrayList(transform(defaultRequirements, new Function<ResourceRequirement, ResourceRequirementConfiguration>()
//            {
//                public ResourceRequirementConfiguration apply(ResourceRequirement resourceRequirement)
//                {
//                    return new ResourceRequirementConfiguration(resourceRequirement);
//                }
//            }));
//
//            try
//            {
//                projectRecord.put(PROPERTY_PROJECT_REQUIREMENTS, projectType.getProperty(PROPERTY_PROJECT_REQUIREMENTS).getType().unstantiate(configurations, null));
//            }
//            catch (TypeException e)
//            {
//                // We can continue without these defaults.
//                //LOG.severe(e);
//            }
//        }
    }

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
