package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.plugins.CommandExtensionManager;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.ui.ConfigModelBuilder;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.model.*;
import com.zutubi.tove.ui.wizards.ConfigurationWizard;
import com.zutubi.tove.ui.wizards.WizardContext;
import com.zutubi.tove.ui.wizards.WizardModelBuilder;
import com.zutubi.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class CommandConfigurationWizard implements ConfigurationWizard
{
    private static final String KEY_RESOURCE = "resource";

    private CommandExtensionManager commandExtensionManager;
    private TypeRegistry typeRegistry;
    private ConfigModelBuilder configModelBuilder;
    private WizardModelBuilder wizardModelBuilder;
    private ResourceManager resourceManager;
    private ConfigurationProvider configurationProvider;

    @Override
    public WizardModel buildModel(CompositeType type, FormContext context) throws TypeException
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForType("", type, context));

        // Establish a set of possible resource names not already required by this project, and
        // only add the prompt for commands that require one of these names by default.
        Set<String> resourceNames = new HashSet<>();
        for (List<ResourceConfiguration> resources: resourceManager.findAllVisible().values())
        {
            for (ResourceConfiguration resource: resources)
            {
                resourceNames.add(resource.getName());
            }
        }

        ProjectConfiguration projectConfig = configurationProvider.getAncestorOfType(context.getClosestExistingPath(), ProjectConfiguration.class);
        for (ResourceRequirementConfiguration projectRequirement: projectConfig.getRequirements())
        {
            resourceNames.remove(projectRequirement.getResource());
        }

        CompositeTypeModel requirementTypeModel = configModelBuilder.buildCompositeTypeModel(typeRegistry.getType(CommandResourceConfiguration.class), new FormContext((String) null), null);
        CustomWizardStepModel requirementStep = new CustomWizardStepModel("resource requirement", KEY_RESOURCE, requirementTypeModel.getForm());
        requirementStep.setDocs(requirementTypeModel.getDocs());

        Set<String> symbolicNames = new HashSet<>();
        for (Class<? extends CommandConfiguration> clazz: commandExtensionManager.getCommandClasses())
        {
            List<ResourceRequirement> defaultRequirements = commandExtensionManager.getDefaultResourceRequirements(clazz);
            if (defaultRequirements.size() > 0 && resourceNames.contains(defaultRequirements.get(0).getResource()))
            {
                String symbolicName = typeRegistry.getType(clazz).getSymbolicName();
                requirementStep.addParameter(symbolicName, defaultRequirements.get(0).getResource());
                symbolicNames.add(symbolicName);
            }
        }

        requirementStep.setFormDefaults(requirementTypeModel.getSimplePropertyDefaults());
        requirementStep.setFilter(new WizardTypeFilter("", symbolicNames));

        model.appendStep(requirementStep);

        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, WizardContext wizardContext) throws TypeException
    {
        // Build the command first to perform validation.
        MutableRecord commandRecord = wizardModelBuilder.buildAndValidateRecord(type, "", wizardContext);

        CompositeModel requirementModel = wizardContext.getModels().get(KEY_RESOURCE);
        if (requirementModel != null)
        {
            CompositeType requirementType = typeRegistry.getType(CommandResourceConfiguration.class);
            MutableRecord resourceRecord = wizardModelBuilder.buildRecord(wizardContext.getTemplateParentRecord(), wizardContext.getTemplateOwnerPath(), requirementType, requirementModel);
            CommandResourceConfiguration resourceConfig = (CommandResourceConfiguration) wizardModelBuilder.buildAndValidateTransientInstance(requirementType, wizardContext.getParentPath(), null, resourceRecord);
            if (resourceConfig.isAddDefaultResource() && StringUtils.stringSet(resourceConfig.getResource()))
            {
                ProjectConfiguration projectConfig = configurationProvider.getAncestorOfType(wizardContext.getParentPath(), ProjectConfiguration.class);
                projectConfig = configurationProvider.deepClone(projectConfig);
                projectConfig.getRequirements().add(resourceConfig.buildRequirement());
                configurationProvider.save(projectConfig);
            }
        }

        return commandRecord;
    }

    public void setCommandExtensionManager(CommandExtensionManager commandExtensionManager)
    {
        this.commandExtensionManager = commandExtensionManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigModelBuilder(ConfigModelBuilder configModelBuilder)
    {
        this.configModelBuilder = configModelBuilder;
    }

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
