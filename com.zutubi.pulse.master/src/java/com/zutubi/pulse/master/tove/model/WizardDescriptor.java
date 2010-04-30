package com.zutubi.pulse.master.tove.model;

import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizard;
import com.zutubi.pulse.master.tove.wizard.TypeWizardState;
import com.zutubi.pulse.master.tove.wizard.WizardState;
import com.zutubi.pulse.master.tove.wizard.WizardTransition;
import com.zutubi.pulse.master.tove.wizard.webwork.ConfigurationWizardInterceptor;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.List;

/**
 *
 *
 */
public class WizardDescriptor extends AbstractParameterised implements Descriptor
{
    private AbstractTypeWizard wizardInstance;

    private boolean ajax;
    private boolean decorate;
    private String namespace;

    private FormDescriptorFactory formDescriptorFactory;

    public WizardDescriptor(AbstractTypeWizard wizardInstance)
    {
        this.wizardInstance = wizardInstance;
    }

    public Wizard instantiate(String path, Record record)
    {
        Wizard wizard = new Wizard();
        wizard.setDecorate(decorate);

        // create the form wizard for the wizard.
        WizardState currentState = wizardInstance.getCurrentState();
        wizard.setTemplateCollectionItem(wizardInstance.isTemplate() && PathUtils.getPathElements(wizardInstance.getInsertPath()).length == 1);

        FormDescriptor formDescriptor = currentState.createFormDescriptor(formDescriptorFactory, path, "wizardForm");
        formDescriptor.setAction("wizard");
        formDescriptor.setAjax(ajax);
        formDescriptor.setNamespace(namespace);

        if (record instanceof TemplateRecord)
        {
            TemplateFormDecorator templateDecorator = new TemplateFormDecorator((TemplateRecord) record);
            templateDecorator.decorate(formDescriptor);
        }

        // decorate the form so that it fits into the wizard.
        decorate(formDescriptor);

        wizard.setForm(formDescriptor.instantiate(path, record));

        for(TypeWizardState state: wizardInstance.getStates())
        {
            wizard.addStep(state.getId(), state.getType(), state.getName());
        }

        wizard.setCurrentStep(wizardInstance.getCurrentState().getId());

        return wizard;
    }

    private void decorate(FormDescriptor descriptor)
    {
        List<String> actions = CollectionUtils.map(wizardInstance.getAvailableActions(), new Mapping<WizardTransition, String>()
        {
            public String map(WizardTransition o)
            {
                return o.name().toLowerCase();
            }
        });

        descriptor.setActions(actions);

        HiddenFieldDescriptor hidden = new HiddenFieldDescriptor();
        hidden.setName(ConfigurationWizardInterceptor.STATE_ID_PARAMETER);
        hidden.setValue(wizardInstance.getCurrentState().getId());
        descriptor.add(hidden);

        hidden = new HiddenFieldDescriptor();
        hidden.setName("symbolicName");
        hidden.setValue(wizardInstance.getCurrentState().getType().getSymbolicName());
        descriptor.add(hidden);
    }

    public void setAjax(boolean ajax)
    {
        this.ajax = ajax;
    }

    public void setDecorate(boolean decorate)
    {
        this.decorate = decorate;
    }

    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }
}
