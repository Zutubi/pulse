package com.zutubi.pulse.master.rest.wizards;

import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.TypedWizardStepModel;
import com.zutubi.pulse.master.rest.model.WizardModel;
import com.zutubi.pulse.master.rest.model.WizardTypeModel;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookTaskConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.CompatibleHooks;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wizard for creating build hooks with tasks.
 */
public class BuildHookConfigurationWizard implements ConfigurationWizard
{
    private WizardModelBuilder wizardModelBuilder;
    private TypeRegistry typeRegistry;

    @Override
    public WizardModel buildModel(CompositeType type, FormContext context) throws TypeException
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForType("", type, context));
        TypedWizardStepModel taskStep = wizardModelBuilder.buildStepForType("task", typeRegistry.getType(BuildHookTaskConfiguration.class), context);
        for (WizardTypeModel taskModel: taskStep.getTypes())
        {
            CompositeType taskType = typeRegistry.getType(taskModel.getType().getSymbolicName());
            CompatibleHooks annotation = taskType.getAnnotation(CompatibleHooks.class, true);
            if (annotation != null)
            {
                Set<String> symbolicNames = new HashSet<>();
                for (Class<? extends BuildHookConfiguration> clazz: annotation.value())
                {
                    symbolicNames.add(typeRegistry.getType(clazz).getSymbolicName());
                }
                taskModel.setTypeFilter("", symbolicNames);
            }
        }
        model.appendStep(taskStep);
        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, String parentPath, String baseName, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models) throws TypeException
    {
        MutableRecord hookRecord = wizardModelBuilder.buildAndValidateRecord(type, parentPath, templateOwnerPath, concrete, models, "");
        hookRecord.put("task", wizardModelBuilder.buildAndValidateRecord(typeRegistry.getType(BuildHookTaskConfiguration.class), parentPath, templateOwnerPath, concrete, models, "task"));
        return hookRecord;
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
