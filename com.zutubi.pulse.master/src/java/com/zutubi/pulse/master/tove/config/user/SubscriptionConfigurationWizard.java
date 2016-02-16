package com.zutubi.pulse.master.tove.config.user;

import com.google.common.collect.Sets;
import com.zutubi.pulse.master.notifications.renderer.BuildResultRenderer;
import com.zutubi.pulse.master.notifications.renderer.TemplateInfo;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.model.TypedWizardStepModel;
import com.zutubi.tove.ui.model.WizardModel;
import com.zutubi.tove.ui.model.WizardTypeModel;
import com.zutubi.tove.ui.wizards.ConfigurationWizard;
import com.zutubi.tove.ui.wizards.WizardContext;
import com.zutubi.tove.ui.wizards.WizardModelBuilder;

import java.util.HashSet;
import java.util.List;

/**
 * A wizard that adds a step to configure a condition when the user adds a project build subscripton.
 */
public class SubscriptionConfigurationWizard implements ConfigurationWizard
{
    public static final String KEY_CONDITION = "condition";

    private WizardModelBuilder wizardModelBuilder;
    private TypeRegistry typeRegistry;
    private ConfigurationProvider configurationProvider;
    private BuildResultRenderer buildResultRenderer;

    @Override
    public WizardModel buildModel(CompositeType type, FormContext context) throws TypeException
    {
        WizardModel model = new WizardModel();

        String projectType = typeRegistry.getType(ProjectSubscriptionConfiguration.class).getSymbolicName();
        TypedWizardStepModel rootStep = wizardModelBuilder.buildStepForType("", type, context);
        rootStep.setDefaultType(projectType);

        String defaultContactPath = null;
        String closestPath = context.getClosestExistingPath();
        if (closestPath != null)
        {
            UserConfiguration user = configurationProvider.getAncestorOfType(closestPath, UserConfiguration.class);
            if (user != null)
            {
                ContactConfiguration primaryContact = user.getPrimaryContact();
                if (primaryContact != null)
                {
                    defaultContactPath = primaryContact.getConfigurationPath();
                }
            }
        }

        for (WizardTypeModel rootType: rootStep.getTypes())
        {
            if (defaultContactPath != null)
            {
                rootType.getType().addSimplePropertyDefault("contact", defaultContactPath);
            }

            String htmlTemplateName = null;
            List<TemplateInfo> templates = buildResultRenderer.getAvailableTemplates(!rootType.getType().getSymbolicName().equals(projectType));
            for (TemplateInfo template: templates)
            {
                if (template.getDisplay().toLowerCase().equals("html email"))
                {
                    htmlTemplateName = template.getTemplate();
                    break;
                }
            }

            if (htmlTemplateName != null)
            {
                rootType.getType().addSimplePropertyDefault("template", htmlTemplateName);
            }
        }

        model.appendStep(rootStep);

        TypedWizardStepModel conditionStep = wizardModelBuilder.buildStepForClass(KEY_CONDITION, SubscriptionConditionConfiguration.class, context);
        HashSet<String> compatible = Sets.newHashSet(projectType);
        for (WizardTypeModel conditionType: conditionStep.getTypes())
        {
            conditionType.setTypeFilter("", compatible);
        }
        model.appendStep(conditionStep);

        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, WizardContext wizardContext) throws TypeException
    {
        MutableRecord subscriptionRecord = wizardModelBuilder.buildAndValidateRecord(type, "", wizardContext);
        if (wizardContext.getModels().containsKey(KEY_CONDITION))
        {
            CompositeType conditionType = wizardModelBuilder.getCompositeType(SubscriptionConditionConfiguration.class);
            subscriptionRecord.put(KEY_CONDITION, wizardModelBuilder.buildAndValidateRecord(conditionType, KEY_CONDITION, wizardContext));
        }
        return subscriptionRecord;
    }

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }
}
