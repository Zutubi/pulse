package com.zutubi.pulse.master.tove.wizard;

import com.opensymphony.xwork.ValidationAware;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.tove.handler.ExtensionOptionProvider;
import com.zutubi.pulse.master.tove.model.FormDescriptor;
import com.zutubi.pulse.master.tove.model.FormDescriptorFactory;
import com.zutubi.pulse.master.tove.model.SelectFieldDescriptor;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Some types need a little more than just a single wizard state / form to present all of the
 * information.  In particular, types that define multiple extension points.  These types need two-step
 * wizards.
 */
public class TwoStepStateBuilder
{
    private static final String SELECT_FIELD_NAME = "wizard.select";

    private MutableRecord selectionRecord;
    private CompositeType baseType;
    private List<CompositeType> extensions;
    private SelectWizardState selectState;
    private UnknownTypeState unknownTypeState;
    private Map<String, ConfigurationWizardState> configureStates = new TreeMap<String, ConfigurationWizardState>();

    public TwoStepStateBuilder(AbstractTypeWizard wizard, int ord, String parentPath, CompositeType baseType, List<CompositeType> extensions)
    {
        this.baseType = baseType;
        this.extensions = extensions;
        selectState = new SelectWizardState(ord);
        unknownTypeState = new UnknownTypeState(ord, baseType);
        for(CompositeType extensionType: baseType.getExtensions())
        {
            ConfigurationWizardState configState = new ConfigurationWizardState(wizard, ord, parentPath, extensionType);
            SpringComponentContext.autowire(configState);
            configureStates.put(extensionType.getSymbolicName(), configState);
        }

        this.selectionRecord = new MutableRecordImpl();
    }

    private String getSelectedSymbolicName()
    {
        return (String) selectionRecord.get(SELECT_FIELD_NAME);
    }

    public SelectWizardState getSelectState()
    {
        return selectState;
    }

    public List<AbstractChainableState> getChainableStates()
    {
        LinkedList<AbstractChainableState> result = new LinkedList<AbstractChainableState>(configureStates.values());
        result.add(unknownTypeState);
        return result;
    }

    public class SelectWizardState implements TypeWizardState
    {
        String id;

        public SelectWizardState(int ord)
        {
            id = Integer.toString(ord) + ".select";
        }

        public String getId()
        {
            return id;
        }

        public CompositeType getType()
        {
            return baseType;
        }

        public CompositeType getConfiguredBaseType()
        {
            return null;
        }

        public String getName()
        {
            return "select";
        }

        public Record getRenderRecord()
        {
            return selectionRecord;
        }

        public MutableRecord getDataRecord()
        {
            return selectionRecord;
        }

        public void updateRecord(Map parameters)
        {
            String[] value = (String[]) parameters.get(SELECT_FIELD_NAME);
            if(value != null)
            {
                selectionRecord.put(SELECT_FIELD_NAME, value[0]);
            }
        }

        public Messages getMessages()
        {
            return Messages.getInstance(baseType.getClazz());
        }

        public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name)
        {
            FormDescriptor descriptor = new FormDescriptor();
            descriptor.setName(name);
            descriptor.setId("select.state");

            SelectFieldDescriptor select = new SelectFieldDescriptor();
            select.setName(SELECT_FIELD_NAME);
            select.addParameter("width", 300);

            ExtensionOptionProvider optionProvider = new ExtensionOptionProvider(extensions);
            select.setListKey(optionProvider.getOptionValue());
            select.setListValue(optionProvider.getOptionText());
            select.setList(optionProvider.getOptions(null, null));

            descriptor.add(select);
            return descriptor;
        }

        public boolean validate(ValidationAware validationCallback)
        {
            return true;
        }

        public TypeWizardState getNextState()
        {
            String selectedType = getSelectedSymbolicName();
            if(selectedType == null)
            {
                return unknownTypeState;
            }
            else
            {
                return configureStates.get(selectedType);
            }
        }

        public boolean hasFields()
        {
            return true;
        }

        public boolean hasConfigurationCheck()
        {
            return false;
        }
    }

    private class ConfigurationWizardState extends AbstractTypeWizardState
    {
        private CompositeType type;
        private MutableRecord record;

        public ConfigurationWizardState(AbstractTypeWizard wizard, int ord, String parentPath, CompositeType type)
        {
            super(wizard, Integer.toString(ord) + "." + baseType.getSymbolicName(), parentPath, baseType);
            this.type = type;
            this.record = type.createNewRecord(true);
        }

        public CompositeType getType()
        {
            return type;
        }

        public CompositeType getConfiguredBaseType()
        {
            return baseType;
        }

        public Record getRenderRecord()
        {
            return getDataRecord();
        }

        public MutableRecord getDataRecord()
        {
            return record;
        }
    }
}
